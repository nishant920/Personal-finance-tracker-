package com.personaltracker.finance.dtos;

import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.enums.Frequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitmentRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    private CommitmentStatus status;
}
