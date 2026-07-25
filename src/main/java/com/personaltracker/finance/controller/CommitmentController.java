package com.personaltracker.finance.controller;

import com.personaltracker.finance.dtos.CommitmentRequestDto;
import com.personaltracker.finance.dtos.CommitmentResponseDto;
import com.personaltracker.finance.services.CommitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commitments")
@RequiredArgsConstructor
public class CommitmentController {

    private final CommitmentService commitmentService;

    @PostMapping
    public ResponseEntity<CommitmentResponseDto> createCommitment(@Valid @RequestBody CommitmentRequestDto requestDto) {
        CommitmentResponseDto responseDto = commitmentService.createCommitment(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CommitmentResponseDto>> getAllCommitments() {
        List<CommitmentResponseDto> commitments = commitmentService.getAllCommitmentsForCurrentUser();
        return new ResponseEntity<>(commitments, HttpStatus.OK);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CommitmentResponseDto>> getPendingCommitments() {
        List<CommitmentResponseDto> pendingCommitments = commitmentService.getPendingCommitmentsForCurrentUser();
        return new ResponseEntity<>(pendingCommitments, HttpStatus.OK);
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<CommitmentResponseDto> markAsPaid(@PathVariable("id") Long id) {
        CommitmentResponseDto responseDto = commitmentService.markAsPaid(id);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCommitment(@PathVariable("id") Long id) {
        commitmentService.deleteCommitment(id);
        return new ResponseEntity<>("Commitment deleted successfully", HttpStatus.OK);
    }
}
