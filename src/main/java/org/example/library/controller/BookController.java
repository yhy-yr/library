package org.example.library.controller;

import org.example.library.entity.Book;
import org.example.library.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAll() {
        return bookService.getAll();
    }
    @PostMapping
    public int create(@RequestBody Book book) {
        return bookService.create(book);
    }
    @GetMapping("/{id}")
    public Optional<Book> getById(@PathVariable Long id) {
        return bookService.getById(id);
    }
    @PutMapping("/{id}")
    public int update(
            @PathVariable Long id,
            @RequestBody Book book
    ) {
        return bookService.update(id, book);
    }







}
