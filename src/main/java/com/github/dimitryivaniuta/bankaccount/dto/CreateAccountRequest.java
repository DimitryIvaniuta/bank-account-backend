package com.github.dimitryivaniuta.bankaccount.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new bank account.
 * <p>
 * Contains the initial balance to be deposited when opening the account.
 * Validation constraints ensure the balance is non-null and non-negative.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    /**
     * Initial amount to deposit into the new account.
     * <p>
     * Must be non-null and greater than or equal to 0.00.
     * Represented in the currency's major unit with two decimal places.
     * </p>
     * <ul>
     *   <li>{@link NotNull} ensures this field is provided.</li>
     *   <li>{@link DecimalMin &#40;"0.00"&#41;} enforces a minimum of zero.</li>
     * </ul>
     */
    @NotNull(message = "Initial amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Initial amount must be >= 0.00")
    private BigDecimal initialAmount;

    /**
     * ISO-4217 currency code for the initial amount (e.g., "USD").
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

}
