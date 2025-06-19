package com.github.dimitryivaniuta.bankaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

/**
 * Request DTO for updating the balance of an existing bank account.
 * <p>
 * Encapsulates the new desired balance to which the account
 * should be adjusted. Validation annotations enforce non-null
 * and non-negative constraints.
 * </p>
 *
 * @see com.github.dimitryivaniuta.bankaccount.service.AccountService#updateAccountBalance(Long, MonetaryAmount)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    /**
     * The target balance for the account.
     * <p>
     * Must be provided and cannot be negative. The service layer
     * calculates the required deposit or withdrawal delta to reach
     * this balance.
     * </p>
     * <ul>
     *   <li>{@link NotNull} ensures a value is supplied.</li>
     *   <li>{@link DecimalMin &#40;"0.00"&#41;} enforces a minimum of zero.</li>
     * </ul>
     */
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Target amount must be >= 0.00")
    private BigDecimal targetAmount;

    /**
     * ISO-4217 currency code of the target amount (e.g., "EUR").
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

}
