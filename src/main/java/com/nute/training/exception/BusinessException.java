package com.nute.training.exception;

/**
 * Exception: BusinessException
 * Ném ra khi vi phạm business rules (lớp đầy, đã đăng ký, v.v.)
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
