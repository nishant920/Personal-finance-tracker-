package com.personaltracker.finance.models;

import com.personaltracker.finance.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // SPEND, INCOME

    private String category;
    private String note;
    private LocalDateTime timestamp;
}

//precision = 12: This is the total number of digits allowed in the entire number (both to the left and to the right of the decimal point).
// scale = 2: This is the number of digits allowed after the decimal point (the fractional part).