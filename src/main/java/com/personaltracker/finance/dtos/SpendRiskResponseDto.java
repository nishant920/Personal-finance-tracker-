package com.personaltracker.finance.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendRiskResponseDto {
    private boolean risk;
    private BigDecimal freeToSpendBefore;
    private BigDecimal remainingAfterSpend;
    private String message;
}
