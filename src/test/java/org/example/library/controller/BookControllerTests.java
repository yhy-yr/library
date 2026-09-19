package org.example.library.controller;

import org.example.library.exception.ConflictException;
import org.example.library.exception.GlobalExceptionHandler;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                        new StringHttpMessageConverter(
                                StandardCharsets.UTF_8
                        )
                )
                .defaultResponseCharacterEncoding(
                        StandardCharsets.UTF_8
                )
                .build();
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
}