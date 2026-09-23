package org.example.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BorrowRequest(

        @NotNull(message = "读者 ID 不能为空")
        @Positive(message = "读者 ID 必须大于 0")
                Long readerId,

        @NotNull(message = "图书 ID 不能为空")
        @Positive(message = "图书 ID 必须大于 0")
        Long bookId
) {
}
