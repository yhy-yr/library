package org.example.library.dto;

import org.example.library.entity.BorrowStatus;
import java.time.LocalDateTime;

public record BorrowDetailResponse(
        Long borrowId,
        Long readerId,
        String readerName,
        Long bookId,
        String bookTitle,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        LocalDateTime returnedAt,
        BorrowStatus status
) {
}