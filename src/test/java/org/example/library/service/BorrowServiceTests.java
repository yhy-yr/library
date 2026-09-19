package org.example.library.service;

import org.example.library.entity.BorrowRecord;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRecordRepository;
import org.example.library.repository.ReaderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.example.library.entity.BorrowStatus.BORROWED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * 这是纯单元测试：
 * 不启动 Spring、不连接数据库，也不占用 8080。
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceTests {

    // 模拟读者数据访问对象。
    @Mock
    private ReaderRepository readerRepository;

    // 模拟图书数据访问对象。
    @Mock
    private BookRepository bookRepository;

    // 模拟借阅记录数据访问对象。
    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    // 创建 BorrowService，并自动注入上面三个假 Repository。
    @InjectMocks
    private BorrowService borrowService;

    @Test
    void shouldReturnBorrowedBook() {
        /*
         * 准备一条“尚未归还”的借阅记录：
         * 借阅记录 ID 是 10，图书 ID 是 5。
         */
        BorrowRecord record = new BorrowRecord(
                10L,
                1L,
                5L,
                null,
                null,
                null,
                BORROWED,
                null,
                null
        );

        // 模拟根据借阅 ID 找到这条记录。
        when(borrowRecordRepository.findById(10L))
                .thenReturn(Optional.of(record));

        // 模拟成功把借阅记录更新为已归还。
        when(borrowRecordRepository.markReturned(10L))
                .thenReturn(1);

        // 模拟成功恢复图书库存。
        when(bookRepository.changeStock(5L, 1))
                .thenReturn(1);

        // 执行还书业务。
        borrowService.returnBook(10L);

        // 验证借阅记录确实被标记为已归还。
        verify(borrowRecordRepository)
                .markReturned(10L);

        // 验证归还后，正确图书的库存增加了 1。
        verify(bookRepository)
                .changeStock(5L, +1);
    }
}
