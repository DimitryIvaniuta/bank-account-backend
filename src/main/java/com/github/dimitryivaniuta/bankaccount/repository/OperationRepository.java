package com.github.dimitryivaniuta.bankaccount.repository;

import com.github.dimitryivaniuta.bankaccount.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for {@link Operation} entities.
 * <p>
 * Extends Spring Data JPA's {@link JpaRepository} to provide standard CRUD
 * operations, plus custom queries for retrieving and deleting operations
 * by account ID.
 * </p>
 *
 * @see Operation
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
public interface OperationRepository extends JpaRepository<Operation, Long> {

    /**
     * Retrieves all operations associated with the given account,
     * ordered chronologically by the operation date (oldest first).
     *
     * @param accountId the unique identifier of the account
     * @return a {@link List} of {@link Operation} sorted by {@code operationDate}
     */
    List<Operation> findByAccountIdOrderByOperationDateAsc(Long accountId);

    /**
     * Deletes all operations for the specified account.
     * <p>
     * Used to clean up operation history when an account is deleted.
     * </p>
     *
     * @param accountId the unique identifier of the account
     */
    void deleteAllByAccountId(Long accountId);
}
