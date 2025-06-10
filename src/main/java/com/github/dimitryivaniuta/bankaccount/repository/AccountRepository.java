package com.github.dimitryivaniuta.bankaccount.repository;

import com.github.dimitryivaniuta.bankaccount.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link Account} entities.
 * <p>
 * Provides CRUD operations and pagination/sorting capabilities for bank accounts
 * through Spring Data JPA. No additional methods are required as basic
 * persistence and retrieval operations are inherited.
 * </p>
 *
 * @see Account
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Inherits:
    // - save(Account entity)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // - and more CRUD and paging operations
}
