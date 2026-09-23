package org.example.library.repository;

import org.example.library.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.example.library.entity.BookStatus.OFF_SALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.example.library.entity.BookStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Transactional
public class BookRepositoryTests{
    @Autowired
    private BookRepository bookRepository;
    @Test
 void shouldSaveBook(){
        Book book = new Book();
        book.setTitle("Java 核心技术");
        book.setAuthor("Cay S. Horstmann");
        book.setIsbn("9787300000001");
        book.setPrice(new BigDecimal("99.00"));
        book.setPublishedDate(LocalDate.of(2026, 9, 14));
        // 库存字段不能为空，准备一个合法库存
        book.setStock(0);

// 新书保存时必须明确指定初始状态
        book.setStatus(BookStatus.ON_SALE);

        int affectedRows = bookRepository.save(book);

        assertEquals(1,affectedRows);
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    void shouldFindBookById() {
        jdbcTemplate.update("""
        INSERT INTO book
            (title, author, isbn, price, stock, status,
             published_date, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """,
                "Java 核心技术",
                "Cay S. Horstmann",
                "9787300000002",
                new BigDecimal("99.00"),
                10,
                "ON_SALE",
                LocalDate.of(2026, 9, 14)
        );

        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM book WHERE isbn = ?",
                Long.class,
                "9787300000002"
        );
        Optional<Book> result = bookRepository.findById(id);
        assertTrue(result.isPresent());
        assertEquals("Java 核心技术", result.get().getTitle());
        assertEquals("9787300000002", result.get().getIsbn());
        assertEquals(BookStatus.ON_SALE, result.get().getStatus());


    }
    @Test
    void shouldReturnEmptyWhenBookDoesNotExist() {
        Optional<Book> result = bookRepository.findById(Long.MAX_VALUE);

        assertTrue(result.isEmpty());
    }
    @Test
    void shouldUpdateBookStatus() {
        String isbn = "TEST-STATUS-001";

        jdbcTemplate.update("""
        INSERT INTO book
            (title, author, isbn, price, stock, status,
             published_date, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """,
                "测试图书",
                "测试作者",
                isbn,
                new BigDecimal("10.00"),
                1,
                "ON_SALE",
                LocalDate.of(2026, 9, 19)
        );

        Long bookId = jdbcTemplate.queryForObject(
                "SELECT id FROM book WHERE isbn = ?",
                Long.class,
                isbn
        );

        int affectedRows =
                bookRepository.updateStatus(bookId, OFF_SALE);

        String actualStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM book WHERE id = ?",
                String.class,
                bookId
        );

        assertEquals(1, affectedRows);
        assertEquals(OFF_SALE.name(), actualStatus);
    }
    @Test
    void shouldNotReduceStockBelowZero() {
        String isbn = "TEST-STOCK-ZERO-01";

        /*
         * 实验条件：
         * 在真实测试数据库中创建一本库存为 0 的图书。
         */
        jdbcTemplate.update("""
        INSERT INTO book
            (title, author, isbn, price, stock, status,
             published_date, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """,
                "零库存测试图书",
                "测试作者",
                isbn,
                new BigDecimal("10.00"),
                0,
                "ON_SALE",
                LocalDate.of(2026, 9, 22)
        );

        Long bookId = jdbcTemplate.queryForObject(
                "SELECT id FROM book WHERE isbn = ?",
                Long.class,
                isbn
        );

        /*
         * 实验操作：
         * 尝试从零库存中再扣减一本。
         */
        int affectedRows = bookRepository.changeStock(
                bookId,
                -1
        );

        /*
         * 再次读取真实数据库，观察最终库存。
         */
        Integer actualStock = jdbcTemplate.queryForObject(
                "SELECT stock FROM book WHERE id = ?",
                Integer.class,
                bookId
        );

        /*
         * 两个独立证据：
         * 1. SQL 没有更新任何记录；
         * 2. 数据库中的库存仍然是 0。
         */
        assertEquals(0, affectedRows);
        assertEquals(0, actualStock);
    }
    @Test
    void shouldPreserveStatusWhenUpdatingBookDetails() {
        String isbn = "TEST-PRESERVE-01";

        /*
         * 先在真实数据库中创建一本已经下架的图书。
         */
        jdbcTemplate.update("""
        INSERT INTO book
            (title, author, isbn, price, stock, status,
             published_date, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """,
                "修改前书名",
                "测试作者",
                isbn,
                new BigDecimal("10.00"),
                1,
                "OFF_SALE",
                LocalDate.of(2026, 9, 22)
        );

        Long bookId = jdbcTemplate.queryForObject(
                "SELECT id FROM book WHERE isbn = ?",
                Long.class,
                isbn
        );

        /*
         * 准备普通资料更新。
         *
         * 故意在对象中放入 ON_SALE，
         * 用来证明 Repository.update() 不会更新 status 列。
         */
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle("修改后书名");
        updatedBook.setAuthor("修改后作者");
        updatedBook.setIsbn(isbn);
        updatedBook.setPrice(new BigDecimal("20.00"));
        updatedBook.setStock(2);
        updatedBook.setStatus(BookStatus.ON_SALE);
        updatedBook.setPublishedDate(
                LocalDate.of(2026, 9, 23)
        );

        int affectedRows =
                bookRepository.update(updatedBook);

        String actualTitle = jdbcTemplate.queryForObject(
                "SELECT title FROM book WHERE id = ?",
                String.class,
                bookId
        );

        String actualStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM book WHERE id = ?",
                String.class,
                bookId
        );

        // 普通资料应该更新成功。
        assertEquals(1, affectedRows);
        assertEquals("修改后书名", actualTitle);

        // 原来的下架状态必须保持不变。
        assertEquals(
                BookStatus.OFF_SALE.name(),
                actualStatus
        );
    }


}