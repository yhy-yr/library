package org.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * 创建读者接口的请求 DTO。
 *
 * 客户端只允许提交姓名和手机号。
 *
 * 不包含：
 * - id：由数据库生成
 * - status：由服务器设置初始状态
 * - createdAt、updatedAt：由数据库维护
 */
public record CreateReaderRequest(

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