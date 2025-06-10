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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "operation")
@Getter @Setter
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BA_UNIQUE_ID")
    @SequenceGenerator(name = "BA_UNIQUE_ID", sequenceName = "BA_UNIQUE_ID", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private OperationType type = OperationType.DEPOSIT;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "operation_date", nullable = false)
    private Instant operationDate = Instant.now();

    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    /**
     * All-args constructor used by unit tests to easily instantiate Operation.
     */
    public Operation(Long id,
                     Account account,
                     OperationType type,
                     BigDecimal amount,
                     Instant operationDate,
                     BigDecimal balanceAfter) {
        this.id             = id;
        this.account        = account;
        this.type           = type;
        this.amount         = amount;
        this.operationDate  = operationDate;
        this.balanceAfter   = balanceAfter;
    }

    public Operation() {
        // no‐args for JPA
    }
}