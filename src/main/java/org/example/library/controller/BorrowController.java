package org.example.library.controller;

import org.example.library.dto.BorrowDetailResponse;
import org.example.library.dto.BorrowRequest;
import org.example.library.entity.BorrowRecord;
import org.example.library.entity.ReaderStatus;
import org.example.library.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrows")

public class BorrowController {
    private final BorrowService borrowService;
    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }
    @PostMapping
    public ResponseEntity<Void> borrow(
            @RequestBody BorrowRequest request
    ){

                borrowService.borrow(request);
        return ResponseEntity. status(HttpStatus.CREATED).build();

    }
    @PatchMapping("/{borrowId}/return")
    public ResponseEntity<Void> returnBook(
            @PathVariable Long borrowId
    ) {
        borrowService.returnBook(borrowId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public List<BorrowRecord> getByReaderId(
            @RequestParam Long readerId
    ) {
        return borrowService.getByReaderId(readerId);
    }
    @GetMapping("/overdue")
    public List<BorrowRecord> getOverdueRecords() {
        return borrowService.getOverdueRecords();
    }
    @GetMapping("/details")
    public List<BorrowDetailResponse> getDetailsByReaderId(
            @RequestParam Long readerId
    ) {
        return borrowService.getDetailsByReaderId(readerId);
    }

}
