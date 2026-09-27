package org.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record UpdateReaderRequest(

        /*
         * 姓名不能是 null、空字符串或只有空格。
         * 最大长度应与 reader.name 的 VARCHAR 长度一致。
         */
        @NotBlank(message = "读者姓名不能为空")
        @Size(
                max = 50,
                message = "读者姓名不能超过 50 个字符"
        )
        String name,

        /*
         * 手机号不能为空。
         * 当前只校验是否为空和数据库允许的最大长度；
         * 暂时不假设号码一定是中国大陆 11 位手机号。
         */
        @NotBlank(message = "手机号不能为空")
        @Size(
                max = 20,
                message = "手机号不能超过 20 个字符"
        )
        String phone
) {
}