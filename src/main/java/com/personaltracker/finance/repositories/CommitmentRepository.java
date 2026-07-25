package com.personaltracker.finance.repositories;

import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.models.Commitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommitmentRepository extends JpaRepository<Commitment, Long> {
    List<Commitment> findByUserId(Long userId);
    List<Commitment> findByUserIdAndStatus(Long userId, CommitmentStatus status);
    Optional<Commitment> findByIdAndUserId(Long id, Long userId);
}
