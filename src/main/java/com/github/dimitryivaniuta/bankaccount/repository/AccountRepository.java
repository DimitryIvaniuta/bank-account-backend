package com.github.dimitryivaniuta.bankaccount.repository;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {}
