package org.example.library.service;

import org.example.library.entity.Book;
import org.example.library.exception.ConflictException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.example.library.entity.BookStatus.OFF_SALE;
import static org.example.library.entity.BookStatus.ON_SALE;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTests {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingBook() {
        when(bookRepository.updateStatus(999L, OFF_SALE))
                .thenReturn(0);

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.updateStatus(999L, OFF_SALE)
        );
    }
    @Test
    void shouldUpdateExistingBookStatus() {
        when(bookRepository.updateStatus(1L, OFF_SALE))
                .thenReturn(1);

        bookService.updateStatus(1L, OFF_SALE);

        verify(bookRepository)
                .updateStatus(1L, OFF_SALE);
    }
    @Test
    void shouldThrowConflictWhenDeletingOffSaleBook() {
        Book book = new Book();
        book.setStatus(OFF_SALE);

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        assertThrows(
                ConflictException.class,
                () -> bookService.delete(1L)
        );

        verify(bookRepository, never())
                .updateStatus(1L, OFF_SALE);
    }
    @Test
    void shouldDeleteOnSaleBook() {
        Book book = new Book();
        book.setStatus(ON_SALE);

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(bookRepository.updateStatus(1L, OFF_SALE))
                .thenReturn(1);

        bookService.delete(1L);

        verify(bookRepository)
                .updateStatus(1L, OFF_SALE);
    }
    @Test
    void shouldThrowNotFoundWhenDeletingMissingBook() {
        when(bookRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.delete(999L)
        );

        verify(bookRepository, never())
                .updateStatus(999L, OFF_SALE);
    }
    @Test
    void shouldRejectBookWithBlankTitle() {
        // 准备一本书名只有空格的非法图书。
        Book book = new Book();
        book.setTitle("   ");

        /*
         * Service 应该在参数校验阶段抛出异常，
         * 不允许程序继续执行到 Repository。
         */
        assertThrows(
                IllegalArgumentException.class,
                () -> bookService.create(book)
        );

        /*
         * 因为参数不合法，save() 一次都不应该执行。
         * any(Book.class) 表示任意 Book 对象。
         */
        verify(bookRepository, never())
                .save(any(Book.class));
    }
    @Test
    void shouldConvertDuplicateIsbnToConflict() {
        // create() 会进行完整校验，因此需要准备一份合法图书数据。
        Book book = createValidBook();

        // 模拟数据库唯一索引发现 ISBN 重复时产生的原始异常。
        DuplicateKeyException originalException =
                new DuplicateKeyException("ISBN duplicate");

        /*
         * 当 Repository 尝试保存这本图书时，
         * 不返回成功结果，而是抛出数据库异常。
         */
        when(bookRepository.save(book))
                .thenThrow(originalException);

        // Service 应把数据库异常转换为更明确的业务冲突异常。
        ConflictException actualException = assertThrows(
                ConflictException.class,
                () -> bookService.create(book)
        );

        /*
         * 验证 ConflictException 保留了原始异常。
         * 出现线上问题时，可以通过异常调用链找到数据库原因。
         */
        assertSame(
                originalException,
                actualException.getCause()
        );
    }

    /*
     * 创建一份符合 BookService 校验规则的图书。
     * 后面的测试也可以重复使用这个辅助方法。
     */
    private Book createValidBook() {
        Book book = new Book();
        book.setTitle("Java 测试");
        book.setAuthor("测试作者");
        book.setIsbn("9780000000001");
        book.setPrice(new BigDecimal("99.00"));
        book.setStock(10);
        book.setStatus(ON_SALE);
        return book;
    }
}