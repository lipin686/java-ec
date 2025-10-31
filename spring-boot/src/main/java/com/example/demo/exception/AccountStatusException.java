package com.example.demo.exception;

/**
 * 帳戶狀態異常
 * 用於處理帳戶被停用、鎖定、過期等狀態問題
 */
public class AccountStatusException extends RuntimeException {
    public AccountStatusException(String message) {
        super(message);
    }
}
