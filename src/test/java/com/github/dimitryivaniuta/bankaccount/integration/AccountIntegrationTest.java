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
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatList;

/**
 * Integration tests for AccountService, exercising the full Spring Boot context,
 * Flyway migrations, and a Testcontainers–managed PostgreSQL database.
 */
// 1. Bootstraps a full Spring ApplicationContext for integration testing:
//    This gives you all beans (services, repos, Flyway migration, Testcontainers JDBC driver, etc.)
//    without listening on HTTP ports.
@SpringBootTest(
        // - classes = BankAccountApplication.class tells Spring Boot which @SpringBootApplication to start.
        classes = BankAccountApplication.class,
        // - webEnvironment = NONE prevents starting an embedded web server.
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
// 2. Activate the "test" profile so that application-test.yml is loaded,
//    pointing the datasource to the jdbc:tc:postgresql URL (Testcontainers).
@ActiveProfiles("test")
// 3. Use a single TestInstance per class (instead of the default per-method).
//    This allows @BeforeAll and @AfterAll to be non-static if needed,
//    and can improve performance by reusing the same test instance.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountIntegrationTest {

    // 4. Inject the real AccountService bean from the Spring context.
    //    This service is wired to the Testcontainers PostgreSQL instance,
    //    Flyway has already run migrations, and TransactionManager is active.
    @Autowired
    private AccountService accountService;

    // 5. Inject the JPA repository for Account.
    //    Enables direct database assertions and manual cleanup.
    @Autowired
    private AccountRepository accountRepository;

    // 6. Inject the JPA repository for Operation.
    @Autowired
    private OperationRepository operationRepository;

    /**
     * 7. Runs **before each** @Test method:
     *    Clean out all data to ensure isolation.
     *    - Deletes **all** Operation rows.
     *    - Deletes **all** Account rows.
     *    This ensures **complete isolation**: each test starts with an empty schema.
     */
    @BeforeEach
    void cleanDatabase() {
        operationRepository.deleteAll();
        accountRepository.deleteAll();
    }

    /**
     * 8. A smoke test to verify that the Spring context loads successfully
     *    and that the key beans (service + repositories) are present.
     *    If this fails, none of the subsequent tests will run.
     */
    @Test
    @DisplayName("Context loads with AccountService and repositories")
    void contextLoads() {
        assertThat(accountService).isNotNull();
        assertThat(accountRepository).isNotNull();
        assertThat(operationRepository).isNotNull();
    }

    /**
     * 9. Tests the **createAccount** workflow end-to-end:
     *    - accountService.createAccount(initialBalance) writes a row to `account`
     *      and, because initialBalance > 0, writes a `DEPOSIT` row to `operation`.
     *    - We then assert:
     *      * The returned Account has a generated ID and correct balance.
     *      * The Operation table contains exactly one row with type=DEPOSIT,
     *        amount=initialBalance, balanceAfter=initialBalance.
     */
    @Test
    @DisplayName("Create account with initial balance")
    void createAccountWithInitialBalance() {
        Account acct = accountService.createAccount(new BigDecimal("123.45"));

        // Verify account row. Account persisted
        assertThat(acct.getId()).isNotNull();
        assertThat(acct.getBalance()).isEqualByComparingTo("123.45");

        // Verify exactly one deposit operation was recorded. One operation recorded
        List<Operation> ops = operationRepository.findByAccountIdOrderByOperationDateAsc(acct.getId());
        assertThatList(ops)
                .hasSize(1)
                .allSatisfy(op -> {
                    assertThat(op.getType()).isEqualTo(OperationType.DEPOSIT);
                    assertThat(op.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(123.45));
                    assertThat(op.getBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(123.45));
                });
    }

    /**
     * 10. Tests deposit(), withdraw(), and getStatement():
     *     - Creates an empty account
     *     - Calls deposit(200), withdraw(50)
     *     - Verifies final balance = 150
     *     - Verifies getStatement returns two chronologically-ordered operations
     *       with correct types, amounts, and balances.
     */
    @Test
    @DisplayName("Deposit, withdraw, and verify statement")
    void depositWithdrawAndStatement() {
        // Given an empty account
        Account acct = accountService.createAccount(BigDecimal.ZERO);

        // When: perform domain operations
        accountService.deposit(acct.getId(), new BigDecimal("200.00"));
        accountService.withdraw(acct.getId(), new BigDecimal("50.00"));

        // Then: final balance check
        Account updated = accountRepository.findById(acct.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("150.00");

        // And: statement correctness
        List<Operation> ops = accountService.getStatement(acct.getId());
        assertThat(ops).hasSize(2);

        // First operation: deposit
        Operation first = ops.getFirst();
        assertThat(first.getType()).isEqualTo(OperationType.DEPOSIT);
        assertThat(first.getAmount()).isEqualByComparingTo("200.00");
        assertThat(first.getBalanceAfter()).isEqualByComparingTo("200.00");

        // Second operation: withdrawal
        Operation second = ops.get(1);
        assertThat(second.getType()).isEqualTo(OperationType.WITHDRAWAL);
        assertThat(second.getAmount()).isEqualByComparingTo("-50.00");
        assertThat(second.getBalanceAfter()).isEqualByComparingTo("150.00");
    }

    /**
     * 11. Tests updateAccountBalance(...) which applies a **delta**:
     *     - Starting from 100, update to 180 -> should deposit 80
     *     - Then update to 140 -> should withdraw 40
     *     Verifies both the returned balance and the appended Operation entries.
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
     * 12. Tests deleteAccount(id):
     *     - Deletes all Operation rows for that account
     *     - Deletes the Account row itself
     *     - Verifies neither account nor operations remain.
     */
    @Test
    @DisplayName("Delete account and its operations")
    void deleteAccountAndOperations() {
        // Given an account with two operations
        Account acct = accountService.createAccount(new BigDecimal("50.00"));
        accountService.deposit(acct.getId(), new BigDecimal("25.00"));
        List<Operation> before = accountService.getStatement(acct.getId());
        assertThat(before).hasSize(2);

        // When: delete
        accountService.deleteAccount(acct.getId());

        // Then: neither account nor operations exist
        assertThat(accountRepository.findById(acct.getId()))
                .isEmpty();
        // And no operations remain
        assertThat(operationRepository.findByAccountIdOrderByOperationDateAsc(acct.getId()))
                .isEmpty();
    }
}
