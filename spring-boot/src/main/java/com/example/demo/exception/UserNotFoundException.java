package com.example.demo.exception;

/**
 * 用戶不存在異常
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
