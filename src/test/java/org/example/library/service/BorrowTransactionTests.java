package org.example.library.service;

import org.example.library.dto.BorrowRequest;
import org.example.library.repository.BorrowRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
class BorrowTransactionTests {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 只模拟保存借阅记录；图书的库存更新仍走真实数据库。
    @MockitoBean
    private BorrowRecordRepository borrowRecordRepository;

    @Test
    void shouldRollbackStockWhenSavingBorrowRecordFails() {
        String marker = UUID.randomUUID()
                .toString()
                .substring(0, 12);
        String phone = "TX-" + marker;
        String isbn = "TX-" + marker;

        try {// 这两条数据由本测试创建，最后只清理这两条。
        jdbcTemplate.update(
                """
                INSERT INTO reader
                    (name, phone, status, created_at, updated_at)
                VALUES (?, ?, 'ACTIVE', NOW(), NOW())
                """,
                "事务测试读者",
                phone
        );


        jdbcTemplate.update(
                """
                INSERT INTO book
                    (title, author, isbn, price, stock, status,
                     created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 'ON_SALE', NOW(), NOW())
                """,
                "事务测试图书",
                "测试作者",
                isbn,
                new BigDecimal("10.00"),
                2
        );


            Long readerId = jdbcTemplate.queryForObject(
                    "SELECT id FROM reader WHERE phone = ?",
                    Long.class,
                    phone
            );
            Long bookId = jdbcTemplate.queryForObject(
                    "SELECT id FROM book WHERE isbn = ?",
                    Long.class,
                    isbn
            );

            Integer stockBefore = jdbcTemplate.queryForObject(
                    "SELECT stock FROM book WHERE id = ?",
                    Integer.class,
                    bookId
            );

            /*
             * 让保存借阅记录时报错。
             * 借书代码会先扣库存，之后才调用这个方法。
             */
            doThrow(new IllegalStateException("模拟保存失败"))
                    .when(borrowRecordRepository)
                    .save(readerId, bookId);

            assertThrows(
                    IllegalStateException.class,
                    () -> borrowService.borrow(
                            new BorrowRequest(readerId, bookId)
                    )
            );

            // 确认代码确实走到了“保存借阅记录”这一步。
            verify(borrowRecordRepository)
                    .save(readerId, bookId);

            // borrow() 已结束，再从真实数据库读取库存。
            Integer stockAfter = jdbcTemplate.queryForObject(
                    "SELECT stock FROM book WHERE id = ?",
                    Integer.class,
                    bookId
            );

            // 发生回滚后，库存应该与借书前相同。
            assertEquals(stockBefore, stockAfter);
        } finally {
            // 即使断言失败，也只清理本测试创建的两条数据。
            jdbcTemplate.update(
                    "DELETE FROM reader WHERE phone = ?",
                    phone
            );
            jdbcTemplate.update(
                    "DELETE FROM book WHERE isbn = ?",
                    isbn
            );
        }
    }
}