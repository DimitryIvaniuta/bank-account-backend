package com.github.dimitryivaniuta.bankaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
     * Amount to deposit; must be positive.
     * <p>
     * Must be non-null and strictly greater than 0.
     * Represented in the currency's major unit with two decimal places.
     * </p>
     * <ul>
     *   <li>{@link NotNull} ensures this field is provided.</li>
     *   <li>{@link Positive} enforces a value > 0.</li>
     * </ul>
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Deposit amount must be > 0")
    private BigDecimal amount;

    /**
     * ISO-4217 currency code of the deposit (e.g., "GBP").
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;
}
