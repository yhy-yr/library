package org.example.library.dto;

import java.util.List;

/**
 * 某位读者的借阅历史分页响应。
 *
 * content：当前页的借阅详情
 * page：当前页码
 * size：每页最多返回多少条
 * total：该读者的全部借阅历史数量
 * totalPages：总页数
 */
public record BorrowPageResponse(
        List<BorrowDetailResponse> content,
        int page,
        int size,
        Long total,
        int totalPages
) {
}