package org.example.library.repository;

import org.example.library.dto.BorrowDetailResponse;
import org.example.library.entity.BorrowRecord;
import org.example.library.entity.BorrowStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BorrowRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public BorrowRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }
    private BorrowRecord mapRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        return new BorrowRecord(
                resultSet.getLong("id"),
                resultSet.getLong("reader_id"),
                resultSet.getLong("book_id"),
                resultSet.getObject(
                        "borrowed_at",
                        LocalDateTime.class
                ),
                resultSet.getObject(
                        "due_at",
                        LocalDateTime.class
                ),
                resultSet.getObject(
                        "returned_at",
                        LocalDateTime.class
                ),
                BorrowStatus.valueOf(
                        resultSet.getString("status")
                ),
                resultSet.getObject(
                        "created_at",
                        LocalDateTime.class
                ),
                resultSet.getObject(
                        "updated_at",
                        LocalDateTime.class
                )
        );
    }

    public int save(Long readerId, Long bookId) {
        String sql = """
                INSERT INTO borrow_record(
                    reader_id,
                    book_id,
                    borrowed_at,
                    due_at,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (
                    ?, ?,
                    NOW(),
                    DATE_ADD(NOW(), INTERVAL  30 DAY),
                    ?,
                    NOW(),
                    NOW()
                )
                """;

        return jdbcTemplate.update(
                sql,
                readerId,
                bookId,
                BorrowStatus.BORROWED.name()
        );
    }
    public Optional<BorrowRecord> findById(Long id) {
        String sql = """
            SELECT id,
                   reader_id,
                   book_id,
                   borrowed_at,
                   due_at,
                   returned_at,
                   status,
                   created_at,
                   updated_at
            FROM borrow_record
            WHERE id = ?
            """;

        List<BorrowRecord> records = jdbcTemplate.query(
                sql,
                this::mapRow,
                id
        );

        return records.stream().findFirst();
    }
    public int markReturned(Long id) {
        String sql = """
            UPDATE borrow_record
            SET status = ?,
                returned_at = NOW(),
                updated_at = NOW()
            WHERE id = ?
              AND status = ?
            """;

        return jdbcTemplate.update(
                sql,
                BorrowStatus.RETURNED.name(),
                id,
                BorrowStatus.BORROWED.name()
        );
    }
    public List<BorrowRecord> findByReaderId(Long readerId) {
        String sql = """
        SELECT id,
               reader_id,
               book_id,
               borrowed_at,
               due_at,
               returned_at,
               status,
               created_at,
               updated_at
        FROM borrow_record
        WHERE reader_id = ?
        ORDER BY borrowed_at DESC
        """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                readerId
        );
    }
    public long countBorrowedByReaderId(Long readerId) {
        String sql = """
        SELECT count(*)
        FROM borrow_record
        WHERE reader_id = ?
          AND status = ?
        """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                readerId,
                BorrowStatus.BORROWED.name()
        );

        return count;
    }
    public boolean existsBorrowedRecord(
            Long readerId,
            Long bookId
    ) {
        String sql = """
        SELECT COUNT(*)
        FROM borrow_record
        WHERE reader_id = ?
          AND book_id = ?
          AND status = ?
        """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                readerId,
                bookId,
                BorrowStatus.BORROWED.name()
        );

        return count > 0;
    }
    public List<BorrowRecord> findOverdueRecords() {
        String sql = """
        SELECT id,
               reader_id,
               book_id,
               borrowed_at,
               due_at,
               returned_at,
               status,
               created_at,
               updated_at
        FROM borrow_record
        WHERE status = ?
          AND due_at < NOW()
        ORDER BY  due_at ASC
        """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                BorrowStatus.BORROWED.name()
        );
    }
    private BorrowDetailResponse mapDetailRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        return new BorrowDetailResponse(
                resultSet.getLong("id"),
                resultSet.getLong("reader_id"),
                resultSet.getString("reader_name"),
                resultSet.getLong("book_id"),
                resultSet.getString("book_title"),
                resultSet.getObject("borrowed_at", LocalDateTime.class),
                resultSet.getObject("due_at", LocalDateTime.class),
                resultSet.getObject("returned_at", LocalDateTime.class),
                BorrowStatus.valueOf(
                        resultSet.getString("status")
                )
        );
    }
    public List<BorrowDetailResponse> findDetailsByReaderId(
            Long readerId
    ) {
        String sql = """
        SELECT br.id,
               br.reader_id,
               r.name AS reader_name,
               br.book_id,
               b.title AS book_title,
               br.borrowed_at,
               br.due_at,
               br.returned_at,
               br.status
        FROM borrow_record br
        JOIN reader r ON br.reader_id = r.id
        JOIN book b ON br.book_id = b.id
        WHERE br.reader_id = ?
        ORDER BY br.borrowed_at DESC
        """;

        return jdbcTemplate.query(
                sql,
                this::mapDetailRow,
                readerId
        );
    }



}