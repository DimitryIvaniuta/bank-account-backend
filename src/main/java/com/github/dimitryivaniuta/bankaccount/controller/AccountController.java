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

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {
    private final AccountService accountService;


    /** CREATE */
    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @RequestBody @Valid CreateAccountRequest req
    ) {
        var acct = accountService.createAccount(req.getInitialBalance());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(acct.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(new AccountResponse(acct.getId(), acct.getBalance(), acct.getCreatedAt()));
    }

    /** READ all */
    @GetMapping
    public List<AccountResponse> listAll() {
        return accountService.getAllAccounts().stream()
                .map(a -> new AccountResponse(a.getId(), a.getBalance(), a.getCreatedAt()))
                .toList();
    }

    /** READ one */
    @GetMapping("/{id}")
    public AccountResponse getOne(@PathVariable Long id) {
        var a = accountService.getAccount(id);
        return new AccountResponse(a.getId(), a.getBalance(), a.getCreatedAt());
    }

    /** UPDATE balance exactly */
    @PutMapping("/{id}")
    public AccountResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateAccountRequest req
    ) {
        var a = accountService.updateAccountBalance(id, req.getNewBalance());
        return new AccountResponse(a.getId(), a.getBalance(), a.getCreatedAt());
    }

    /** DELETE */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable Long id,
            @RequestBody @Valid DepositRequest req
    ) {
        accountService.deposit(id, req.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long id,
            @RequestBody @Valid WithdrawRequest req
    ) {
        accountService.withdraw(id, req.getAmount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<List<StatementResponse>> statement(@PathVariable Long id) {
        var ops = accountService.getStatement(id);
        var resp = ops.stream()
                .map(op -> new StatementResponse(
                        op.getOperationDate(),
                        op.getType().name(),
                        op.getAmount(),
                        op.getBalanceAfter()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }
}
