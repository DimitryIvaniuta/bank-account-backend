package com.github.dimitryivaniuta.bankaccount.controller;

import com.github.dimitryivaniuta.bankaccount.dto.AccountResponse;
import com.github.dimitryivaniuta.bankaccount.dto.CreateAccountRequest;
import com.github.dimitryivaniuta.bankaccount.dto.DepositRequest;
import com.github.dimitryivaniuta.bankaccount.dto.StatementResponse;
import com.github.dimitryivaniuta.bankaccount.dto.UpdateAccountRequest;
import com.github.dimitryivaniuta.bankaccount.dto.WithdrawRequest;
import com.github.dimitryivaniuta.bankaccount.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller exposing CRUD and domain operations for bank accounts.
 * <p>
 * Supports:
 * <ul>
 *   <li>Create a new account (optionally with initial balance)</li>
 *   <li>Retrieve all accounts or a single account by ID</li>
 *   <li>Update an account’s balance exactly (via deposit/withdrawal delta)</li>
 *   <li>Delete an account (along with its operations)</li>
 *   <li>Deposit and withdraw operations</li>
 *   <li>Fetch account statement (history of operations)</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    /**
     * Service layer for account business logic.
     */
    private final AccountService accountService;

    /**
     * Creates a new bank account, optionally depositing the given initial balance.
     *
     * @param req the request containing the initial balance (must be ≥ 0)
     * @return HTTP 201 Created with Location header pointing to the new resource,
     * and the created {@link AccountResponse} in the body
     */
    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @RequestBody @Valid final CreateAccountRequest req
    ) {
        var acct = accountService.createAccount(req.getInitialBalance());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(acct.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(new AccountResponse(acct.getId(), acct.getBalance(), acct.getCreatedAt()));
    }

    /**
     * Retrieves all existing bank accounts.
     *
     * @return a list of {@link AccountResponse}, each containing ID, balance, and creation timestamp
     */
    @GetMapping
    public List<AccountResponse> listAll() {
        return accountService.getAllAccounts().stream()
                .map(a -> new AccountResponse(a.getId(), a.getBalance(), a.getCreatedAt()))
                .toList();
    }

    /**
     * Retrieves a single bank account by its ID.
     *
     * @param id the unique identifier of the account
     * @return the {@link AccountResponse} for the given account
     * @throws IllegalArgumentException if no account exists with the given ID
     */
    @GetMapping("/{id}")
    public AccountResponse getOne(@PathVariable final Long id) {
        var a = accountService.getAccount(id);
        return new AccountResponse(a.getId(), a.getBalance(), a.getCreatedAt());
    }

    /**
     * Updates an account’s balance to the exact specified amount.
     * <p>
     * Internally computes the delta from the current balance and applies
     * a deposit or withdrawal operation accordingly.
     * </p>
     *
     * @param id  the account ID to update
     * @param req the request containing the new desired balance (must be ≥ 0)
     * @return the updated {@link AccountResponse}
     * @throws IllegalStateException    on insufficient funds when decreasing balance
     * @throws IllegalArgumentException if no account exists with the given ID
     */
    @PutMapping("/{id}")
    public AccountResponse update(
            @PathVariable final Long id,
            @RequestBody @Valid final UpdateAccountRequest req
    ) {
        var a = accountService.updateAccountBalance(id, req.getNewBalance());
        return new AccountResponse(a.getId(), a.getBalance(), a.getCreatedAt());
    }

    /**
     * Deletes an account and all its associated operations.
     *
     * @param id the account ID to delete
     * @return HTTP 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deposits the specified amount into the account.
     *
     * @param id  the account ID to deposit into
     * @param req the request containing the amount to deposit (must be > 0)
     * @return HTTP 200 OK on success
     * @throws IllegalArgumentException if no account exists with the given ID
     */
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable final Long id,
            @RequestBody @Valid final DepositRequest req
    ) {
        accountService.deposit(id, req.getAmount());
        return ResponseEntity.ok().build();
    }

    /**
     * Withdraws the specified amount from the account.
     *
     * @param id  the account ID to withdraw from
     * @param req the request containing the amount to withdraw (must be > 0)
     * @return HTTP 200 OK on success
     * @throws IllegalStateException    if the account has insufficient funds
     * @throws IllegalArgumentException if no account exists with the given ID
     */
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable final Long id,
            @RequestBody @Valid final WithdrawRequest req
    ) {
        accountService.withdraw(id, req.getAmount());
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the statement (operation history) for the account.
     *
     * @param id the account ID whose statement to fetch
     * @return HTTP 200 OK with a list of {@link StatementResponse}, each containing
     * date, type, amount, and resulting balance
     * @throws IllegalArgumentException if no account exists with the given ID
     */
    @GetMapping("/{id}/statement")
    public ResponseEntity<List<StatementResponse>> statement(@PathVariable final Long id) {
        var ops = accountService.getStatement(id);
        var resp = ops.stream()
                .map(op -> new StatementResponse(
                        op.getOperationDate(),
                        op.getType().name(),
                        op.getAmount(),
                        op.getBalanceAfter()
                ))
                .toList();
        return ResponseEntity.ok(resp);
    }
}
