package com.github.dimitryivaniuta.bankaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data @AllArgsConstructor @NoArgsConstructor
public class StatementResponse {
    private Instant date;
    private String type;
    private BigDecimal amount;
    private BigDecimal balance;
}