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
        if (book.getPrice() == null
                || book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("图书价格不能为负数");
        }
        if (book.getStock() == null || book.getStock() < 0) {
            throw new IllegalArgumentException("图书库存不能为负数");
        }

        return bookRepository.save(book);
    }
    public List<Book> getAll() {
        return bookRepository.findAll();
    }
    public int update(Long id, Book book) {
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
}