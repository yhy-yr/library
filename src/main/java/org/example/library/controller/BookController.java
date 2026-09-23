package org.example.library.controller;

import jakarta.validation.Valid;
import org.example.library.dto.BookPageResponse;
import org.example.library.dto.CreateBookRequest;
import org.example.library.dto.UpdateBookRequest;
import org.example.library.entity.Book;
import org.example.library.entity.BookStatus;
import org.example.library.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    @GetMapping("/count")
    public long count() {
        // 返回 book 表中的记录总数。
        return bookService.count();
    }

    @GetMapping
    public List<Book> getAll() {
        return bookService.getAll();
    }
    @PostMapping
    public ResponseEntity<Void> create(
            /*
             * @Valid 告诉 Spring：
             * 在进入方法之前，执行 CreateBookRequest 上的校验注解。
             */
            @Valid @RequestBody CreateBookRequest request
    ) {
        /*
         * DTO 只负责接收客户端数据。
         * 在这里把允许的字段转换成数据库实体。
         */
        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPrice(request.price());
        book.setStock(request.stock());
        book.setPublishedDate(request.publishedDate());

        /*
         * 状态由服务器决定，客户端不能在创建时任意指定。
         */
        book.setStatus(BookStatus.ON_SALE);

        bookService.create(book);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
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

            /*
             * 更新请求同样需要在进入方法体前执行 DTO 校验。
             */
            @Valid  @RequestBody UpdateBookRequest request
    ) {
        /*
         * 把允许更新的字段转换成 Book。
         * 不设置 id：路径 id 会在 Service 中写入。
         * 不设置 status：普通更新不能改变上下架状态。
         */
        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPrice(request.price());
        book.setStock(request.stock());
        book.setPublishedDate(request.publishedDate());

        int affectedRows = bookService.update(
                id,
                book
        );

        // 更新 0 行，表示 URL 中的图书 ID 不存在。
        if (affectedRows == 0) {
            return ResponseEntity.notFound().build();
        }

        // 更新成功且不需要响应正文。
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/available")
    public List<Book> getAvailableBooks() {
        return bookService.getAvailableBooks();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public List<Book> searchByTitle(
            @RequestParam String keyword
    ) {
        return bookService.searchByTitle(keyword);
    }
    @GetMapping("/search/author")
    public List<Book> searchByAuthor(
            @RequestParam String keyword
    ) {
        return bookService.searchByAuthor(keyword);
    }
    @GetMapping("/price-range")
    public List<Book> getByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice
    ) {
        return bookService.getByPriceRange(
                minPrice,
                maxPrice
        );
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam BookStatus status
    ) {
        bookService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/status")
    public List<Book> getByStatus(
            @RequestParam BookStatus status
    ) {
        return bookService.getByStatus(status);
    }
    @GetMapping("/page")
    public BookPageResponse getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getPage(page, size);
    }
    @PatchMapping("/{id}/stock")
    public ResponseEntity<String> changeStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {
        // 执行原子的库存增减。
        int affectedRows = bookService.changeStock(id, quantity);

        // 返回 0 表示 SQL 条件不成立。
        if (affectedRows == 0) {
            return ResponseEntity
                    .badRequest()
                    .body("图书不存在或库存不足");
        }

        // 修改成功，不返回正文。
        return ResponseEntity.noContent().build();
    }






}
