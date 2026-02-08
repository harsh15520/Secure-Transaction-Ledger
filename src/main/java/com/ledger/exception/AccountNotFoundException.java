package com.ledger.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
    
    public AccountNotFoundException(Long accountId) {
        super("Account with ID " + accountId + " not found");
    }
}
