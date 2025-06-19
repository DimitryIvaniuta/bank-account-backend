package com.github.dimitryivaniuta.bankaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object representing a single entry in an account statement.
 * <p>
 * Encapsulates the details of a transaction performed on a bank account,
 * including when it occurred, what type of operation it was, the amount,
 * and the resulting balance after the operation.
 * </p>
 *
 * @see com.github.dimitryivaniuta.bankaccount.model.OperationType
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementResponse {

    /**
     * The timestamp when the operation occurred.
     * <p>
     * Represented in UTC as an {@link Instant}.
     * </p>
     */
    private Instant date;

    /**
     * The type of operation executed.
     * <p>
     * Expected values correspond to constants defined in the
     * {@link com.github.dimitryivaniuta.bankaccount.model.OperationType} enum,
     * such as "DEPOSIT" or "WITHDRAWAL".
     * </p>
     */
    private String type;

    /**
     * The monetary amount of the transaction.
     * <p>
     * Positive values indicate credits (deposits),
     * and negative values indicate debits (withdrawals).
     * Expressed in the account's currency with two decimal places.
     * </p>
     */
    private BigDecimal amount;

    /**
     * ISO-4217 currency code of the operation amount and resulting balance.
     */
    private String currency;

    /**
     * The account balance immediately after the operation was applied.
     * <p>
     * Reflects the updated balance state post-transaction,
     * using two decimal precision in the account's currency.
     * </p>
     */
    private BigDecimal balance;
}
