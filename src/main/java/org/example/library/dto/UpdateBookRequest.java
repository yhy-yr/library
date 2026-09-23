package org.example.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * 修改图书时客户端可以提交的数据。
 *
 * 不包含：
 * - id：由 URL 决定
 * - status：由专门的状态接口修改
 * - createdAt、updatedAt：由服务器和数据库维护
 */
public record UpdateBookRequest(

        @NotBlank(message = "书名不能为空")
        @Size(
                max = 100,
                message = "书名不能超过 100 个字符"
        )
        String title,

        @NotBlank(message = "作者不能为空")
        @Size(
                max = 50,
                message = "作者不能超过 50 个字符"
        )
        String author,

        @NotBlank(message = "ISBN 不能为空")
        @Size(
                max = 20,
                message = "ISBN 不能超过 20 个字符"
        )
        String isbn,

        @NotNull(message = "价格不能为空")
        @DecimalMin(
                value = "0.00",
                message = "价格不能为负数"
        )
        BigDecimal price,

        @NotNull(message = "库存不能为空")
        @Min(
                value = 0,
                message = "库存不能为负数"
        )
        Integer stock,

        LocalDate publishedDate
) {
}