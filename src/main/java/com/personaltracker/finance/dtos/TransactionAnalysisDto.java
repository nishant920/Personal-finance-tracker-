package com.personaltracker.finance.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAnalysisDto {
    private BigDecimal totalSpend;
    private BigDecimal totalIncome;
    private BigDecimal spendToIncomePercentage;
    private Map<String, BigDecimal> topCategories;
}
