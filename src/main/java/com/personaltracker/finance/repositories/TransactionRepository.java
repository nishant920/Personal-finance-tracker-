package com.personaltracker.finance.repositories;

import com.personaltracker.finance.enums.TransactionType;
import com.personaltracker.finance.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTimestampDesc(Long userId);
    List<Transaction> findByUserIdAndTransactionTypeOrderByTimestampDesc(Long userId, TransactionType transactionType);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
