package org.example.library.service;

import org.example.library.entity.Book;
import org.example.library.entity.BookStatus;
import org.example.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.example.library.dto.BookPageResponse;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    public long count() {
        return bookRepository.count();
    }

    public Optional<Book> getById(Long id) {
        return bookRepository.findById(id);
    }
    public int create(Book book) {
        validateBook(book);

        return bookRepository.save(book);
    }
    public List<Book> getAll() {
        return bookRepository.findAll();
    }
    public int update(Long id, Book book) {
        validateBook(book);
        book.setId(id);

        return bookRepository.update(book);
    }
    public int delete(Long id) {
        return bookRepository.deleteById(id);
    }
    public List<Book> searchByTitle(String keyword) {
        return bookRepository.findByTitle(keyword);
    }
    public List<Book> searchByAuthor(String keyword){
        return bookRepository.findByAuthor(keyword);
    }
    public List<Book> getByStatus(BookStatus status) {
        return bookRepository.findByStatus(status);
    }
    public List<Book> getByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        // 最低价和最高价都必须提供。
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("价格范围不能为空");
        }

        // 两个价格都不能小于 0。
        if (minPrice.compareTo(BigDecimal.ZERO) < 0
                || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格不能为负数");
        }

        // 最低价不能大于最高价。
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("最低价格不能大于最高价格");
        }

        return bookRepository.findByPriceRange(minPrice, maxPrice);
    }
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    public BookPageResponse getPage(int page, int size) {

        // 页码从 1 开始。
        if (page < 1) {
            throw new IllegalArgumentException("页码必须从 1 开始");
        }

        // 限制每页的数据量。
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("每页数量必须在 1 到 100 之间");
        }


        // 第一次查询：取得当前页的数据。
        List<Book> content = bookRepository.findPage(page, size);

        // 第二次查询：取得整张表的总记录数。
        long total = bookRepository.count();
        int totalPages = (int) ((total + size - 1) / size);

        // 把数据、页码、每页数量和总数组合成分页响应。
        return new BookPageResponse(
                content,
                page,
                size,
                total,
                totalPages
        );
    }

    public int changeStock(Long id, int quantity) {
        if (quantity == 0) {
            throw new IllegalArgumentException("库存变化量不能为 0");
        }

        return bookRepository.changeStock(id, quantity);
    }
    /**
     * 检查图书数据是否符合业务规则。
     * private 表示这个方法只在 BookService 内部使用。
     */
    private void validateBook(Book book) {
        // 书名不能是 null、空字符串或只有空格。
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new IllegalArgumentException("书名不能为空");
        }

// 作者不能是 null、空字符串或只有空格。
        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            throw new IllegalArgumentException("作者不能为空");
        }

// ISBN 不能是 null、空字符串或只有空格。
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new IllegalArgumentException("ISBN 不能为空");
        }

// 状态必须存在。
        if (book.getStatus() == null) {
            throw new IllegalArgumentException("图书状态不能为空");
        }

        // 价格必须填写，并且不能小于 0。
        if (book.getPrice() == null
                || book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("图书价格不能为负数");
        }

        // 库存必须填写，并且不能小于 0。
        if (book.getStock() == null || book.getStock() < 0) {
            throw new IllegalArgumentException("图书库存不能为负数");
        }
    }


}