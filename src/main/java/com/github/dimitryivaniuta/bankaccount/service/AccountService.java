package com.github.dimitryivaniuta.bankaccount.service;

import com.github.dimitryivaniuta.bankaccount.model.Account;
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

    /**
     * Create a new account, optionally with an initial deposit.
     */
    @Transactional
    public Account createAccount(BigDecimal initialBalance) {
        var acct = new Account();
        acct.setBalance(BigDecimal.ZERO);
        acct.setCreatedAt(Instant.now());
        acct = accountRepo.save(acct);
        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            deposit(acct.getId(), initialBalance);
            acct = accountRepo.findById(acct.getId()).orElseThrow();
        }
        return acct;
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    /**
     * READ all
     */
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    /**
     * Adjust balance to exactly newBalance.
     * Internally routes through deposit/withdraw to record an operation.
     */
    @Transactional
    public Account updateAccountBalance(Long accountId, BigDecimal newBalance) {
        Account acct = getAccount(accountId);
        BigDecimal oldBal = acct.getBalance();
        BigDecimal delta = newBalance.subtract(oldBal);

        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            deposit(accountId, delta);
        } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
            withdraw(accountId, delta.abs());
        }
        return accountRepo.findById(accountId).orElseThrow();
    }

    /**
     * Delete an account and all its operations.
     */
    @Transactional
    public void deleteAccount(Long id) {
        operationRepo.deleteAllByAccountId(id);
        accountRepo.deleteById(id);
    }

    /**
     * Deposit money
     */
    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        Account acct = getAccount(accountId);
        BigDecimal newBal = acct.getBalance().add(amount);
        acct.setBalance(newBal);
        acct = accountRepo.save(acct);

        operationRepo.save(new Operation(
                null, acct, OperationType.DEPOSIT, amount, Instant.now(), newBal
        ));
    }

    /**
     * Withdraw money
     */
    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        Account acct = getAccount(accountId);
        if (acct.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        BigDecimal newBal = acct.getBalance().subtract(amount);
        acct.setBalance(newBal);
        acct = accountRepo.save(acct);

        operationRepo.save(new Operation(
                null, acct, OperationType.WITHDRAWAL, amount.negate(), Instant.now(), newBal
        ));
    }

    /**
     * Statement history
     */
    @Transactional(readOnly = true)
    public List<Operation> getStatement(Long accountId) {
        return operationRepo.findByAccountIdOrderByOperationDateAsc(accountId);
    }
}