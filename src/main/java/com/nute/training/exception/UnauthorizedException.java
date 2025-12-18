package com.nute.training.exception;

/**
 * Exception: UnauthorizedException
 * Ném ra khi người dùng chưa đăng nhập hoặc session hết hạn
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.");
    }
}
