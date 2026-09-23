package org.example.library.service;

import org.example.library.dto.BorrowRequest;
import org.example.library.entity.Book;
import org.example.library.entity.BorrowRecord;
import org.example.library.entity.BorrowStatus;
import org.example.library.entity.Reader;
import org.example.library.exception.ConflictException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRecordRepository;
import org.example.library.repository.ReaderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.example.library.entity.BookStatus.ON_SALE;
import static org.example.library.entity.BorrowStatus.BORROWED;
import static org.example.library.entity.BorrowStatus.RETURNED;
import static org.example.library.entity.ReaderStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
        BorrowRecord record = createBorrowRecord(BORROWED);

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
    @Test
    void shouldThrowNotFoundWhenBorrowRecordDoesNotExist() {
        // 模拟数据库中不存在 ID 为 999 的借阅记录。
        when(borrowRecordRepository.findById(999L))
                .thenReturn(Optional.empty());

        // 找不到借阅记录时，应立即抛出资源不存在异常。
        assertThrows(
                ResourceNotFoundException.class,
                () -> borrowService.returnBook(999L)
        );

        // 查找已经失败，不能继续把记录标记为已归还。
        verify(borrowRecordRepository, never())
                .markReturned(999L);

        /*
         * 连借阅记录都不存在，也就不知道对应的 bookId，
         * 因此不能修改任何图书库存。
         */
        verify(bookRepository, never())
                .changeStock(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyInt()
                );
    }
    @Test
    void shouldThrowIllegalStateWhenBookCannotBeRestocked() {
        // 准备一条正常借阅中的记录，对应图书 ID 为 5。
        BorrowRecord record = createBorrowRecord(BORROWED);

        when(borrowRecordRepository.findById(10L))
                .thenReturn(Optional.of(record));

        // 借阅记录成功标记为已归还。
        when(borrowRecordRepository.markReturned(10L))
                .thenReturn(1);

        /*
         * 库存更新返回 0，说明借阅记录指向的图书不存在。
         * 这不是普通用户操作冲突，而是系统数据不一致。
         */
        when(bookRepository.changeStock(5L, +1))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> borrowService.returnBook(10L)
        );

        // 确认系统确实尝试过更新正确图书的库存。
        verify(bookRepository)
                .changeStock(5L, +1);
    }
    @Test
    void shouldThrowConflictWhenBookAlreadyReturned() {
        /*
         * 准备一条状态已经是 RETURNED 的借阅记录。
         */
        BorrowRecord record = createBorrowRecord(RETURNED);

        // 模拟成功找到这条已归还记录。
        when(borrowRecordRepository.findById(10L))
                .thenReturn(Optional.of(record));

        // 重复还书属于状态冲突。
        assertThrows(
                ConflictException.class,
                () -> borrowService.returnBook(10L)
        );

        // 已归还的记录不能再次执行 UPDATE。
        verify(borrowRecordRepository, never())
                .markReturned(10L);

        // 也不能重复增加图书库存。
        verify(bookRepository, never())
                .changeStock(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyInt()
                );
    }
    @Test
    void shouldThrowConflictWhenRecordWasReturnedConcurrently() {
        // 第一次查询时，记录看起来仍然处于借阅中。
        BorrowRecord record = createBorrowRecord(BORROWED);

        when(borrowRecordRepository.findById(10L))
                .thenReturn(Optional.of(record));

        /*
         * 返回 0 表示带有 status = BORROWED 条件的 UPDATE
         * 没有更新到记录，通常说明它刚刚被其他请求归还了。
         */
        when(borrowRecordRepository.markReturned(10L))
                .thenReturn(0);

        // 当前请求应得到状态冲突异常。
        assertThrows(
                ConflictException.class,
                () -> borrowService.returnBook(10L)
        );

        /*
         * 借阅记录没有被当前请求成功更新，
         * 所以当前请求绝不能增加图书库存。
         */
        verify(bookRepository, never())
                .changeStock(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyInt()
                );
    }
    @Test
    void shouldBorrowAvailableBook() {
        // 准备借书请求：读者 ID 为 1，图书 ID 为 5。
        BorrowRequest request = new BorrowRequest(1L, 5L);

        // 准备正常读者。
        Reader reader = createActiveReader();

        // 准备在售图书。
        Book book = createOnSaleBook();

        // 模拟成功找到读者。
        when(readerRepository.findById(1L))
                .thenReturn(Optional.of(reader));

        // 模拟该读者当前一本书都没有借。
        when(borrowRecordRepository.countBorrowedByReaderId(1L))
                .thenReturn(0L) ;

        // 模拟成功找到图书。
        when(bookRepository.findById(5L))
                .thenReturn(Optional.of(book));

        // 模拟读者尚未借阅这本书。
        when(borrowRecordRepository.existsBorrowedRecord(1L, 5L))
                .thenReturn(false);

        /*
         * 模拟原子扣减库存成功。
         * 借出一本书时，库存变化量应为负数。
         */
        when(bookRepository.changeStock(5L, -1))
                .thenReturn(1);

        // 执行借书业务。
        borrowService.borrow(request);

        // 验证库存确实减少了一本。
        verify(bookRepository)
                .changeStock(5L, -1);

        // 验证借阅记录被保存。
        verify(borrowRecordRepository)
                .save(1L, 5L);
    }
    @Test
    void shouldRejectBorrowWhenReaderReachedLimit() {
        BorrowRequest request = new BorrowRequest(1L, 5L);

        Reader reader = createActiveReader();

        when(readerRepository.findById(1L))
                .thenReturn(Optional.of(reader));

        // 模拟该读者已经借满 5 本。
        when(borrowRecordRepository.countBorrowedByReaderId(1L))
                .thenReturn(5L);

        // 借阅数量达到上限，应抛出状态冲突异常。
        assertThrows(
                ConflictException.class,
                () -> borrowService.borrow(request)
        );

        /*
         * 在检查借阅上限时就已经失败，
         * 所以不应该执行任何图书查询或库存更新。
         */
        verifyNoInteractions(bookRepository);

        // 也不能创建新的借阅记录。
        verify(borrowRecordRepository, never())
                .save(anyLong(), anyLong());
    }
    @Test
    void shouldRejectBorrowWhenStockIsInsufficient() {
        BorrowRequest request = new BorrowRequest(1L, 5L);

        // 准备正常读者。
        Reader reader = createActiveReader();

        // 准备在售图书。
        Book book = createOnSaleBook();

        // 前面的业务检查全部通过。
        when(readerRepository.findById(1L))
                .thenReturn(Optional.of(reader));

        when(borrowRecordRepository.countBorrowedByReaderId(1L))
                .thenReturn(0L);

        when(bookRepository.findById(5L))
                .thenReturn(Optional.of(book));

        when(borrowRecordRepository.existsBorrowedRecord(1L, 5L))
                .thenReturn(false);

        /*
         * 原子扣库存返回 0，表示库存不足，
         * 或库存刚刚被另一个并发请求借完。
         */
        when(bookRepository.changeStock(5L, -1))
                .thenReturn(0);

        // 库存不足应转换成业务冲突异常。
        assertThrows(
                ConflictException.class,
                () -> borrowService.borrow(request)
        );

        // 确认系统确实尝试过原子扣库存。
        verify(bookRepository)
                .changeStock(5L, -1);

        // 扣库存失败，绝不能生成借阅记录。
        verify(borrowRecordRepository, never())
                .save(1L, 5L);
    }
    @Test
    void shouldRejectDuplicateBorrow() {
        BorrowRequest request = new BorrowRequest(1L, 5L);

        // 准备正常读者。
        Reader reader = createActiveReader();

        // 准备在售图书。
        Book book = createOnSaleBook();

        when(readerRepository.findById(1L))
                .thenReturn(Optional.of(reader));

        // 读者还没有达到 5 本上限。
        when(borrowRecordRepository.countBorrowedByReaderId(1L))
                .thenReturn(0L);

        when(bookRepository.findById(5L))
                .thenReturn(Optional.of(book));

        /*
         * 模拟已经存在相同读者、相同图书，
         * 并且状态为 BORROWED 的借阅记录。
         */
        when(borrowRecordRepository.existsBorrowedRecord(1L, 5L))
                .thenReturn(true);

        // 重复借阅同一本书属于业务冲突。
        assertThrows(
                ConflictException.class,
                () -> borrowService.borrow(request)
        );

        // 发现重复借阅后，不能继续扣库存。
        verify(bookRepository, never())
                .changeStock(anyLong(), anyInt());

        // 也不能保存第二条借阅记录。
        verify(borrowRecordRepository, never())
                .save(anyLong(), anyLong());
    }
    /*
     * 创建测试用借阅记录。
     *
     * 固定借阅记录 ID、读者 ID 和图书 ID，
     * 每个测试只需要决定当前借阅状态。
     */
    private BorrowRecord createBorrowRecord(
            BorrowStatus status
    ) {
        return new BorrowRecord(
                10L,
                1L,
                5L,
                null,
                null,
                null,
                status,
                null,
                null
        );
    }
    private Reader createActiveReader() {
        Reader reader = new Reader();
        reader.setStatus(ACTIVE);
        return reader;
    }

    private Book createOnSaleBook() {
        Book book = new Book();
        book.setStatus(ON_SALE);
        return book;
    }


}
