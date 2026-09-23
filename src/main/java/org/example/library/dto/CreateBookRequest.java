package org.example.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * 创建图书接口的请求 DTO。
 *
 * 它只描述“客户端创建图书时可以提交什么”，
 * 不包含数据库 ID、创建时间和更新时间。
 */
public record CreateBookRequest(

        /*
         * @NotBlank 同时拒绝：
         * null、空字符串 "" 和只有空格的字符串 "   "。
         *
         * @Size 与数据库 VARCHAR(100) 保持一致。
         */
        @NotBlank(message = "书名不能为空")
        @Size(max = 100, message = "书名不能超过 100 个字符")
        String title,

        // 与数据库 author VARCHAR(50) 保持一致。
        @NotBlank(message = "作者不能为空")
        @Size(max = 50, message = "作者不能超过 50 个字符")
        String author,

        // 与数据库 isbn VARCHAR(20) 保持一致。
        @NotBlank(message = "ISBN 不能为空")
        @Size(max = 20, message = "ISBN 不能超过 20 个字符")
        String isbn,

        /*
         * @NotNull：价格必须提供。
         * @DecimalMin：价格不能小于 0。
         */
        @NotNull(message = "价格不能为空")
        @DecimalMin(
                value = "0.00",
                message = "价格不能为负数"
        )
        BigDecimal price,

        /*
         * 库存必须提供，并且不能小于 0。
         */
        @NotNull(message = "库存不能为空")
        @Min(value = 0, message = "库存不能为负数")
        Integer stock,

        /*
         * 出版日期暂时允许为空，
         * 因此这里没有添加 @NotNull。
         */
        LocalDate publishedDate
) {
}