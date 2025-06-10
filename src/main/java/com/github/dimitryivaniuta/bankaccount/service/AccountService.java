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

/**
 * Service layer providing transactional operations for bank accounts.
 * <p>
 * Encapsulates business logic for creating, retrieving, updating,
 * and deleting accounts, as well as performing deposit and withdrawal
 * transactions and fetching account statements.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    /**
     * Repository for CRUD operations on {@link Account} entities.
     */
    private final AccountRepository accountRepo;

    /**
     * Repository for CRUD operations on {@link Operation} entities.
     */
    private final OperationRepository operationRepo;

    /**
     * Creates a new account with an optional initial deposit.
     * <p>
     * Initializes the account with zero balance, persists it, and
     * applies a deposit if {@code initialBalance} is greater than zero.
     * </p>
     *
     * @param initialBalance the starting balance for the account; must be ≥ 0
     * @return the persisted {@link Account} with updated balance and ID
     */
    @Transactional
    public Account createAccount(final BigDecimal initialBalance) {
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

    /**
     * Retrieves an existing account by its identifier.
     *
     * @param id the unique identifier of the account
     * @return the found {@link Account}
     * @throws IllegalArgumentException if no account exists with the given ID
     */
    @Transactional(readOnly = true)
    public Account getAccount(final Long id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    /**
     * Retrieves all accounts in the system.
     *
     * @return a list of all {@link Account} entities
     */
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    /**
     * Updates the account balance to the specified amount.
     * <p>
     * Calculates the difference between {@code newBalance} and current balance,
     * and performs a deposit or withdrawal operation accordingly to record the change.
     * </p>
     *
     * @param accountId  the ID of the account to update
     * @param newBalance the desired balance; must be ≥ 0
     * @return the {@link Account} after balance adjustment
     * @throws IllegalArgumentException if the account does not exist
     * @throws IllegalStateException    if attempting to withdraw more than available
     */
    @Transactional
    public Account updateAccountBalance(final Long accountId, final BigDecimal newBalance) {
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
     * Deletes the account and all associated operations.
     *
     * @param id the ID of the account to delete
     */
    @Transactional
    public void deleteAccount(final Long id) {
        operationRepo.deleteAllByAccountId(id);
        accountRepo.deleteById(id);
    }

    /**
     * Deposits a specified amount into an account.
     * <p>
     * Increases the account balance and records a deposit {@link Operation} entry.
     * </p>
     *
     * @param accountId the ID of the account to debit
     * @param amount    the amount to deposit; must be > 0
     * @throws IllegalArgumentException if the account does not exist
     */
    @Transactional
    public void deposit(final Long accountId, final BigDecimal amount) {
        Account acct = getAccount(accountId);
        BigDecimal newBal = acct.getBalance().add(amount);
        acct.setBalance(newBal);
        acct = accountRepo.save(acct);

        operationRepo.save(new Operation(
                null, acct, OperationType.DEPOSIT, amount, Instant.now(), newBal
        ));
    }

    /**
     * Withdraws a specified amount from an account.
     * <p>
     * Decreases the account balance if sufficient funds are available and
     * records a withdrawal {@link Operation} entry.
     * </p>
     *
     * @param accountId the ID of the account to credit
     * @param amount    the amount to withdraw; must be > 0
     * @throws IllegalArgumentException if the account does not exist
     * @throws IllegalStateException    if insufficient funds are available
     */
    @Transactional
    public void withdraw(final Long accountId, final BigDecimal amount) {
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
     * Retrieves the chronological list of operations for an account.
     *
     * @param accountId the ID of the account whose statement to fetch
     * @return a list of {@link Operation} sorted by operation date ascending
     */
    @Transactional(readOnly = true)
    public List<Operation> getStatement(final Long accountId) {
        return operationRepo.findByAccountIdOrderByOperationDateAsc(accountId);
    }
}
