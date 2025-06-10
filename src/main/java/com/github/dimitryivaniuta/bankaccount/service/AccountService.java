package com.github.dimitryivaniuta.bankaccount.service;

import com.github.dimitryivaniuta.bankaccount.model.Operation;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepo;
    private final OperationRepository operationRepo;

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        var account = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        var newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        accountRepo.save(account);
        operationRepo.save(new Operation(
                null, account, OperationType.DEPOSIT, amount, Instant.now(), newBalance
        ));
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        var account = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        var newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepo.save(account);
        operationRepo.save(new Operation(
                null, account, OperationType.WITHDRAWAL, amount.negate(), Instant.now(), newBalance
        ));
    }

    @Transactional(readOnly = true)
    public List<Operation> getStatement(Long accountId) {
        return operationRepo.findByAccountIdOrderByOperationDateAsc(accountId);
    }
}