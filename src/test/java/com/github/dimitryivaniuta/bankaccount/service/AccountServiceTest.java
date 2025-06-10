package com.github.dimitryivaniuta.bankaccount.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

class AccountServiceTest {
    @Mock
    AccountRepository accountRepo;
    @Mock
    OperationRepository operationRepo;

    @InjectMocks
    AccountService service;

    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        account = new Account(1L, BigDecimal.ZERO, null);
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
    }

    @Test
    void deposit_shouldIncreaseBalanceAndSaveOperation() {
        service.deposit(1L, BigDecimal.valueOf(100));
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        verify(operationRepo).save(argThat(op ->
                op.getType() == OperationType.DEPOSIT &&
                        op.getAmount().compareTo(BigDecimal.valueOf(100)) == 0 &&
                        op.getBalanceAfter().compareTo(BigDecimal.valueOf(100)) == 0
        ));
    }

    @Test
    void withdraw_insufficientFunds_throws() {
        assertThatThrownBy(() -> service.withdraw(1L, BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient funds");
    }


    @Test
    void updateAccountBalance_createsCorrectOperations() {
        Account acct = new Account(1L, new BigDecimal("100.00"), Instant.now());
        when(accountRepo.findById(1L)).thenReturn(Optional.of(acct));

        // increase balance to 150
        service.updateAccountBalance(1L, new BigDecimal("150.00"));
        verify(operationRepo).save(argThat(op ->
                op.getType() == OperationType.DEPOSIT &&
                        op.getAmount().compareTo(new BigDecimal("50.00")) == 0
        ));

        // now decrease to 120
        acct.setBalance(new BigDecimal("150.00"));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(acct));
        service.updateAccountBalance(1L, new BigDecimal("120.00"));
        verify(operationRepo).save(argThat(op ->
                op.getType() == OperationType.WITHDRAWAL &&
                        op.getAmount().compareTo(new BigDecimal("-30.00")) == 0
        ));
    }
}