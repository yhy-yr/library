package org.example.library.controller;

import org.example.library.exception.GlobalExceptionHandler;
import org.example.library.service.BorrowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BorrowControllerTests {

    private MockMvc mockMvc;
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        borrowService = mock(BorrowService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new BorrowController(borrowService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter()
                )
                .build();
    }

    @Test
    void shouldRejectInvalidBorrowIdsBeforeCallingService()
            throws Exception {

        String invalidJson = """
            {
              "readerId": null,
              "bookId": 0
            }
            """;

        mockMvc.perform(
                        post("/borrows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                // 两个字段校验失败，整个请求是什么状态？
                .andExpect(status().isBadRequest())

                // null 违反 @NotNull。
                .andExpect(
                        jsonPath("$.fieldErrors.readerId")
                                .value("读者 ID 不能为空")
                )

                // 0 违反 @Positive。
                .andExpect(
                        jsonPath("$.fieldErrors.bookId")
                                .value("图书 ID 必须大于 0")
                );

        // 校验失败，借书业务逻辑不能执行。
        verifyNoInteractions(borrowService);
    }
}