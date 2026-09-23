package org.example.library.controller;

import org.example.library.entity.Reader;
import org.example.library.entity.ReaderStatus;
import org.example.library.exception.GlobalExceptionHandler;
import org.example.library.service.ReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReaderControllerTests {

    private MockMvc mockMvc;
    private ReaderService readerService;

    @BeforeEach
    void setUp() {
        readerService = mock(ReaderService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReaderController(readerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter()
                )
                .build();
    }

    @Test
    void shouldCreateReaderWithServerAssignedStatus()
            throws Exception {

        String validJson = """
            {
              "name": "张三",
              "phone": "13800138000"
            }
            """;

        // 发出注册请求，检查成功时的 HTTP 状态。
        mockMvc.perform(
                post("/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson)
        ).andExpect(status().isCreated());

        // 捕获 Controller 实际交给 Service 的 Reader。
        ArgumentCaptor<Reader> captor =
                ArgumentCaptor.forClass(Reader.class);

        verify(readerService).create(captor.capture());
        Reader createdReader = captor.getValue();

        assertEquals("张三", createdReader.getName());
        assertEquals("13800138000", createdReader.getPhone());

        // 客户端没提供状态；检查服务器设置的初始状态。
        assertEquals(
                ReaderStatus.ACTIVE,
                createdReader.getStatus()
        );
    }
    @Test
    void shouldRejectReaderWithBlankName()
            throws Exception {

        String invalidJson = """
        {
          "name": "   ",
          "phone": "13800138000"
        }
        """;

        mockMvc.perform(
                        post("/readers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                // @NotBlank 校验失败，对应什么 HTTP 状态？
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.fieldErrors.name")
                                .value("读者姓名不能为空")
                );

        // 校验失败时，Service 不应收到任何调用。
        verifyNoInteractions(readerService);
    }



}