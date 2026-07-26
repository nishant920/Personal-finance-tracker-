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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service managing user authentication, account registration, email verification,
 * and credential validation.
 */
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

    /**
     * Registers a new user account in the system.
     * Encodes the raw password with BCrypt, sets initial verification status to false,
     * generates an email verification token expiring in 30 minutes, and dispatches a verification email.
     *
     * @param userDto DTO containing user registration details (name, email, password)
     * @return Saved User entity
     * @throws UserAlreadyExistsException if an account with the provided email already exists
     */
    @Transactional
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

    /**
     * Resends a new verification email token to an unverified user.
     *
     * @param email User email address
     * @return Success message string
     */
    @Transactional
    public String resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("User not found with email: " + email);
        }

        if (user.isVerified()) {
            throw new BadRequestException("Email is already verified. You can log in directly.");
        }

        // Delete any pre-existing token for this user
        verificationRepository.deleteByUserId(user.getId());

        String newToken = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(newToken);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        verificationToken.setUser(user);

        verificationRepository.save(verificationToken);

        mailService.sendVerificationEmail(user.getEmail(), user.getName(), newToken);
        log.info("Resent new verification token {} to user {}", newToken, user.getEmail());

        return "Verification email sent successfully! Please check your inbox.";
    }

    /**
     * Verifies a user's account using the provided email verification token.
     * Validates token presence and expiration, sets the user's verified status to true,
     * and deletes the consumed verification token from the database.
     *
     * @param token UUID verification token string
     * @return Success message string
     * @throws BadRequestException if token is invalid or has expired
     */
    @Transactional
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

    /**
     * Authenticates a user's login credentials.
     * Verifies the email exists, compares the raw password against the BCrypt hash,
     * ensures the email has been verified, and issues a signed JWT authentication token.
     *
     * @param email User login email address
     * @param password User login raw password
     * @return Signed JWT authentication token string
     * @throws InvalidCredentialsException if email is not found or password does not match
     * @throws BadRequestException if the user has not verified their email address
     */
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
