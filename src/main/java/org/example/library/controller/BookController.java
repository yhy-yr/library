package org.example.library.controller;

import org.example.library.entity.Book;
import org.example.library.entity.BookStatus;
import org.example.library.service.BookService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        Optional<Book> book = bookService.getById(id);

        if (book.isPresent()) {
            return ResponseEntity.ok(book.get());
        }

        return ResponseEntity.notFound().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody Book book
    ) {
        // 执行修改，并取得受影响的记录数量。
        int affectedRows = bookService.update(id, book);

        // 返回 0 表示数据库中不存在该 ID。
        if (affectedRows == 0) {
            return ResponseEntity.notFound().build();
        }

        // 修改成功，但不需要返回完整响应体。
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        // Service 返回受影响行数：1 表示删除成功，0 表示 ID 不存在。
        int affectedRows = bookService.delete(id);

        // 没有删除任何记录，说明图书不存在。
        if (affectedRows == 0) {
            return ResponseEntity.notFound().build();
        }

        // 删除成功。204 表示操作成功，但不需要返回响应数据。
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public List<Book> searchByTitle(
            @RequestParam String keyword
    ) {
        return bookService.searchByTitle(keyword);
    }
    @GetMapping("/status")
    public List<Book> getByStatus(
            @RequestParam BookStatus status
    ) {
        return bookService.getByStatus(status);
    }
    @GetMapping("/page")
    public List<Book> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getPage(page, size);
    }
    @PatchMapping("/{id}/stock")
    public int changeStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {
        return bookService.changeStock(id, quantity);
    }







}
