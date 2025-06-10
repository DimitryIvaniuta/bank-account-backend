package com.github.dimitryivaniuta.bankaccount.integration;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import com.github.dimitryivaniuta.bankaccount.repository.AccountRepository;
import com.github.dimitryivaniuta.bankaccount.service.AccountService;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountIntegrationTest {
    @Autowired
    AccountService service;
    @Autowired
    AccountRepository accountRepo;

    private Long accountId;

    @BeforeEach
    void init() {
        var acct = accountRepo.save(new Account());
        accountId = acct.getId();
    }

    @Test
    void fullFlow_depositWithdrawStatement() {
        service.deposit(accountId, BigDecimal.valueOf(200));
        service.withdraw(accountId, BigDecimal.valueOf(50));
        var ops = service.getStatement(accountId);
        assertThat(ops).hasSize(2);
        assertThat(ops.get(1).getBalanceAfter()).isEqualByComparingTo("150.00");
    }
}