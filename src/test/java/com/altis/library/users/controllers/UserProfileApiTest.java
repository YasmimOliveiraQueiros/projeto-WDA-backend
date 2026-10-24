package com.altis.library.users.controllers;

import com.altis.library.auth.services.JwtService;
import com.altis.library.books.models.entities.Book;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.loans.services.LoanService;
import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import com.altis.library.users.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.altis.library.exceptions.ConflictException;

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileApiTest {

    private static final String CURRENT_PASSWORD = "CurrentPassword123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User user;
    private User otherUser;
    private User admin;
    private Publisher publisher;
    private Book book;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void createUsers() throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");
        user = createUser("user-" + unique + "@example.com", unique.substring(0, 10));
        otherUser = createUser("other-" + unique + "@example.com", unique.substring(10, 20));
        admin = createUser("admin-" + unique + "@example.com", unique.substring(20, 30));
        admin.setAdmin(true);
        userRepository.save(admin);
        publisher = publisherRepository.save(new Publisher(
                "Profile test publisher " + unique,
                unique + "@publisher.example",
                unique.substring(0, 18),
                "City",
                "ST"
        ));
        book = bookRepository.save(new Book(
                "Profile test book " + unique,
                "Test author",
                publisher,
                10,
                null
        ));

        userToken = jwtService.generateToken(
                user.getEmail(), false, user.getId(), user.getTokenVersion());
        adminToken = jwtService.generateToken(
                admin.getEmail(), true, admin.getId(), admin.getTokenVersion());
    }

    @AfterEach
    void removeUsers() {
        loanRepository.findAll().stream()
                .filter(loan -> loan.getUser().getId().equals(user.getId())
                        || loan.getUser().getId().equals(otherUser.getId()))
                .forEach(loanRepository::delete);
        bookRepository.deleteById(book.getId());
        publisherRepository.deleteById(publisher.getId());
        userRepository.deleteAllById(List.of(user.getId(), otherUser.getId(), admin.getId()));
    }

    @Test
    void ownProfileRequiresAnActiveCommonUser() throws Exception {
        mockMvc.perform(get("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/users/me").header("Authorization", bearer(adminToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());

        user.setActive(false);
        userRepository.save(user);
        mockMvc.perform(get("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileUpdateRejectsProtectedFieldsAndKeepsTheTokenValidAfterEmailChange()
            throws Exception {
        String profileRequest = objectMapper.writeValueAsString(java.util.Map.of(
                "name", "Updated Name",
                "email", "new-" + user.getEmail(),
                "phone", "555-0199",
                "address", "Updated address"
        ));

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new-" + user.getEmail()))
                .andExpect(jsonPath("$.cpf").value(user.getCpf()))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"));

        mockMvc.perform(get("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new-" + user.getEmail()));

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Name",
                                  "email": "new-%s",
                                  "phone": "555-0199",
                                  "address": "Updated address",
                                  "cpf": "00000000000"
                                }
                                """.formatted(user.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(user.getCpf()))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"))
                .andExpect(jsonPath("$.isAdmin").value(false));

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "name", "Attempt",
                                "email", otherUser.getEmail(),
                                "phone", "555-0100",
                                "address", "Attempt address"
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    void passwordChangeChecksCurrentPasswordAndInvalidatesOldTokens() throws Exception {
        mockMvc.perform(patch("/users/me/password")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordRequest("wrong", "NewPassword123", "NewPassword123")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/users/me/password")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordRequest(CURRENT_PASSWORD, "NewPassword123", "Mismatch123")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/users/me/password")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordRequest(CURRENT_PASSWORD, "NewPassword123", "NewPassword123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", user.getEmail(),
                                "password", "NewPassword123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", user.getEmail(),
                                "password", CURRENT_PASSWORD
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pendingAndOverdueLoansBlockAccountClosure() throws Exception {
        saveLoan(user, LoanStatus.PENDING);
        saveLoan(otherUser, LoanStatus.OVERDUE);
        String otherToken = jwtService.generateToken(
                otherUser.getEmail(), false, otherUser.getId(), otherUser.getTokenVersion());

        mockMvc.perform(delete("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Não é possível encerrar sua conta enquanto houver empréstimos pendentes. "
                                + "Regularize suas pendências antes de continuar."));
        mockMvc.perform(delete("/users/me").header("Authorization", bearer(otherToken)))
                .andExpect(status().isConflict());

        assertTrue(userRepository.findById(user.getId()).orElseThrow().isActive());
        assertTrue(otherUser.isActive());
    }

    @Test
    void returnedLoanStatusesAllowClosureAndPreserveUserAndHistory() throws Exception {
        Loan returned = saveLoan(user, LoanStatus.RETURNED);
        Loan returnedLate = saveLoan(otherUser, LoanStatus.RETURNED_LATE);
        String otherToken = jwtService.generateToken(
                otherUser.getEmail(), false, otherUser.getId(), otherUser.getTokenVersion());

        mockMvc.perform(delete("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/users/me").header("Authorization", bearer(otherToken)))
                .andExpect(status().isNoContent());

        User persisted = userRepository.findById(user.getId()).orElseThrow();
        assertFalse(persisted.isActive());
        assertEquals(user.getId(), persisted.getId());
        assertEquals(1, loanRepository.findByUserId(user.getId()).size());
        assertEquals(LoanStatus.RETURNED,
                loanRepository.findById(returned.getId()).orElseThrow().getStatus());
        assertTrue(userRepository.findByIdAndIsAdminFalse(user.getId()).isPresent());

        mockMvc.perform(get("/users/" + user.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get("/loans/" + returned.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
        mockMvc.perform(get("/loans/" + returnedLate.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED_LATE"));

        mockMvc.perform(get("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/loans/my").header("Authorization", bearer(userToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/me").header("Authorization", bearer(otherToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", user.getEmail(),
                                "password", CURRENT_PASSWORD
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void activeUserWithoutLoansCanCloseAndOldTokenIsRejected() throws Exception {
        mockMvc.perform(delete("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.findById(user.getId()).orElseThrow().isActive());
        mockMvc.perform(delete("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", user.getEmail(),
                                "password", CURRENT_PASSWORD
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accountClosureRequiresUserRoleAndAuthentication() throws Exception {
        mockMvc.perform(delete("/users/me").header("Authorization", bearer(adminToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/users/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/users/" + otherUser.getId())
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void inactiveUsersCannotReceiveNewLoans() {
        user.setActive(false);
        userRepository.save(user);

        assertThrows(ConflictException.class, () -> loanService.create(loanRequest(user)));
    }

    @Test
    void concurrentLoanCreationAndClosurePreserveTheAccountInvariant() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var create = executor.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    loanService.create(loanRequest(user));
                    return true;
                } catch (ConflictException exception) {
                    return false;
                }
            });
            var close = executor.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    userService.closeMyAccount(user.getEmail());
                    return true;
                } catch (ConflictException exception) {
                    return false;
                }
            });

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            boolean loanCreated = create.get();
            boolean accountClosed = close.get();

            User persisted = userRepository.findById(user.getId()).orElseThrow();
            long activeLoans = loanRepository.countByUserIdAndStatusIn(
                    user.getId(),
                    List.of(LoanStatus.PENDING, LoanStatus.OVERDUE)
            );
            assertFalse(accountClosed && loanCreated);
            if (persisted.isActive()) {
                assertTrue(loanCreated);
                assertEquals(1, activeLoans);
            } else {
                assertTrue(accountClosed);
                assertFalse(loanCreated);
                assertEquals(0, activeLoans);
            }
        } finally {
            start.countDown();
        }
    }

    private Loan saveLoan(User borrower, LoanStatus status) {
        Loan loan = new Loan(
                borrower,
                book,
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                null
        );
        loan.setStatus(status);
        return loanRepository.save(loan);
    }

    private LoanRequest loanRequest(User borrower) {
        var request = new LoanRequest();
        request.setUserId(borrower.getId());
        request.setBookId(book.getId());
        request.setLoanDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));
        return request;
    }

    private User createUser(String email, String cpfSuffix) {
        User created = new User(
                "Test User",
                email,
                passwordEncoder.encode(CURRENT_PASSWORD),
                "555-0100",
                cpfSuffix,
                LocalDate.of(1990, 1, 1),
                "Test address"
        );
        return userRepository.save(created);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String passwordRequest(String current, String next, String confirmation)
            throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "currentPassword", current,
                "newPassword", next,
                "confirmPassword", confirmation
        ));
    }
}
