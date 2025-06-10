package com.github.dimitryivaniuta.bankaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object representing the essential details of a bank account
 * as returned by the API.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {

    /**
     * Unique identifier of the bank account.
     */
    private Long id;

    /**
     * Current balance of the account, represented in the currency's
     * major unit (e.g., dollars). Precision is 2 decimal places.
     */
    private BigDecimal balance;

    /**
     * Timestamp indicating when the account was created.
     * Stored in UTC.
     */
    private Instant createdAt;
}
