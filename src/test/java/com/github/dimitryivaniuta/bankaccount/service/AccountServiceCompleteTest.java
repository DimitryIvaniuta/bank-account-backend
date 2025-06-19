package com.github.dimitryivaniuta.bankaccount.service;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.model.MoneyValue;
import com.github.dimitryivaniuta.bankaccount.model.Operation;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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


        @Test
        @DisplayName("zero initial balance")
        void createAccountWithZeroInitialBalance() {
            // Arrange: save returns an Account with zero balance
            Account saved = new Account();
            saved.setId(1L);
            saved.setBalance(MoneyValue.zero());
            when(accountRepo.save(any(Account.class))).thenReturn(saved);

            // Act: create with 0 EUR
            MonetaryAmount initial = Money.of(BigDecimal.ZERO, "EUR");
            Account result = accountService.createAccount(initial);

            // Assert: no operations saved
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(initial);
            verify(operationRepo, never()).save(any(Operation.class));
        }


        @Test
        @DisplayName("positive initial balance")
        void createAccountWithPositiveInitialBalance() {
            // Arrange: initial save and subsequent find
            Account initialSave = new Account(); initialSave.setId(2L); initialSave.setBalance(MoneyValue.zero());
            Account afterDeposit = new Account(); afterDeposit.setId(2L);
            afterDeposit.setBalance(MoneyValue.from(Money.of(new BigDecimal("150.00"), "EUR")));
            when(accountRepo.save(any(Account.class))).thenReturn(initialSave);
            when(accountRepo.findById(2L))
                    .thenReturn(Optional.of(initialSave))
                    .thenReturn(Optional.of(afterDeposit));

            // Act
            MonetaryAmount depositAmt = Money.of(new BigDecimal("150.00"), "EUR");
            Account result = accountService.createAccount(depositAmt);

            // Assert: one DEPOSIT operation with correct values
            assertThat(result.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(depositAmt);
            verify(operationRepo, times(1)).save(argThat(op ->
                    op.getType() == OperationType.DEPOSIT &&
                            op.getFunds().toMonetaryAmount().isEqualTo(depositAmt) &&
                            op.getBalanceAfter().toMonetaryAmount().isEqualTo(depositAmt)
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
            Account acct = new Account(); acct.setId(3L);
            acct.setBalance(MoneyValue.from(Money.of(new BigDecimal("20.00"), "EUR")));
            when(accountRepo.findById(3L)).thenReturn(Optional.of(acct));

            // Act & Assert
            assertThat(accountService.getAccount(3L)).isSameAs(acct);
        }


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


        @Test
        @DisplayName("list all accounts")
        void listAllAccounts() {
            // Arrange
            Account a1 = new Account(); a1.setId(4L); a1.setBalance(MoneyValue.zero());
            Account a2 = new Account(); a2.setId(5L);
            a2.setBalance(MoneyValue.from(Money.of(BigDecimal.ONE, "EUR")));
            when(accountRepo.findAll()).thenReturn(List.of(a1, a2));

            // Act
            List<Account> result = accountService.getAllAccounts();

            // Assert
            assertThat(result).containsExactly(a1, a2);
        }
    }

    @Nested
    @DisplayName("Balance Update via Delta")
    class UpdateBalanceTests {


        @Test
        @DisplayName("increase balance")
        void increaseBalance() {
            Account acct = new Account(); acct.setId(6L);
            acct.setBalance(MoneyValue.from(Money.of(new BigDecimal("100.00"), "EUR")));
            when(accountRepo.findById(6L)).thenReturn(Optional.of(acct));

            // Act
            MonetaryAmount target = Money.of(new BigDecimal("140.00"), "EUR");
            Account updated = accountService.updateAccountBalance(6L, target);

            // Assert
            assertThat(updated.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(target);
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.DEPOSIT &&
                            op.getFunds().toMonetaryAmount().isEqualTo(
                                    Money.of(new BigDecimal("40.00"), "EUR"))
            ));
        }

        @Test
        @DisplayName("decrease balance")
        void decreaseBalance() {
            Account acct = new Account(); acct.setId(7L);
            acct.setBalance(MoneyValue.from(Money.of(new BigDecimal("200.00"), "EUR")));
            when(accountRepo.findById(7L)).thenReturn(Optional.of(acct));

            MonetaryAmount target = Money.of(new BigDecimal("180.00"), "EUR");
            Account updated = accountService.updateAccountBalance(7L, target);

            assertThat(updated.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(target);
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.WITHDRAWAL &&
                            op.getFunds().toMonetaryAmount().isEqualTo(
                                    Money.of(new BigDecimal("-20.00"), "EUR"))
            ));
        }


        @Test
        @DisplayName("no change when balances equal")
        void noBalanceChange() {
            Account acct = new Account(); acct.setId(8L);
            acct.setBalance(MoneyValue.from(Money.of(new BigDecimal("50.00"), "EUR")));
            when(accountRepo.findById(8L)).thenReturn(Optional.of(acct));

            MonetaryAmount target = Money.of(new BigDecimal("50.00"), "EUR");
            Account updated = accountService.updateAccountBalance(8L, target);

            assertThat(updated.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(target);
            verify(operationRepo, never()).save(any(Operation.class));
        }
    }

    @Nested
    @DisplayName("Deposit & Withdrawal Operations")
    class DepositWithdrawTests {


        @Test
        @DisplayName("deposit operation")
        void depositShouldWork() {
            Account acct = new Account(); acct.setId(9L);
            acct.setBalance(MoneyValue.zero());
            when(accountRepo.findById(9L)).thenReturn(Optional.of(acct));

            MonetaryAmount depositAmt = Money.of(new BigDecimal("25.00"), "EUR");
            accountService.deposit(9L, depositAmt);

            assertThat(acct.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(depositAmt);
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.DEPOSIT &&
                            op.getFunds().toMonetaryAmount().isEqualTo(depositAmt)
            ));
        }


        @Test
        @DisplayName("withdraw operation")
        void withdrawShouldWork() {
            // Arrange
            Account acct = new Account(); acct.setId(10L);
            acct.setBalance(MoneyValue.from(Money.of(new BigDecimal("100.00"), "EUR")));
            when(accountRepo.findById(10L)).thenReturn(Optional.of(acct));

            // Act
            MonetaryAmount withdrawAmt = Money.of(new BigDecimal("40.00"), "EUR");
            accountService.withdraw(10L, withdrawAmt);

            // Assert
            assertThat(acct.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(Money.of(new BigDecimal("60.00"), "EUR"));
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.WITHDRAWAL &&
                            op.getFunds().toMonetaryAmount().isEqualTo(
                                    Money.of(new BigDecimal("-40.00"), "EUR"))
            ));
        }


        @Test
        @DisplayName("insufficient funds")
        void withdrawInsufficientFunds() {
            // Arrange
            Account acct = new Account(); acct.setId(11L);
            acct.setBalance(MoneyValue.zero());
            when(accountRepo.findById(11L)).thenReturn(Optional.of(acct));

            // Act & Assert
            MonetaryAmount ten = Money.of(BigDecimal.TEN, "EUR");
            assertThatThrownBy(() -> accountService.withdraw(11L, ten))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("Account Deletion")
    class DeleteAccountTests {


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

        @Test
        @DisplayName("fetch statement")
        void fetchStatement() {
            // Arrange
            Operation o1 = new Operation(null, OperationType.DEPOSIT,
                    MoneyValue.from(Money.of(new BigDecimal("10.00"), "EUR")),
                    Instant.now(),
                    MoneyValue.from(Money.of(new BigDecimal("10.00"), "EUR"))
            );
            Operation o2 = new Operation(null, OperationType.WITHDRAWAL,
                    MoneyValue.from(Money.of(new BigDecimal("-5.00"), "EUR")),
                    Instant.now(),
                    MoneyValue.from(Money.of(new BigDecimal("5.00"), "EUR"))
            );
            when(operationRepo.findByAccountIdOrderByOperationDateAsc(13L))
                    .thenReturn(List.of(o1, o2));
            // Act
            List<Operation> result = accountService.getStatement(13L);

            // Assert
            assertThat(result).containsExactly(o1, o2);
        }
    }
}
