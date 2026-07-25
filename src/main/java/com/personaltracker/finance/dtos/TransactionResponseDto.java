package com.personaltracker.finance.dtos;

import com.personaltracker.finance.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDto {
    private Long id;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String category;
    private String note;
    private LocalDateTime timestamp;
}
