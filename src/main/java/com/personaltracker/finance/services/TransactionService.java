package com.personaltracker.finance.services;

import com.personaltracker.finance.dtos.TransactionRequestDto;
import com.personaltracker.finance.dtos.TransactionResponseDto;
import com.personaltracker.finance.enums.TransactionType;
import com.personaltracker.finance.exceptions.BadRequestException;
import com.personaltracker.finance.models.Transaction;
import com.personaltracker.finance.models.User;
import com.personaltracker.finance.repositories.TransactionRepository;
import com.personaltracker.finance.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing real-time monetary transactions (SPEND and INCOME).
 * Updates current account balance in database and records activity history.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Extracts the currently authenticated User entity directly from the SecurityContext.
     */
    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new BadRequestException("No authenticated user found in security context");
    }

    /**
     * Maps a Transaction database entity into a clean response DTO.
     */
    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .category(transaction.getCategory())
                .note(transaction.getNote())
                .timestamp(transaction.getTimestamp())
                .build();
    }

    /**
     * Records a new transaction (SPEND or INCOME) and automatically updates user's current balance.
     *
     * @param requestDto Transaction details (amount, transactionType, category, note)
     * @return Created TransactionResponseDto
     */
    @Transactional
    public TransactionResponseDto recordTransaction(TransactionRequestDto requestDto) {
        User currentUser = getAuthenticatedUser();
        BigDecimal currentBalance = currentUser.getCurrentBalance() != null ? currentUser.getCurrentBalance() : BigDecimal.ZERO;

        BigDecimal updatedBalance;
        if (requestDto.getTransactionType() == TransactionType.SPEND) {
            updatedBalance = currentBalance.subtract(requestDto.getAmount());
        } else if (requestDto.getTransactionType() == TransactionType.INCOME) {
            updatedBalance = currentBalance.add(requestDto.getAmount());
        } else {
            throw new BadRequestException("Invalid transaction type");
        }

        currentUser.setCurrentBalance(updatedBalance);
        userRepository.save(currentUser);

        Transaction transaction = new Transaction();
        transaction.setUser(currentUser);
        transaction.setAmount(requestDto.getAmount());
        transaction.setTransactionType(requestDto.getTransactionType());
        transaction.setCategory(requestDto.getCategory());
        transaction.setNote(requestDto.getNote());
        transaction.setTimestamp(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Recorded {} transaction ID {} for User ID {}. New Balance: {}",
                requestDto.getTransactionType(), savedTransaction.getId(), currentUser.getId(), updatedBalance);

        return mapToResponseDto(savedTransaction);
    }

    /**
     * Retrieves all transaction history for the authenticated user ordered by most recent.
     */
    public List<TransactionResponseDto> getAllTransactionsForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        return transactionRepository.findByUserIdOrderByTimestampDesc(currentUser.getId()).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves transaction history filtered by type (SPEND or INCOME).
     */
    public List<TransactionResponseDto> getTransactionsByTypeForCurrentUser(TransactionType type) {
        User currentUser = getAuthenticatedUser();
        return transactionRepository.findByUserIdAndTransactionTypeOrderByTimestampDesc(currentUser.getId(), type).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Permanently deletes a transaction for the authenticated user and reverses its balance impact.
     *
     * @param transactionId ID of the transaction to delete
     */
    @Transactional
    public void deleteTransaction(Long transactionId) {
        User currentUser = getAuthenticatedUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Transaction not found with id: " + transactionId));

        BigDecimal currentBalance = currentUser.getCurrentBalance() != null ? currentUser.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal updatedBalance;

        // Reverse transaction effect on currentBalance
        if (transaction.getTransactionType() == TransactionType.SPEND) {
            updatedBalance = currentBalance.add(transaction.getAmount());
        } else {
            updatedBalance = currentBalance.subtract(transaction.getAmount());
        }

        currentUser.setCurrentBalance(updatedBalance);
        userRepository.save(currentUser);

        transactionRepository.delete(transaction);
        log.info("Deleted transaction ID {} for User ID {}. Reverted Balance: {}", transactionId, currentUser.getId(), updatedBalance);
    }
}
