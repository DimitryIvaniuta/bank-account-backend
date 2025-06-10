package com.github.dimitryivaniuta.bankaccount.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity representing a bank account.
 * <p>
 * Encapsulates the unique identifier, current balance, and creation timestamp
 * of a client's bank account. Mapped to the "account" table in the database.
 * </p>
 */
@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /**
     * Unique primary key for the account.
     * <p>
     * Automatically generated using a database sequence "BA_UNIQUE_ID".
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
     * Current balance of the account.
     * <p>
     * Stored with two-decimal precision to represent the major currency unit.
     * Defaults to zero when the account is created.
     * </p>
     */
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Timestamp when the account was created.
     * <p>
     * Assigned automatically at instantiation and never modified.
     * Stored in UTC.
     * </p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
