package org.example.library.entity;

import java.time.LocalDateTime;

import static org.example.library.entity.BorrowStatus.BORROWED;

public record BorrowRecord(
        Long id,
        Long readerId,
        Long bookId,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        LocalDateTime returnedAt,
        BorrowStatus status ,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}