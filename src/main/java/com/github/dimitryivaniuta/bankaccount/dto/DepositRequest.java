package com.github.dimitryivaniuta.bankaccount.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for depositing funds into an existing bank account.
 * <p>
 * Contains the amount to deposit.
 * Validation constraints ensure the amount is provided and positive.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    /**
     * Amount to deposit into the account.
     * <p>
     * Must be non-null and strictly greater than 0.
     * Represented in the currency's major unit with two decimal places.
     * </p>
     * <ul>
     *   <li>{@link NotNull} ensures this field is provided.</li>
     *   <li>{@link Positive} enforces a value > 0.</li>
     * </ul>
     */
    @NotNull
    @Positive
    private BigDecimal amount;
}
