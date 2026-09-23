package org.example.library.controller;

import org.example.library.entity.Book;
import org.example.library.entity.BookStatus;
import org.example.library.exception.ConflictException;
import org.example.library.exception.GlobalExceptionHandler;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.example.library.entity.BookStatus.ON_SALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookControllerTests {

    // 用来模拟 HTTP 请求，不会真正启动 Tomcat。
    private MockMvc mockMvc;

    // Controller 使用假的 Service，避免连接 Repository 和数据库。
    private BookService bookService;

    /*
     * 每个测试执行前都会运行这个方法。
     * 它负责组装 Controller 和全局异常处理器。
     */
    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new BookController(bookService))
                .setControllerAdvice(new GlobalExceptionHandler())

                // 让模拟响应使用 UTF-8 写入和读取中文。
                .setMessageConverters(
                        // 用于处理异常返回的中文字符串。
                        new StringHttpMessageConverter(
                                StandardCharsets.UTF_8
                        ),

                        // 用于把 JSON 请求转换成 CreateBookRequest。
                        new JacksonJsonHttpMessageConverter()
                )
                .defaultResponseCharacterEncoding(
                        StandardCharsets.UTF_8
                )
                .build();
    }
    @Test
    void shouldReturn400WhenCreateBookRequestIsInvalid()
            throws Exception {

        /*
         * 这个 JSON 可以正常解析，
         * 但多个字段违反 DTO 上的校验规则。
         */
        String invalidJson = """
            {
              "title": "   ",
              "author": "测试作者",
              "isbn": "9787111000001",
              "price": -1.00,
              "stock": -1,
              "publishedDate": "2026-09-22"
            }
            """;

        /*
         * 模拟客户端发送创建图书请求。
         */
        mockMvc.perform(
                        post("/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )

                /*
                 * @Valid 应在进入 Controller 方法体前发现错误，
                 * Spring 随后返回 400 Bad Request。
                 */
                .andExpect(status().isBadRequest())

// 验证整体错误结构。
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("参数校验失败"))

// 验证三个字段错误都被一次性返回。
                .andExpect(
                        jsonPath("$.fieldErrors.title")
                                .value("书名不能为空")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.price")
                                .value("价格不能为负数")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.stock")
                                .value("库存不能为负数")
                );

        /*
         * 这是最关键的反向证据：
         * 参数不合法时，BookService 的任何方法都不能执行。
         */
        verifyNoInteractions(bookService);
    }

    @Test
    void shouldReturn409WhenDeletingOffSaleBook() throws Exception {
        /*
         * delete() 没有返回值，因此使用 doThrow() 模拟异常。
         *
         * 规定调用 bookService.delete(1L) 时，
         * Service 抛出“图书已经下架”的冲突异常。
         */
        doThrow(new ConflictException("图书已经下架"))
                .when(bookService)
                .delete(1L);

        /*
         * 模拟客户端发送 DELETE /books/1。
         */
        mockMvc.perform(delete("/books/1"))

                // 验证 HTTP 状态码是不是 409。
                .andExpect(status().isConflict())

                // 验证响应正文是否保留了异常消息。
                .andExpect(content().string("图书已经下架"));
    }
    @Test
    void shouldReturn404WhenDeletingMissingBook() throws Exception {
        /*
         * 模拟 Service 查不到图书。
         */
        doThrow(new ResourceNotFoundException("图书不存在"))
                .when(bookService)
                .delete(999L);

        /*
         * 模拟客户端删除不存在的图书。
         */
        mockMvc.perform(delete("/books/999"))

                // 验证响应状态为 404。
                .andExpect(status().isNotFound())

                // 验证响应正文。
                .andExpect(content().string("图书不存在"));
    }
    @Test
    void shouldReturn204WhenBookIsDeleted() throws Exception {
        /*
         * Mockito 模拟的 void 方法默认什么也不做，
         * 因此这里不需要配置 when() 或 doThrow()。
         */

        // 模拟客户端成功删除 ID 为 1 的图书。
        mockMvc.perform(delete("/books/1"))

                // DELETE 成功并且没有响应正文，应返回 204。
                .andExpect(status().isNoContent());

        /*
         * 除了检查 HTTP 状态，还要确认 Controller
         * 确实调用了 Service。
         */
        verify(bookService).delete(1L);
    }
    @Test
    void shouldCreateBookFromValidRequest() throws Exception {
        // 一份完全符合 CreateBookRequest 校验规则的 JSON。
        String validJson = """
            {
              "title": "Java 核心技术",
              "author": "Cay S. Horstmann",
              "isbn": "9787111000001",
              "price": 99.00,
              "stock": 10,
              "publishedDate": "2026-09-22"
            }
            """;

        /*
         * 发送合法创建请求。
         * 创建成功应该返回 HTTP 201。
         */
        mockMvc.perform(
                        post("/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validJson)
                )
                .andExpect(status().isCreated());

        /*
         * ArgumentCaptor 用来捕获 Controller
         * 实际传给 BookService 的 Book 对象。
         */
        ArgumentCaptor<Book> bookCaptor =
                ArgumentCaptor.forClass(Book.class);

        verify(bookService)
                .create(bookCaptor.capture());

        Book createdBook = bookCaptor.getValue();

        // 验证 JSON 字段被正确映射。
        assertEquals(
                "Java 核心技术",
                createdBook.getTitle()
        );
        assertEquals(
                "9787111000001",
                createdBook.getIsbn()
        );
        assertEquals(
                10,
                createdBook.getStock()
        );

        /*
         * JSON 中没有 status；
         * 它必须由服务器设置成初始状态。
         */
        assertEquals(
                ON_SALE,
                createdBook.getStatus()
        );

        /*
         * 客户端不能控制数据库 ID，
         * 创建前它应该仍然为 null。
         */
        assertNull(createdBook.getId());
    }
    @Test
    void shouldUpdateBookDetailsWithoutChangingStatus()
            throws Exception {

        String validJson = """
        {
          "title": "修改后的书名",
          "author": "修改后的作者",
          "isbn": "9787111000001",
          "price": 88.00,
          "stock": 5,
          "publishedDate": "2026-09-22"
        }
        """;

        /*
         * Mockito 默认让 int 方法返回 0。
         * 但 Controller 会把更新 0 行解释为“图书不存在”，返回 404。
         *
         * 因此这里模拟数据库成功更新了一行。
         */
        when(
                bookService.update(
                        eq(7L),
                        any(Book.class)
                )
        ).thenReturn(1);

        mockMvc.perform(
                        put("/books/7")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validJson)
                )

                // 成功更新且没有响应正文。
                .andExpect(status().isNoContent());

        /*
         * 捕获 Controller 实际传给 Service 的 Book，
         * 检查 DTO 到实体的转换结果。
         */
        ArgumentCaptor<Book> bookCaptor =
                ArgumentCaptor.forClass(Book.class);

        verify(bookService).update(
                eq(7L),
                bookCaptor.capture()
        );

        Book updatedBook = bookCaptor.getValue();

        assertEquals(
                "修改后的书名",
                updatedBook.getTitle()
        );

        assertEquals(
                5,
                updatedBook.getStock()
        );

        /*
         * 普通资料修改不能给 status 赋值；
         * 上下架必须通过专门的状态接口完成。
         */
        assertNull(updatedBook.getStatus());
    }
    @Test
    void shouldReturn404WhenUpdatingMissingBook()
            throws Exception {

        String validJson = """
        {
          "title": "修改后的书名",
          "author": "修改后的作者",
          "isbn": "9787111000001",
          "price": 88.00,
          "stock": 5,
          "publishedDate": "2026-09-22"
        }
        """;

        /*
         * 模拟 Repository 没有更新任何记录。
         */
        when(
                bookService.update(
                        eq(999L),
                        any(Book.class)
                )
        ).thenReturn(0);

        mockMvc.perform(
                        put("/books/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validJson)
                )
                .andExpect(status().isNotFound());

        /*
         * 即使最终返回 404，也要确认请求确实到达了 Service，
         * 并且使用的是路径中的 ID。
         */
        verify(bookService).update(
                eq(999L),
                any(Book.class)
        );
    }
    @Test
    void shouldReturn400WhenUpdateBookRequestIsInvalid()
            throws Exception {

        String invalidJson = """
        {
          "title": "   ",
          "author": "测试作者",
          "isbn": "9787111000001",
          "price": 88.00,
          "stock": 5,
          "publishedDate": "2026-09-22"
        }
        """;

        mockMvc.perform(
                        put("/books/7")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )

                // DTO 参数校验失败。
                .andExpect(status().isBadRequest())

                // 确认具体是 title 字段违反规则。
                .andExpect(
                        jsonPath("$.fieldErrors.title")
                                .value("书名不能为空")
                );

        /*
         * @Valid 在进入 Controller 方法体前拦截了请求，
         * 所以 Service 的任何方法都不应该执行。
         */
        verifyNoInteractions(bookService);
    }
}