package com.github.dimitryivaniuta.bankaccount.service;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.model.Operation;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AccountService}, covering:
 * <ul>
 *   <li>Account creation (with zero or positive initial balance)</li>
 *   <li>Account retrieval (single and all)</li>
 *   <li>Balance adjustment via deposit/withdrawal delta</li>
 *   <li>Deposit and withdrawal operations</li>
 *   <li>Account deletion</li>
 *   <li>Statement retrieval</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceCompleteTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private OperationRepository operationRepo;

    @InjectMocks
    private AccountService accountService;

    @Nested
    @DisplayName("Account Creation")
    class CreateAccountTests {

        /**
         * Creating an account with zero initial balance:
         * - Should call AccountRepository.save once.
         * - Should not record any Operation.
         */
        @Test
        @DisplayName("zero initial balance")
        void createAccountWithZeroInitialBalance() {
            // Arrange
            Account saved = new Account(1L, BigDecimal.ZERO, Instant.now());
            when(accountRepo.save(any(Account.class))).thenReturn(saved);

            // Act
            Account result = accountService.createAccount(BigDecimal.ZERO);

            // Assert
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getBalance()).isEqualByComparingTo("0.00");
            verify(accountRepo, times(1)).save(any(Account.class));
            verify(operationRepo, never()).save(any(Operation.class));
        }

        /**
         * Creating an account with a positive initial balance:
         * - Should save the Account (balance zero).
         * - Should perform one deposit operation of that amount.
         * - Final Account returned should have the correct balance.
         */
        @Test
        @DisplayName("positive initial balance")
        void createAccountWithPositiveInitialBalance() {
            // Arrange
            // 1) Initial save returns ID 2, balance 0
            Account savedInitial = new Account(2L, BigDecimal.ZERO, Instant.now());
            // 2) After deposit, repository.findById should first return savedInitial (balance 0)
            //    then return the Account with updated balance 150.00
            Account afterDeposit = new Account(2L, new BigDecimal("150.00"), Instant.now());

            when(accountRepo.save(any(Account.class)))
                    .thenReturn(savedInitial);
            when(accountRepo.findById(2L))
                    .thenReturn(Optional.of(savedInitial))
                    .thenReturn(Optional.of(afterDeposit));

            // Act
            Account result = accountService.createAccount(new BigDecimal("150.00"));

            // Assert
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getBalance()).isEqualByComparingTo("150.00");

            // Verify one deposit operation recorded
            verify(operationRepo, times(1)).save(argThat(op ->
                    op.getType() == OperationType.DEPOSIT
                            && op.getAmount().compareTo(new BigDecimal("150.00")) == 0
                            && op.getBalanceAfter().compareTo(new BigDecimal("150.00")) == 0
            ));
        }
    }

    @Nested
    @DisplayName("Account Retrieval")
    class ReadAccountTests {

        /**
         * Retrieving an existing account by ID should return that Account.
         */
        @Test
        @DisplayName("existing account")
        void getAccountExists() {
            // Arrange
            Account acct = new Account(3L, new BigDecimal("20.00"), Instant.now());
            when(accountRepo.findById(3L)).thenReturn(Optional.of(acct));

            // Act & Assert
            assertThat(accountService.getAccount(3L)).isSameAs(acct);
        }

        /**
         * Retrieving a non-existent account should throw IllegalArgumentException.
         */
        @Test
        @DisplayName("non-existent account")
        void getAccountNotFound() {
            // Arrange
            when(accountRepo.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.getAccount(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account not found");
        }

        /**
         * Retrieving all accounts should return the full list from the repository.
         */
        @Test
        @DisplayName("list all accounts")
        void listAllAccounts() {
            // Arrange
            List<Account> list = List.of(
                    new Account(4L, BigDecimal.ZERO, Instant.now()),
                    new Account(5L, BigDecimal.ONE, Instant.now())
            );
            when(accountRepo.findAll()).thenReturn(list);

            // Act
            List<Account> result = accountService.getAllAccounts();

            // Assert
            assertThat(result).containsExactlyElementsOf(list);
        }
    }

    @Nested
    @DisplayName("Balance Update via Delta")
    class UpdateBalanceTests {

        /**
         * Increasing balance should result in a deposit operation of the difference.
         */
        @Test
        @DisplayName("increase balance")
        void increaseBalance() {
            // Arrange
            Account acct = new Account(6L, new BigDecimal("100.00"), Instant.now());
            when(accountRepo.findById(6L)).thenReturn(Optional.of(acct));

            // Act
            Account updated = accountService.updateAccountBalance(6L, new BigDecimal("140.00"));

            // Assert
            assertThat(updated.getBalance()).isEqualByComparingTo("140.00");
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.DEPOSIT
                            && op.getAmount().compareTo(new BigDecimal("40.00")) == 0
            ));
        }

        /**
         * Decreasing balance should result in a withdrawal operation of the difference.
         */
        @Test
        @DisplayName("decrease balance")
        void decreaseBalance() {
            // Arrange
            Account acct = new Account(7L, new BigDecimal("200.00"), Instant.now());
            when(accountRepo.findById(7L)).thenReturn(Optional.of(acct));

            // Act
            Account updated = accountService.updateAccountBalance(7L, new BigDecimal("180.00"));

            // Assert
            assertThat(updated.getBalance()).isEqualByComparingTo("180.00");
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.WITHDRAWAL
                            && op.getAmount().compareTo(new BigDecimal("-20.00")) == 0
            ));
        }

        /**
         * Updating to the same balance should record no operation.
         */
        @Test
        @DisplayName("no change when balances equal")
        void noBalanceChange() {
            // Arrange
            Account acct = new Account(8L, new BigDecimal("50.00"), Instant.now());
            when(accountRepo.findById(8L)).thenReturn(Optional.of(acct));

            // Act
            Account updated = accountService.updateAccountBalance(8L, new BigDecimal("50.00"));

            // Assert
            assertThat(updated.getBalance()).isEqualByComparingTo("50.00");
            verify(operationRepo, never()).save(any(Operation.class));
        }
    }

    @Nested
    @DisplayName("Deposit & Withdrawal Operations")
    class DepositWithdrawTests {

        /**
         * Deposit should increase the balance and record a DEPOSIT operation.
         */
        @Test
        @DisplayName("deposit operation")
        void depositShouldWork() {
            // Arrange
            Account acct = new Account(9L, BigDecimal.ZERO, Instant.now());
            when(accountRepo.findById(9L)).thenReturn(Optional.of(acct));

            // Act
            accountService.deposit(9L, new BigDecimal("25.00"));

            // Assert
            assertThat(acct.getBalance()).isEqualByComparingTo("25.00");
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.DEPOSIT
                            && op.getAmount().compareTo(new BigDecimal("25.00")) == 0
            ));
        }

        /**
         * Withdrawal should decrease the balance and record a WITHDRAWAL operation.
         */
        @Test
        @DisplayName("withdraw operation")
        void withdrawShouldWork() {
            // Arrange
            Account acct = new Account(10L, new BigDecimal("100.00"), Instant.now());
            when(accountRepo.findById(10L)).thenReturn(Optional.of(acct));

            // Act
            accountService.withdraw(10L, new BigDecimal("40.00"));

            // Assert
            assertThat(acct.getBalance()).isEqualByComparingTo("60.00");
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.WITHDRAWAL
                            && op.getAmount().compareTo(new BigDecimal("-40.00")) == 0
            ));
        }

        /**
         * Withdrawal with insufficient funds should throw IllegalStateException.
         */
        @Test
        @DisplayName("insufficient funds")
        void withdrawInsufficientFunds() {
            // Arrange
            Account acct = new Account(11L, BigDecimal.ZERO, Instant.now());
            when(accountRepo.findById(11L)).thenReturn(Optional.of(acct));

            // Act & Assert
            assertThatThrownBy(() -> accountService.withdraw(11L, BigDecimal.TEN))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("Account Deletion")
    class DeleteAccountTests {

        /**
         * Deleting an account should delete its operations and then the account itself.
         */
        @Test
        @DisplayName("delete account and operations")
        void deleteAccount() {
            // Arrange
            doNothing().when(operationRepo).deleteAllByAccountId(12L);
            doNothing().when(accountRepo).deleteById(12L);

            // Act
            accountService.deleteAccount(12L);

            // Assert
            verify(operationRepo).deleteAllByAccountId(12L);
            verify(accountRepo).deleteById(12L);
        }
    }

    @Nested
    @DisplayName("Statement Retrieval")
    class StatementTests {

        /**
         * Retrieving a statement should return the ordered list of operations.
         */
        @Test
        @DisplayName("fetch statement")
        void fetchStatement() {
            // Arrange
            List<Operation> ops = List.of(
                    new Operation(1L, null, OperationType.DEPOSIT, new BigDecimal("10.00"), Instant.now(), new BigDecimal("10.00")),
                    new Operation(2L, null, OperationType.WITHDRAWAL, new BigDecimal("-5.00"), Instant.now(), new BigDecimal("5.00"))
            );
            when(operationRepo.findByAccountIdOrderByOperationDateAsc(13L)).thenReturn(ops);

            // Act
            List<Operation> result = accountService.getStatement(13L);

            // Assert
            assertThat(result).containsExactlyElementsOf(ops);
        }
    }
}
