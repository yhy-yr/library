package org.example.library.dto;

import org.example.library.entity.Book;

import java.util.List;

/**
 * 专门表示图书分页查询的响应数据。
 *
 * content：当前页的图书
 * page：当前页码
 * size：每页数量
 * total：数据库中的图书总数
 */
public record BookPageResponse(
        List<Book> content,
        int page,
        int size,
        long total,
        int totalPages
) {
}