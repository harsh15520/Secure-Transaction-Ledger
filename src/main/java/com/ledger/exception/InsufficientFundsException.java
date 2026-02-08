package com.ledger.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException(Long accountId, Double balance, Double amount) {
        super(String.format("Insufficient funds in account %d. Current balance: %.2f, Required: %.2f", 
                accountId, balance, amount));
    }
}
