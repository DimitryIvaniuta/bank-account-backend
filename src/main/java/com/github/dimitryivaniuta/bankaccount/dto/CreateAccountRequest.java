package com.github.dimitryivaniuta.bankaccount.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialBalance;
}
