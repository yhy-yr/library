package org.example.library.dto;

public record BorrowRequest(
        Long readerId,
        Long bookId
) {
}