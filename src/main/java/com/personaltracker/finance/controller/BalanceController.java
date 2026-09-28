package com.personaltracker.finance.controller;

import com.personaltracker.finance.dtos.*;
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

    //why we are using dto even for a single param in methode like BalanceRequestDto where there is only one entity in class?
    @PostMapping("/add")
    public ResponseEntity<?> addBalance(@RequestBody BalanceRequestDto requestDto){
        BalanceResponseDto responseDto = balanceService.addBalanceForCurrentUser(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<BalanceResponseDto> updateBalance(@Valid @RequestBody BalanceRequestDto requestDto) {
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
