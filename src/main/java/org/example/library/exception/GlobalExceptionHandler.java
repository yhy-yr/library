package org.example.library.exception;

import org.example.library.dto.ValidationErrorResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

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
                .body("数据已存在");
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
    /*
     * 处理 @Valid 产生的请求字段校验异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse>
    handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        /*
         * LinkedHashMap 会保留添加顺序，
         * 让响应和测试结果更容易阅读。
         */
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        /*
         * 一个请求可能同时包含多个字段错误，
         * 因此遍历所有 FieldError，而不是只取第一个。
         */
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    String message = error.getDefaultMessage();

                    /*
                     * 同一个字段可能同时违反多个注解。
                     * putIfAbsent() 保留该字段遇到的第一条错误。
                     */
                    fieldErrors.putIfAbsent(
                            error.getField(),
                            message != null
                                    ? message
                                    : "参数不合法"
                    );
                });

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        400,
                        "参数校验失败",
                        fieldErrors
                );

        return ResponseEntity
                .badRequest()
                .body(response);
    }


}