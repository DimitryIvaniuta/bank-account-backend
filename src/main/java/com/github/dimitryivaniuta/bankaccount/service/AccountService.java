package com.github.dimitryivaniuta.bankaccount.service;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.model.MoneyValue;
import com.github.dimitryivaniuta.bankaccount.model.Operation;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
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
     * Provider for exchange rates (ECB) to perform currency conversions.
     */
    private final ExchangeRateProvider rateProvider =
            MonetaryConversions.getExchangeRateProvider("ECB");

    /**
     * Creates a new account with an optional initial deposit.
     * <p>
     * Initializes balance to zero in default currency (EUR). Applies a deposit
     * for any positive initial amount, converting currencies if necessary.
     * </p>
     *
     * @param initialAmount the initial deposit amount (can be zero or in any currency)
     * @return the persisted {@link Account} with its generated ID and balance
     */
    @Transactional
    public Account createAccount(final MonetaryAmount initialAmount) {
        Account acct = new Account();
        acct.setBalance(MoneyValue.zero());
        acct.setCreatedAt(Instant.now());
        acct = accountRepo.save(acct);

        if (initialAmount.isPositive()) {
            deposit(acct.getId(), initialAmount);
            acct = accountRepo.findById(acct.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found after create"));
        }
        return acct;
    }

    /**
     * Retrieves an existing account by its ID.
     *
     * @param id the unique ID of the account
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
     * Updates account balance to an exact target amount.
     * <p>
     * Computes the delta from current balance and applies deposit or withdrawal
     * so the change is recorded as an operation.
     * </p>
     *
     * @param accountId    the ID of the account
     * @param targetAmount the desired balance as {@link MonetaryAmount}
     * @return the updated {@link Account}
     */
    @Transactional
    public Account updateAccountBalance(final Long accountId, final MonetaryAmount targetAmount) {
        Account acct = getAccount(accountId);
        MonetaryAmount current = acct.getBalance().toMonetaryAmount();
        CurrencyUnit acctCcy = current.getCurrency();

        // Convert target to account currency if needed
        MonetaryAmount desired = targetAmount;
        if (!desired.getCurrency().equals(acctCcy)) {
            CurrencyConversion conversion = rateProvider.getCurrencyConversion(acctCcy);
            desired = desired.with(conversion);
        }

        // Compute delta
        MonetaryAmount delta = desired.subtract(current);
        if (delta.isPositive()) {
            deposit(accountId, delta);
        } else if (delta.isNegative()) {
            withdraw(accountId, delta.negate());
        }

        return accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
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
     * Deposits a monetary amount into an account, converting currency if needed.
     * Records a DEPOSIT operation.
     *
     * @param accountId   the account to credit
     * @param incomingAmt the amount to deposit (in any currency)
     */
    @Transactional
    public void deposit(final Long accountId, final MonetaryAmount incomingAmt) {
        Account account = getAccount(accountId);
        MonetaryAmount current = account.getBalance().toMonetaryAmount();
        CurrencyUnit acctCcy = current.getCurrency();

        // Convert incomingAmt to account currency
        MonetaryAmount toDeposit = incomingAmt;
        if (!incomingAmt.getCurrency().equals(acctCcy)) {
            CurrencyConversion conversion = rateProvider.getCurrencyConversion(acctCcy);
            toDeposit = incomingAmt.with(conversion);
        }

        // Update balance
        MonetaryAmount updated = current.add(toDeposit);
        account.setBalance(MoneyValue.from(updated));
        accountRepo.save(account);

        // Record operation
        Operation op = new Operation(
                account,
                OperationType.DEPOSIT,
                MoneyValue.from(toDeposit),
                Instant.now(),
                MoneyValue.from(updated)
        );
        operationRepo.save(op);
    }

    /**
     * Withdraws a monetary amount from an account, converting currency if needed.
     * Records a WITHDRAWAL operation. Throws if insufficient funds.
     *
     * @param accountId   the account to debit
     * @param incomingAmt the amount to withdraw (in any currency)
     * @throws IllegalStateException if balance < withdrawal amount
     */
    @Transactional
    public void withdraw(final Long accountId, final MonetaryAmount incomingAmt) {
        Account account = getAccount(accountId);
        MonetaryAmount current = account.getBalance().toMonetaryAmount();
        CurrencyUnit acctCcy = current.getCurrency();

        MonetaryAmount toWithdraw = incomingAmt;
        if (!incomingAmt.getCurrency().equals(acctCcy)) {
            CurrencyConversion conversion = rateProvider.getCurrencyConversion(acctCcy);
            toWithdraw = incomingAmt.with(conversion);
        }

        if (current.isLessThan(toWithdraw)) {
            throw new IllegalStateException("Insufficient funds");
        }

        // Update balance
        MonetaryAmount updated = current.subtract(toWithdraw);
        account.setBalance(MoneyValue.from(updated));
        accountRepo.save(account);

        // Record operation with negative amount
        Operation op = new Operation(
                account,
                OperationType.WITHDRAWAL,
                MoneyValue.from(toWithdraw.negate()),
                Instant.now(),
                MoneyValue.from(updated)
        );
        operationRepo.save(op);
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
