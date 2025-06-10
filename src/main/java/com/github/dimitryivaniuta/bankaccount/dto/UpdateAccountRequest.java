package com.github.dimitryivaniuta.bankaccount.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateAccountRequest {
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal newBalance;
}