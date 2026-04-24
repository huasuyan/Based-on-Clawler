package com.crawler.config;

import com.crawler.entity.Result;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;



/**
 * 全局异常处理器
 * 捕获所有Controller抛出的异常，统一返回格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 参数校验失败异常（JSON格式参数）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(MethodArgumentNotValidException e) {
        // 获取第一个错误信息
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数错误";
        return Result.error(message);
    }

    /**
     * 处理表单参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数错误";
        return Result.error(message);
    }

    /**
     * 处理手动抛出的业务异常（比如：账号已存在、验证码错误）
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    // 新增：专门处理权限不足，返回 403
    @ExceptionHandler(com.crawler.exception.PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handlePermissionDenied(com.crawler.exception.PermissionDeniedException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.error("服务器异常，请稍后重试");
    }
}