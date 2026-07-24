package com.personaltracker.finance.models;

import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.enums.Frequency;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commitment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;               // e.g. "Car EMI", "Rent", "Netflix"

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDate dueDate;         // Next payment date

    @Enumerated(EnumType.STRING)
    private Frequency frequency;        // MONTHLY, WEEKLY, ONE_TIME

    @Enumerated(EnumType.STRING)
    private CommitmentStatus status;   // PENDING, PAID, OVERDUE
}