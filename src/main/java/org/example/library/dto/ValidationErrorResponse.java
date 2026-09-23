package org.example.library.dto;

import java.util.Map;

/*
 * 参数校验失败时返回给客户端的结构。
 */
public record ValidationErrorResponse(

        // HTTP 状态码，例如 400。
        int status,

        // 这次错误的整体说明。
        String message,

        /*
         * 每个字段对应的错误信息。
         *
         * 例如：
         * title -> 书名不能为空
         * price -> 价格不能为负数
         */
        Map<String, String> fieldErrors
) {
}