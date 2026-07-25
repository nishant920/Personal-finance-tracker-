package com.personaltracker.finance.services;

import com.personaltracker.finance.dtos.BalanceResponseDto;
import com.personaltracker.finance.dtos.SpendRiskResponseDto;
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
     * Updates the current balance for the authenticated user and returns updated balance details.
     */
    public BalanceResponseDto updateBalanceForCurrentUser(BigDecimal newBalance) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User currentUser;
        if (principal instanceof User) {
            currentUser = (User) principal;
        } else {
            throw new BadRequestException("No authenticated user found in security context");
        }

        currentUser.setCurrentBalance(newBalance);
        User savedUser = userRepository.save(currentUser);

        BigDecimal freeToSpend = calculateFreeToSpend(savedUser);

        log.info("Updated Current Balance for User ID {}: New Balance = {}, Free-to-Spend = {}",
                savedUser.getId(), savedUser.getCurrentBalance(), freeToSpend);

        return BalanceResponseDto.builder()
                .currentBalance(savedUser.getCurrentBalance())
                .freeToSpend(freeToSpend)
                .build();
    }

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
     * Checks if a proposed spend amount eats into committed money for the authenticated user.
     */
    public SpendRiskResponseDto checkSpendRisk(BigDecimal proposedAmount) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User currentUser;
        if (principal instanceof User) {
            currentUser = (User) principal;
        } else {
            throw new BadRequestException("No authenticated user found in security context");
        }

        BigDecimal freeToSpendBefore = calculateFreeToSpend(currentUser);
        BigDecimal remainingAfterSpend = freeToSpendBefore.subtract(proposedAmount);

        boolean risk = remainingAfterSpend.compareTo(BigDecimal.ZERO) < 0;
        String message = risk
                ? "This spend eats into ₹" + remainingAfterSpend.abs() + " of your committed money"
                : null;

        log.info("Check Spend Risk for User ID {}: Proposed = {}, FreeBefore = {}, Remaining = {}, Risk = {}",
                currentUser.getId(), proposedAmount, freeToSpendBefore, remainingAfterSpend, risk);

        return SpendRiskResponseDto.builder()
                .risk(risk)
                .freeToSpendBefore(freeToSpendBefore)
                .remainingAfterSpend(remainingAfterSpend)
                .message(message)
                .build();
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
