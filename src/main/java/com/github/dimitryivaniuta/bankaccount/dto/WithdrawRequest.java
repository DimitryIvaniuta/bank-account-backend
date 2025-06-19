package com.github.dimitryivaniuta.bankaccount.dto;

import java.math.BigDecimal;
import java.util.Currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.money.MonetaryAmount;

/**
 * Request DTO for withdrawing funds from an existing bank account.
 * <p>
 * Contains the amount to withdraw. Validation ensures the amount
 * is provided and strictly positive. The service layer will
 * enforce sufficient balance before performing the withdrawal.
 * </p>
 *
 * @see com.github.dimitryivaniuta.bankaccount.service.AccountService#withdraw(Long, MonetaryAmount)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    /**
     * Amount to withdraw from the account.
     * <p>
     * Must be non-null and greater than zero. Represented in the
     * account's currency with two decimal places.
     * </p>
     * <ul>
     *   <li>{@link NotNull} ensures a value is supplied.</li>
     *   <li>{@link Positive} enforces that the value is > 0.</li>
     * </ul>
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Withdrawal amount must be > 0")
    private BigDecimal amount;

    /**
     * ISO-4217 currency code of the withdrawal (e.g., "USD").
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private Currency currency;

}
