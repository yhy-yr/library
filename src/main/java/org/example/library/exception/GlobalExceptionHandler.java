package org.example.library.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 对所有 Controller 统一处理异常。
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 这个方法专门处理 IllegalArgumentException。
    @ExceptionHandler (IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        // 返回 HTTP 400，并把异常消息放进响应正文。
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }
    // 处理 ISBN 等唯一字段重复。
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKeyException(
            DuplicateKeyException exception
    ) {
        // 409 表示请求与数据库当前数据发生冲突。
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("ISBN 已存在");
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflictException(
            ConflictException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }
}