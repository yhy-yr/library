package org.example.library.service;

import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.ReaderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.example.library.entity.ReaderStatus.DISABLED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * ReaderService 的纯单元测试。
 * 不启动 Spring，不连接数据库。
 */
@ExtendWith(MockitoExtension.class)
class ReaderServiceTests {

    // 假的 Repository，不执行真实 SQL。
    @Mock
    private ReaderRepository readerRepository;

    // 创建 ReaderService，并自动注入假的 Repository。
    @InjectMocks
    private ReaderService readerService;

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingReader() {
        /*
         * updateStatus() 返回 0，
         * 表示数据库没有找到这个读者。
         */
        when(readerRepository.updateStatus(999L, DISABLED))
                .thenReturn(0);

        // Service 应把“更新 0 行”转换成资源不存在异常。
        assertThrows(
                ResourceNotFoundException.class,
                () -> readerService.updateStatus(999L, DISABLED)
        );
    }
    @Test
    void shouldDisableExistingReader() {
        /*
         * 返回 1 表示数据库成功更新了一条读者记录。
         */
        when(readerRepository.updateStatus(1L, DISABLED))
                .thenReturn(1);

        // 执行状态更新。
        readerService.updateStatus(1L, DISABLED);

        /*
         * updateStatus() 没有返回值，
         * 所以使用 verify() 检查 Repository 是否收到正确参数。
         */
        verify(readerRepository)
                .updateStatus(1L, DISABLED);
    }

}