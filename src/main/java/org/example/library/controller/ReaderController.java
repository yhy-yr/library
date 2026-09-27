package org.example.library.controller;

import jakarta.validation.Valid;
import org.example.library.dto.CreateReaderRequest;
import org.example.library.dto.UpdateReaderRequest;
import org.example.library.entity.Reader;
import org.example.library.entity.ReaderStatus;
import org.example.library.service.ReaderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody CreateReaderRequest request
    ) {
        Reader reader = new Reader();

        // 只从请求中取客户端允许提供的字段。
        reader.setName(request.name());
        reader.setPhone(request.phone());

        // 新注册的读者由服务器决定初始状态。
        reader.setStatus(ReaderStatus.ACTIVE);

        readerService.create(reader);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam ReaderStatus status
    ) {
        readerService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Reader> getById(@PathVariable Long id) {
        Optional<Reader> reader = readerService.getById(id);

        if (reader.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(reader.get());
    }
    @GetMapping
    public List<Reader> getAll() {
        return readerService.getAll();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReaderRequest request
    ) {
        Reader reader = new Reader();
        reader.setName(request.name());
        reader.setPhone(request.phone());

        // 不设置 status；普通资料修改不能改变读者状态。
        int affectedRows = readerService.update(id, reader);

        if (affectedRows == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

}