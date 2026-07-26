package com.personaltracker.finance.controller;

import com.personaltracker.finance.dtos.LoginDto;
import com.personaltracker.finance.dtos.UserDto;
import com.personaltracker.finance.dtos.UserResponseDto;
import com.personaltracker.finance.models.User;
import com.personaltracker.finance.services.UserService;
import com.personaltracker.finance.utility.Mapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final Mapper mapper;

    @PostMapping("/save")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserDto userDto) {
        User savedUser = userService.registerUser(userDto);
        UserResponseDto responseDto = mapper.mapUserToUserResponseDto(savedUser);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto loginDto) {
        String token = userService.isValidCredentials(loginDto.getEmail(), loginDto.getPassword());
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        String message = userService.verifyEmail(token);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam("email") String email) {
        String message = userService.resendVerificationEmail(email);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
