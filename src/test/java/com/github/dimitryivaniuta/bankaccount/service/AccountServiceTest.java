package com.github.dimitryivaniuta.bankaccount.service;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.model.MoneyValue;
import com.github.dimitryivaniuta.bankaccount.model.Operation;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AccountService}, updated for multi-currency support
 * using Embedded MoneyValue and JSR-354 MonetaryAmount.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private OperationRepository operationRepo;

    @InjectMocks
    private AccountService service;

    private Account account;

    /**
     * Prepare a default account with zero balance in EUR before each test.
     */
    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setBalance(MoneyValue.zero());
        account.setCreatedAt(Instant.now());
        lenient().when(accountRepo.findById(1L))
                .thenReturn(Optional.of(account));
    }

    @Nested
    @DisplayName("Deposit behavior")
    class DepositTests {

        @Test
        @DisplayName("should increase balance and record operation")
        void deposit_shouldIncreaseBalanceAndSaveOperation() {
            // Act
            MonetaryAmount depositAmt = Money.of(100, "EUR");
            service.deposit(1L, depositAmt);

            // Assert balance updated
            assertThat(account.getBalance().toMonetaryAmount())
                    .isEqualByComparingTo(depositAmt);

            // Assert operation recorded correctly
            verify(operationRepo).save(argThat(new ArgumentMatcher<Operation>() {
                @Override
                public boolean matches(Operation op) {
                    return op.getType() == OperationType.DEPOSIT
                            && op.getFunds().toMonetaryAmount()
                            .isEqualTo(depositAmt)
                            && op.getBalanceAfter().toMonetaryAmount()
                            .isEqualTo(depositAmt);
                }
            }));
        }
    }

    @Nested
    @DisplayName("Withdrawal behavior")
    class WithdrawalTests {

        @Test
        @DisplayName("should throw if insufficient funds")
        void withdraw_insufficientFunds_throws() {
            // Act & Assert
            MonetaryAmount withdrawalAmt = Money.of(10, "EUR");
            assertThatThrownBy(() -> service.withdraw(1L, withdrawalAmt))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient funds");

            // Should not record any operation
            verify(operationRepo, never()).save(org.mockito.ArgumentMatchers.any(Operation.class));
        }
    }

    @Nested
    @DisplayName("Balance Update via Delta logic")
    class UpdateBalanceTests {

        @Test
        @DisplayName("should deposit when target > current")
        void increaseBalance() {
            // Arrange
            Account acct = new Account(); acct.setId(6L);
            acct.setBalance(MoneyValue.from(Money.of(100, "EUR")));
            when(accountRepo.findById(6L)).thenReturn(Optional.of(acct));

            // Act
            MonetaryAmount target = Money.of(140, "EUR");
            service.updateAccountBalance(6L, target);

            // Assert deposit recorded for delta
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.DEPOSIT
                            && op.getFunds().toMonetaryAmount()
                            .isEqualTo(Money.of(40, "EUR"))
            ));
        }

        @Test
        @DisplayName("should withdraw when target < current")
        void decreaseBalance() {
            // Arrange
            Account acct = new Account(); acct.setId(7L);
            acct.setBalance(MoneyValue.from(Money.of(200, "EUR")));
            when(accountRepo.findById(7L)).thenReturn(Optional.of(acct));

            // Act
            MonetaryAmount target = Money.of(180, "EUR");
            service.updateAccountBalance(7L, target);

            // Assert withdrawal recorded for delta
            verify(operationRepo).save(argThat(op ->
                    op.getType() == OperationType.WITHDRAWAL
                            && op.getFunds().toMonetaryAmount()
                            .isEqualTo(Money.of(-20, "EUR"))
            ));
        }

        @Test
        @DisplayName("should do nothing when target == current")
        void noBalanceChange() {
            // Arrange
            Account acct = new Account(); acct.setId(8L);
            acct.setBalance(MoneyValue.from(Money.of(50, "EUR")));
            when(accountRepo.findById(8L)).thenReturn(Optional.of(acct));

            // Act
            MonetaryAmount target = Money.of(50, "EUR");
            service.updateAccountBalance(8L, target);

            // Assert no operation recorded
            verify(operationRepo, never()).save(org.mockito.ArgumentMatchers.any(Operation.class));
        }
    }
}
