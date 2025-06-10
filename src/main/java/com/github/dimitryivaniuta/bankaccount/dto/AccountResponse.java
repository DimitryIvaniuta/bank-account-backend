package com.github.dimitryivaniuta.bankaccount.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data @AllArgsConstructor @NoArgsConstructor
public class AccountResponse {
    private Long id;
    private BigDecimal balance;
    private Instant createdAt;
}