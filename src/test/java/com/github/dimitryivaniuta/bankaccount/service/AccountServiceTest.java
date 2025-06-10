package com.github.dimitryivaniuta.bankaccount.service;

import static org.assertj.core.api.Assertions.*;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.model.OperationType;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.repository.OperationRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
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
        Mockito.when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
    }

    @Test
    void deposit_shouldIncreaseBalanceAndSaveOperation() {
        service.deposit(1L, BigDecimal.valueOf(100));
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        Mockito.verify(operationRepo).save(Mockito.argThat(op ->
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
}