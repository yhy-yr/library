package org.example.library.repository;

import org.example.library.entity.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.example.library.entity.BookStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public class BookRepository{
    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public int save(Book book){
        String sql = """
    INSERT  INTO book(title, author, isbn, price, stock, status,
     published_date, created_at, updated_at )
     VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())""";
        return jdbcTemplate.update(sql, book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getStock(),
                book.getStatus().name(),
                book.getPublishedDate());
    }
    private Book mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Book book = new Book();

        book.setId(resultSet.getLong("id"));
        book.setTitle(resultSet.getString("title"));
        book.setAuthor(resultSet.getString("author"));
        book.setIsbn(resultSet.getString("isbn"));
        book.setPrice(resultSet.getBigDecimal("price"));
        book.setStock(resultSet.getInt("stock"));
        book.setStatus(
                BookStatus.valueOf(resultSet.getString("status"))
        );
        book.setPublishedDate(
                resultSet.getObject("published_date", LocalDate.class)
        );
        book.setCreatedAt(
                resultSet.getObject("created_at", LocalDateTime.class)
        );
        book.setUpdatedAt(
                resultSet.getObject("updated_at", LocalDateTime.class)
        );

        return book;
    }
    public Optional<Book> findById(Long id){
        String sql = """
                SELECT id,title,author,isbn,price,stock,status,published_date,created_at,updated_at
                FROM book
                WHERE id = ?
                """;
        List<Book> books = jdbcTemplate.query(sql,this::mapRow,id);
        return books.stream().findFirst();
    }
    public int update(Book book) {
        String sql = """
            UPDATE book
            SET title = ?,
                author = ?,
                isbn = ?,
                price = ?,
                stock = ?,
                status = ?,
                published_date = ?,
                updated_at = NOW()
            WHERE id = ?
            """;

        return jdbcTemplate.update(
                sql,
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getStock(),
                book.getStatus().name(),
                book.getPublishedDate(),
                book.getId()
        );
    }
    public int deleteById(Long id) {
        String sql = """
            DELETE FROM book
            WHERE id = ?
            """;

        return jdbcTemplate.update(sql, id);
    }
    public List<Book> findAll() {
        String sql = """
            SELECT *
            FROM book
            ORDER BY id DESC
            """;

        return jdbcTemplate.query(sql, this::mapRow);
    }
    public List<Book> findByStatus(BookStatus status) {
        String sql = """
            SELECT *
            FROM book
            WHERE status = ?
            ORDER BY id DESC
            """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                status.name()
        );
    }
    public List<Book> findByTitle(String keyword) {
        String sql = """
            SELECT *
            FROM book
            WHERE title LIKE ?
            ORDER BY id DESC
            """;

        String searchKeyword = "%" + keyword + "%";

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                searchKeyword
        );
    }
    public List<Book> findPage(int page, int size) {
        int offset = (page - 1) * size;

        String sql = """
            SELECT *
            FROM book
            ORDER BY id DESC
            LIMIT ?
            OFFSET ?
            """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                size,
                offset
        );
    }
    public int changeStock(Long id, int quantity) {
        String sql = """
            UPDATE book
            SET stock = stock + ?,
                updated_at = NOW()
            WHERE id = ?
              AND stock + ? >= 0
            """;

        return jdbcTemplate.update(
                sql,
                quantity,
                id,
                quantity
        );
    }
    public long count() {
        String sql = """
            SELECT COUNT(*)
            FROM book
            """;

        Long total = jdbcTemplate.queryForObject(
                sql,
                Long.class
        );

        return total;
    }

}