package com.personaltracker.finance.controller;

import com.personaltracker.finance.dtos.BalanceResponseDto;
import com.personaltracker.finance.dtos.SpendRiskRequestDto;
import com.personaltracker.finance.dtos.SpendRiskResponseDto;
import com.personaltracker.finance.dtos.UpdateBalanceRequestDto;
import com.personaltracker.finance.services.BalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @PutMapping
    public ResponseEntity<BalanceResponseDto> updateBalance(@Valid @RequestBody UpdateBalanceRequestDto requestDto) {
        BalanceResponseDto responseDto = balanceService.updateBalanceForCurrentUser(requestDto.getBalance());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/free-to-spend")
    public ResponseEntity<BigDecimal> getFreeToSpend() {
        BigDecimal freeToSpend = balanceService.freeToSpendForCurrentUser();
        return new ResponseEntity<>(freeToSpend, HttpStatus.OK);
    }

    @PostMapping("/check-risk")
    public ResponseEntity<SpendRiskResponseDto> checkSpendRisk(@Valid @RequestBody SpendRiskRequestDto requestDto) {
        SpendRiskResponseDto responseDto = balanceService.checkSpendRisk(requestDto.getAmount());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
