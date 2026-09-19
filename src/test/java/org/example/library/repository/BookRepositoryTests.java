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

}