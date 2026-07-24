package com.personaltracker.finance.services;

import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.exceptions.BadRequestException;
import com.personaltracker.finance.models.Commitment;
import com.personaltracker.finance.models.User;
import com.personaltracker.finance.repositories.CommitmentRepository;
import com.personaltracker.finance.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final UserRepository userRepository;
    private final CommitmentRepository commitmentRepository;

    /**
     * Calculates Free-to-Spend for the currently authenticated user automatically from SecurityContext.
     */
    public BigDecimal freeToSpendForCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User currentUser;
        if (principal instanceof User) {
            currentUser = (User) principal;
        } else {
            throw new BadRequestException("No authenticated user found in security context");
        }

        return calculateFreeToSpend(currentUser);
    }

    /**
     * Calculates Free-to-Spend for a given User entity.
     */
    public BigDecimal calculateFreeToSpend(User user) {
        BigDecimal currentBalance = user.getCurrentBalance() != null ? user.getCurrentBalance() : BigDecimal.ZERO;

        List<Commitment> pendingCommitments = commitmentRepository.findByUserIdAndStatus(user.getId(), CommitmentStatus.PENDING);

        BigDecimal reserved = pendingCommitments.stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("User ID {}: Current Balance = {}, Reserved = {}", user.getId(), currentBalance, reserved);

        return currentBalance.subtract(reserved);
    }

    /**
     * Helper overload by userId.
     */
    public BigDecimal freeToSpend(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        return calculateFreeToSpend(user);
    }
}
