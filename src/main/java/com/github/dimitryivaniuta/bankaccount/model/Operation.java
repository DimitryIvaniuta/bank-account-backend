package com.github.dimitryivaniuta.bankaccount.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity representing a single operation (transaction) performed on a bank account.
 * <p>
 * Each operation records the type of transaction (deposit or withdrawal),
 * the amount, the timestamp when it occurred, and the resulting account balance after execution.
 * </p>
 */
@Entity
@Table(name = "operation")
@Getter
@Setter
@NoArgsConstructor
public class Operation {

    /**
     * Unique primary key for the operation.
     * <p>
     * Generated via the database sequence "BA_UNIQUE_ID".
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BA_UNIQUE_ID")
    @SequenceGenerator(
            name = "BA_UNIQUE_ID",
            sequenceName = "BA_UNIQUE_ID",
            allocationSize = 1
    )
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Reference to the account on which this operation was performed.
     * <p>
     * Fetched lazily to avoid unnecessary database access when loading operations alone.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Type of the operation (deposit or withdrawal).
     * <p>
     * Stored as an integer ordinal corresponding to the {@link OperationType} enum.
     * Defaults to DEPOSIT if not explicitly set.
     * </p>
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private OperationType type = OperationType.DEPOSIT;

    /**
     * Monetary amount of the operation.
     * <p>
     * Positive values indicate deposits, negative values indicate withdrawals.
     * Represented with two decimal precision in the account's currency.
     * </p>
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Timestamp when the operation was executed.
     * <p>
     * Initialized to the current server time in UTC at creation.
     * </p>
     */
    @Column(name = "operation_date", nullable = false)
    private Instant operationDate = Instant.now();

    /**
     * Account balance immediately after this operation was applied.
     * <p>
     * Shows the new balance state resulting from this transaction.
     * </p>
     */
    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    /**
     * All-args constructor for creating Operation instances in tests or internal code.
     *
     * @param id             the unique identifier of the operation
     * @param account        the account associated with this operation
     * @param type           the type of operation (deposit or withdrawal)
     * @param amount         the amount transacted
     * @param operationDate  the timestamp when the operation occurred
     * @param balanceAfter   the resulting account balance after the operation
     */
    public Operation(final Long id,
                     final Account account,
                     final OperationType type,
                     final BigDecimal amount,
                     final Instant operationDate,
                     final BigDecimal balanceAfter) {
        this.id = id;
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.operationDate = operationDate;
        this.balanceAfter = balanceAfter;
    }
}
