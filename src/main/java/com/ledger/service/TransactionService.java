package com.ledger.service;

import com.ledger.exception.AccountNotFoundException;
import com.ledger.exception.InsufficientFundsException;
import com.ledger.model.Account;
import com.ledger.model.Transaction;
import com.ledger.repository.AccountRepository;
import com.ledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    @Transactional
    public Transaction transferFunds(Long fromId, Long toId, Double amount) {
        // Validate amount
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }
        
        // Load accounts with pessimistic locking to ensure consistency
        // This ensures that even if the system crashes mid-way, the transaction
        // will be rolled back and total balance remains constant
        Account fromAccount = accountRepository.findByIdWithLock(fromId)
                .orElseThrow(() -> new AccountNotFoundException(fromId));
        
        Account toAccount = accountRepository.findByIdWithLock(toId)
                .orElseThrow(() -> new AccountNotFoundException(toId));
        
        // Check sufficient funds
        if (fromAccount.getBalance() < amount) {
            throw new InsufficientFundsException(fromId, fromAccount.getBalance(), amount);
        }
        
        // Perform transfer atomically
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        
        // Save accounts
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Create and save transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromId);
        transaction.setToAccount(toId);
        transaction.setAmount(amount);
        
        return transactionRepository.save(transaction);
    }
    
    public Double getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return account.getBalance();
    }
}
