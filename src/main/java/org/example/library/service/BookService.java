package org.example.library.service;

import org.example.library.entity.Book;
import org.example.library.entity.BookStatus;
import org.example.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
    public List<Book> getByStatus(BookStatus status) {
        return bookRepository.findByStatus(status);
    }
    public List<Book> getPage(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("页码必须从 1 开始");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("每页数量必须在 1 到 100 之间");
        }

        return bookRepository.findPage(page, size);
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