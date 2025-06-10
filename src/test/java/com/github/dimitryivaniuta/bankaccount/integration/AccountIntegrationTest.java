package com.github.dimitryivaniuta.bankaccount.integration;

import com.github.dimitryivaniuta.bankaccount.BankAccountApplication;
import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.model.Operation;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import com.github.dimitryivaniuta.bankaccount.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AccountService, exercising the full Spring Boot context,
 * Flyway migrations, and a Testcontainers–managed PostgreSQL database.
 */
@SpringBootTest(classes = BankAccountApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OperationRepository operationRepository;

    /**
     * Clean out all data before each test to ensure isolation.
     */
    @BeforeEach
    void cleanDatabase() {
        operationRepository.deleteAll();
        accountRepository.deleteAll();
    }

    /**
     * Verifies that the Spring context loads and key beans are present.
     */
    @Test
    @DisplayName("Context loads with AccountService and repositories")
    void contextLoads() {
        assertThat(accountService).isNotNull();
        assertThat(accountRepository).isNotNull();
        assertThat(operationRepository).isNotNull();
    }

    /**
     * Tests creating an account with an initial balance.
     * Expects that an Account row is inserted and a single DEPOSIT operation is recorded.
     */
    @Test
    @DisplayName("Create account with initial balance")
    void createAccountWithInitialBalance() {
        Account acct = accountService.createAccount(new BigDecimal("123.45"));

        // Account persisted
        assertThat(acct.getId()).isNotNull();
        assertThat(acct.getBalance()).isEqualByComparingTo("123.45");

        // One operation recorded
        List<Operation> ops = operationRepository.findByAccountIdOrderByOperationDateAsc(acct.getId());
        assertThat(ops)
                .hasSize(1)
                .allSatisfy(op -> {
                    assertThat(op.getType()).isEqualTo(OperationType.DEPOSIT);
                    assertThat(op.getAmount()).isEqualByComparingTo("123.45");
                    assertThat(op.getBalanceAfter()).isEqualByComparingTo("123.45");
                });
    }

    /**
     * Tests deposit, withdrawal, and statement retrieval.
     * - Deposit 200.00
     * - Withdraw 50.00
     * - Statement should contain two entries in chronological order.
     */
    @Test
    @DisplayName("Deposit, withdraw, and verify statement")
    void depositWithdrawAndStatement() {
        // Given a new account
        Account acct = accountService.createAccount(BigDecimal.ZERO);

        // When
        accountService.deposit(acct.getId(), new BigDecimal("200.00"));
        accountService.withdraw(acct.getId(), new BigDecimal("50.00"));

        // Then balance is correct
        Account updated = accountRepository.findById(acct.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("150.00");

        // And statement has two ops
        List<Operation> ops = accountService.getStatement(acct.getId());
        assertThat(ops).hasSize(2);

        Operation first = ops.get(0);
        assertThat(first.getType()).isEqualTo(OperationType.DEPOSIT);
        assertThat(first.getAmount()).isEqualByComparingTo("200.00");
        assertThat(first.getBalanceAfter()).isEqualByComparingTo("200.00");

        Operation second = ops.get(1);
        assertThat(second.getType()).isEqualTo(OperationType.WITHDRAWAL);
        assertThat(second.getAmount()).isEqualByComparingTo("-50.00");
        assertThat(second.getBalanceAfter()).isEqualByComparingTo("150.00");
    }

    /**
     * Tests updating the account to an exact new balance via delta logic.
     * Expects that the service computes the correct delta and records it.
     */
    @Test
    @DisplayName("Update account balance via delta (deposit/withdraw)")
    void updateAccountBalanceViaDelta() {
        Account acct = accountService.createAccount(new BigDecimal("100.00"));

        // Increase to 180.00
        Account up1 = accountService.updateAccountBalance(acct.getId(), new BigDecimal("180.00"));
        assertThat(up1.getBalance()).isEqualByComparingTo("180.00");
        List<Operation> ops1 = accountService.getStatement(acct.getId());
        assertThat(ops1).hasSize(2);  // initial deposit + this deposit
        assertThat(ops1.get(1).getAmount()).isEqualByComparingTo("80.00");
        assertThat(ops1.get(1).getType()).isEqualTo(OperationType.DEPOSIT);

        // Decrease to 140.00
        Account up2 = accountService.updateAccountBalance(acct.getId(), new BigDecimal("140.00"));
        assertThat(up2.getBalance()).isEqualByComparingTo("140.00");
        List<Operation> ops2 = accountService.getStatement(acct.getId());
        assertThat(ops2).hasSize(3);
        assertThat(ops2.get(2).getAmount()).isEqualByComparingTo("-40.00");
        assertThat(ops2.get(2).getType()).isEqualTo(OperationType.WITHDRAWAL);
    }

    /**
     * Tests deleting an account: operations should be cascaded/deleted first, then the account.
     */
    @Test
    @DisplayName("Delete account and its operations")
    void deleteAccountAndOperations() {
        // Given an account with two operations
        Account acct = accountService.createAccount(new BigDecimal("50.00"));
        accountService.deposit(acct.getId(), new BigDecimal("25.00"));
        List<Operation> before = accountService.getStatement(acct.getId());
        assertThat(before).hasSize(2);

        // When
        accountService.deleteAccount(acct.getId());

        // Then account gone
        assertThat(accountRepository.findById(acct.getId())).isEmpty();
        // And no operations remain
        assertThat(operationRepository.findByAccountIdOrderByOperationDateAsc(acct.getId())).isEmpty();
    }
}
