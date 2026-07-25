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

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitmentService {

    private final CommitmentRepository commitmentRepository;

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new BadRequestException("No authenticated user found in security context");
    }

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

    public List<CommitmentResponseDto> getAllCommitmentsForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        return commitmentRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CommitmentResponseDto> getPendingCommitmentsForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        return commitmentRepository.findByUserIdAndStatus(currentUser.getId(), CommitmentStatus.PENDING).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public CommitmentResponseDto markAsPaid(Long commitmentId) {
        User currentUser = getAuthenticatedUser();
        Commitment commitment = commitmentRepository.findByIdAndUserId(commitmentId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Commitment not found with id: " + commitmentId));

        commitment.setStatus(CommitmentStatus.PAID);
        Commitment updatedCommitment = commitmentRepository.save(commitment);
        log.info("Marked commitment ID {} as PAID for User ID {}", commitmentId, currentUser.getId());

        return mapToResponseDto(updatedCommitment);
    }

    public void deleteCommitment(Long commitmentId) {
        User currentUser = getAuthenticatedUser();
        Commitment commitment = commitmentRepository.findByIdAndUserId(commitmentId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Commitment not found with id: " + commitmentId));

        commitmentRepository.delete(commitment);
        log.info("Deleted commitment ID {} for User ID {}", commitmentId, currentUser.getId());
    }
}
