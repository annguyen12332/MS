package com.nute.training.exception;

/**
 * Exception: ResourceNotFoundException
 * Ném ra khi không tìm thấy tài nguyên (User, Class, Enrollment, v.v.)
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("Không tìm thấy %s với ID: %d", resourceName, id));
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
