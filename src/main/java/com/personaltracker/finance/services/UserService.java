package com.personaltracker.finance.services;

import com.personaltracker.finance.dtos.UserDto;
import com.personaltracker.finance.exceptions.BadRequestException;
import com.personaltracker.finance.exceptions.InvalidCredentialsException;
import com.personaltracker.finance.exceptions.UserAlreadyExistsException;
import com.personaltracker.finance.models.User;
import com.personaltracker.finance.models.VerificationToken;
import com.personaltracker.finance.repositories.UserRepository;
import com.personaltracker.finance.repositories.VerificationTokenRepository;
import com.personaltracker.finance.utility.JwtUtility;
import com.personaltracker.finance.utility.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationRepository;
    private final Mapper mapper;
    private final JwtUtility jwtUtility;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public User registerUser(UserDto userDto) {
        User existingUser = userRepository.findByEmail(userDto.getEmail());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = mapper.mapUserDetailsToUser(userDto);
        user.setVerified(false);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        verificationToken.setUser(savedUser);

        log.info("Token generated on registration of user {} ", token);
        verificationRepository.save(verificationToken);

        mailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getName(), token);

        return savedUser;
    }

    public String verifyEmail(String token) {
        VerificationToken verificationToken = verificationRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        verificationRepository.delete(verificationToken);

        return "Email verified successfully! You can now log in.";
    }

    public String isValidCredentials(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new BadRequestException("Please verify your email first");
        }

        return jwtUtility.generateToken(user.getEmail());
    }

}
