package org.example.library.repository;

import org.example.library.entity.Reader;
import org.example.library.entity.ReaderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReaderRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReaderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private Reader mapRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        Reader reader = new Reader();

        reader.setId(resultSet.getLong("id"));
        reader.setName(resultSet.getString("name"));
        reader.setPhone(resultSet.getString("phone"));

        reader.setStatus(
                ReaderStatus.valueOf(
                        resultSet.getString("status")
                )
        );

        reader.setCreatedAt(
                resultSet.getObject(
                        "created_at",
                        LocalDateTime.class
                )
        );

        reader.setUpdatedAt(
                resultSet.getObject(
                        "updated_at",
                        LocalDateTime.class
                )
        );

        return reader;
    }
    public int save(Reader reader) {
        String sql = """
            INSERT INTO reader(
                name,
                phone,
                status,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, NOW(), NOW())
            """;

        return jdbcTemplate.update(
                sql,
                reader.getName(),
                reader.getPhone(),
                reader.getStatus().name()
        );
    }
    public Optional<Reader> findById(Long id) {
        String sql = """
            SELECT id,
                   name,
                   phone,
                   status,
                   created_at,
                   updated_at
            FROM reader
            WHERE id = ?
            """;

        List<Reader> readers = jdbcTemplate.query(
                sql,
                this::mapRow,
                id
        );

        return readers.stream().findFirst();
    }
}