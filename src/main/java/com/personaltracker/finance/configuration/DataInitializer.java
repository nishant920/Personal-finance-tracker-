package com.personaltracker.finance.configuration;

import com.personaltracker.finance.enums.CommitmentStatus;
import com.personaltracker.finance.enums.Frequency;
import com.personaltracker.finance.enums.TransactionType;
import com.personaltracker.finance.models.Commitment;
import com.personaltracker.finance.models.Transaction;
import com.personaltracker.finance.models.User;
import com.personaltracker.finance.repositories.CommitmentRepository;
import com.personaltracker.finance.repositories.TransactionRepository;
import com.personaltracker.finance.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Database Initializer to seed default demo credentials on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CommitmentRepository commitmentRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User dummyUser = userRepository.findByEmail("dummy@gmail.com");
        if (dummyUser == null) {
            dummyUser = new User();
            dummyUser.setName("Demo User");
            dummyUser.setEmail("dummy@gmail.com");
            dummyUser.setPassword(passwordEncoder.encode("password"));
            dummyUser.setVerified(true);
            dummyUser.setCurrentBalance(new BigDecimal("50000.00"));

            dummyUser = userRepository.save(dummyUser);
            log.info("Successfully seeded demo user account: dummy@gmail.com");
        }

        // Seed demo commitments if none exist for demo user
        if (commitmentRepository.findByUserId(dummyUser.getId()).isEmpty()) {
            Commitment rent = new Commitment();
            rent.setUser(dummyUser);
            rent.setName("Apartment Rent");
            rent.setAmount(new BigDecimal("15000.00"));
            rent.setDueDate(LocalDate.now().plusDays(7));
            rent.setFrequency(Frequency.MONTHLY);
            rent.setStatus(CommitmentStatus.PENDING);
            commitmentRepository.save(rent);

            Commitment internet = new Commitment();
            internet.setUser(dummyUser);
            internet.setName("High-Speed Internet");
            internet.setAmount(new BigDecimal("1200.00"));
            internet.setDueDate(LocalDate.now().plusDays(12));
            internet.setFrequency(Frequency.MONTHLY);
            internet.setStatus(CommitmentStatus.PENDING);
            commitmentRepository.save(internet);

            log.info("Successfully seeded demo commitments for dummy@gmail.com");
        }

        // Seed demo transactions if none exist for demo user
        if (transactionRepository.findByUserIdOrderByTimestampDesc(dummyUser.getId()).isEmpty()) {
            Transaction income = new Transaction();
            income.setUser(dummyUser);
            income.setAmount(new BigDecimal("65000.00"));
            income.setTransactionType(TransactionType.INCOME);
            income.setCategory("Salary");
            income.setNote("Monthly tech salary deposit");
            income.setTimestamp(LocalDateTime.now().minusDays(5));
            transactionRepository.save(income);

            Transaction spend1 = new Transaction();
            spend1.setUser(dummyUser);
            spend1.setAmount(new BigDecimal("3500.00"));
            spend1.setTransactionType(TransactionType.SPEND);
            spend1.setCategory("Groceries");
            spend1.setNote("Supermarket weekly supplies");
            spend1.setTimestamp(LocalDateTime.now().minusDays(2));
            transactionRepository.save(spend1);

            Transaction spend2 = new Transaction();
            spend2.setUser(dummyUser);
            spend2.setAmount(new BigDecimal("1800.00"));
            spend2.setTransactionType(TransactionType.SPEND);
            spend2.setCategory("Dining Out");
            spend2.setNote("Weekend dinner with friends");
            spend2.setTimestamp(LocalDateTime.now().minusDays(1));
            transactionRepository.save(spend2);

            log.info("Successfully seeded demo transactions for dummy@gmail.com");
        }
    }
}
