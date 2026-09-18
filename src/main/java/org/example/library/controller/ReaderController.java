package org.example.library.controller;

import org.example.library.entity.Reader;
import org.example.library.service.ReaderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody Reader reader
    ) {
        readerService.create(reader);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}