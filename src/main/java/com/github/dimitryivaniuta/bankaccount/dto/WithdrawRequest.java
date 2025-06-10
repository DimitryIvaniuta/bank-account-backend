package com.github.dimitryivaniuta.bankaccount.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class WithdrawRequest {
    @NotNull @Positive
    private BigDecimal amount;
}