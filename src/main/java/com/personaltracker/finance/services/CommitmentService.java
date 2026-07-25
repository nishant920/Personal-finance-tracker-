package com.personaltracker.finance.services;

import com.personaltracker.finance.dtos.CommitmentRequestDto;
import com.personaltracker.finance.dtos.CommitmentResponseDto;
import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.exceptions.BadRequestException;
import com.personaltracker.finance.models.Commitment;
import com.personaltracker.finance.models.User;
import com.personaltracker.finance.repositories.CommitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing financial commitments (EMIs, bills, subscriptions, rent).
 * Handles creation, status updates, deletion, and user-scoped data retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommitmentService {

    private final CommitmentRepository commitmentRepository;

    /**
     * Extracts the currently authenticated User entity directly from the SecurityContext.
     * Prevents security vulnerabilities by ensuring users can only interact with their own data.
     *
     * @return Currently authenticated User
     * @throws BadRequestException if no valid user is present in the security context
     */
    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new BadRequestException("No authenticated user found in security context");
    }

    /**
     * Maps a Commitment database entity into a clean response DTO for API clients.
     *
     * @param commitment The database entity to transform
     * @return CommitmentResponseDto populated with commitment fields
     */
    private CommitmentResponseDto mapToResponseDto(Commitment commitment) {
        return CommitmentResponseDto.builder()
                .id(commitment.getId())
                .name(commitment.getName())
                .amount(commitment.getAmount())
                .dueDate(commitment.getDueDate())
                .frequency(commitment.getFrequency())
                .status(commitment.getStatus())
                .build();
    }

    /**
     * Creates a new financial commitment for the currently authenticated user.
     * Defaults the commitment status to PENDING if not explicitly specified.
     *
     * @param requestDto Payload containing commitment details (name, amount, dueDate, frequency)
     * @return CommitmentResponseDto containing the created commitment details
     */
    public CommitmentResponseDto createCommitment(CommitmentRequestDto requestDto) {
        User currentUser = getAuthenticatedUser();

        Commitment commitment = new Commitment();
        commitment.setUser(currentUser);
        commitment.setName(requestDto.getName());
        commitment.setAmount(requestDto.getAmount());
        commitment.setDueDate(requestDto.getDueDate());
        commitment.setFrequency(requestDto.getFrequency());
        commitment.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : CommitmentStatus.PENDING);

        Commitment savedCommitment = commitmentRepository.save(commitment);
        log.info("Created commitment ID {} for User ID {}", savedCommitment.getId(), currentUser.getId());

        return mapToResponseDto(savedCommitment);
    }

    /**
     * Retrieves all commitments (both PENDING and PAID) belonging to the authenticated user.
     *
     * @return List of CommitmentResponseDto items
     */
    public List<CommitmentResponseDto> getAllCommitmentsForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        return commitmentRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves only unpaid/pending commitments for the authenticated user.
     * Used by BalanceService to calculate total reserved funds.
     *
     * @return List of PENDING CommitmentResponseDto items
     */
    public List<CommitmentResponseDto> getPendingCommitmentsForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        return commitmentRepository.findByUserIdAndStatus(currentUser.getId(), CommitmentStatus.PENDING).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks a specific commitment as PAID for the authenticated user.
     * Once paid, the commitment is no longer reserved, increasing the user's Free-to-Spend balance.
     *
     * @param commitmentId ID of the commitment to update
     * @return Updated CommitmentResponseDto with status PAID
     */
    public CommitmentResponseDto markAsPaid(Long commitmentId) {
        User currentUser = getAuthenticatedUser();
        Commitment commitment = commitmentRepository.findByIdAndUserId(commitmentId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Commitment not found with id: " + commitmentId));

        commitment.setStatus(CommitmentStatus.PAID);
        Commitment updatedCommitment = commitmentRepository.save(commitment);
        log.info("Marked commitment ID {} as PAID for User ID {}", commitmentId, currentUser.getId());

        return mapToResponseDto(updatedCommitment);
    }

    /**
     * Permanently deletes a commitment belonging to the authenticated user.
     * Ensures strict user ownership validation before deletion.
     *
     * @param commitmentId ID of the commitment to delete
     */
    public void deleteCommitment(Long commitmentId) {
        User currentUser = getAuthenticatedUser();
        Commitment commitment = commitmentRepository.findByIdAndUserId(commitmentId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Commitment not found with id: " + commitmentId));

        commitmentRepository.delete(commitment);
        log.info("Deleted commitment ID {} for User ID {}", commitmentId, currentUser.getId());
    }
}
