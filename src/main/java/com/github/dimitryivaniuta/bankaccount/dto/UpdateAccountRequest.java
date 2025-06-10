package com.github.dimitryivaniuta.bankaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for updating the balance of an existing bank account.
 * <p>
 * Encapsulates the new desired balance to which the account
 * should be adjusted. Validation annotations enforce non-null
 * and non-negative constraints.
 * </p>
 *
 * @see com.github.dimitryivaniuta.bankaccount.service.AccountService#updateAccountBalance(Long, BigDecimal)
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
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal newBalance;
}
