package com.personaltracker.finance.repositories;

import com.personaltracker.finance.models.Commitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitmentRepository extends JpaRepository<Commitment, Long> {
}
