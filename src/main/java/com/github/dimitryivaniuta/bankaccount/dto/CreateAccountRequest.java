package com.github.dimitryivaniuta.bankaccount.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialBalance;
}