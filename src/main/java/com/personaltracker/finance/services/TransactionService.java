package com.personaltracker.finance.services;

import com.personaltracker.finance.dtos.TransactionAnalysisDto;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
     * Analyzes transactions for the authenticated user:
     * - Total spend vs total income
     * - Spend-to-income percentage
     * - Top 5 spend categories ranked highest to lowest
     */
    public TransactionAnalysisDto getTransactionAnalysis() {
        User currentUser = getAuthenticatedUser();
        List<TransactionResponseDto> transactions = getAllTransactionsForCurrentUser();

        BigDecimal totalSpend = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (TransactionResponseDto transaction : transactions) {
            if (transaction.getTransactionType() == TransactionType.SPEND) {
                totalSpend = totalSpend.add(transaction.getAmount());
            } else if (transaction.getTransactionType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            }
        }

        BigDecimal spendPercentage = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            spendPercentage = totalSpend
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Map<String, BigDecimal> top5Categories = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.SPEND && t.getCategory() != null && !t.getCategory().isBlank())
                .collect(Collectors.groupingBy(
                        TransactionResponseDto::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                TransactionResponseDto::getAmount,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return TransactionAnalysisDto.builder()
                .totalSpend(totalSpend)
                .totalIncome(totalIncome)
                .spendToIncomePercentage(spendPercentage)
                .topCategories(top5Categories)
                .build();
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

    @Transactional
    public List<TransactionResponseDto> getAllTransactionsByCategory(String category){
        User currentUser = getAuthenticatedUser();
        return transactionRepository.findByUserIdAndCategory(currentUser.getId(), category).
                stream().
                map(this::mapToResponseDto).
                collect(Collectors.toList());

        /*
        List<Transaction> entites = transactionRepository.findByUserIdAndCategory(currentUser.getId(), category);

        * List<TransactionResponseDtos> listDto = new ArrayList<>();
         for(Transaction transaction : entites){
            TransactionResponseDto dto = mapToResponseDto(transaction);
            listDto.add(dto);
         }
         return dtoList;
         */

    }
}
