package com.personaltracker.finance.repositories;

import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.models.Commitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitmentRepository extends JpaRepository<Commitment, Long> {
    List<Commitment> findByUserIdAndStatus(Long userId, CommitmentStatus status);
}
