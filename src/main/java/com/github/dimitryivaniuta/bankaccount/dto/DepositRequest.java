package com.github.dimitryivaniuta.bankaccount.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class DepositRequest {
    @NotNull @Positive
    private BigDecimal amount;
}