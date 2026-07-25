package com.personaltracker.finance.dtos;

import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.enums.Frequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitmentResponseDto {
    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Frequency frequency;
    private CommitmentStatus status;
}
