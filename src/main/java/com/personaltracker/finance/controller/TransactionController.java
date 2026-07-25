package com.personaltracker.finance.controller;

import com.personaltracker.finance.dtos.TransactionRequestDto;
import com.personaltracker.finance.dtos.TransactionResponseDto;
import com.personaltracker.finance.enums.TransactionType;
import com.personaltracker.finance.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> recordTransaction(@Valid @RequestBody TransactionRequestDto requestDto) {
        TransactionResponseDto responseDto = transactionService.recordTransaction(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        List<TransactionResponseDto> transactions = transactionService.getAllTransactionsForCurrentUser();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByType(@PathVariable("type") TransactionType type) {
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByTypeForCurrentUser(type);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable("id") Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>("Transaction deleted successfully", HttpStatus.OK);
    }
}
