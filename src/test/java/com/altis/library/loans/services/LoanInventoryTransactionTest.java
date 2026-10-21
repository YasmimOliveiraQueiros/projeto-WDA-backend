package com.altis.library.loans.services;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.models.enums.BookStatus;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.exceptions.ConflictException;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class LoanInventoryTransactionTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private BookRepository bookRepository;

    @MockitoSpyBean
    private LoanRepository loanRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private UserRepository userRepository;

    private Publisher publisher;
    private User user;
    private Book book;

    @BeforeEach
    void createInventoryFixture() {
        String unique = UUID.randomUUID().toString();
        publisher = publisherRepository.save(new Publisher(
                "Inventory test " + unique,
                unique + "@publisher.example",
                unique.replace("-", "").substring(0, 18),
                "City",
                "ST"
        ));
        user = userRepository.save(new User(
                "Inventory test",
                unique + "@user.example",
                "password",
                "555-0100",
                unique.replace("-", "").substring(0, 14),
                LocalDate.of(2000, 1, 1),
                "Test address"
        ));
        book = bookRepository.save(new Book(
                "Inventory test " + unique,
                "Test author",
                publisher,
                1,
                null
        ));
    }

    @AfterEach
    void removeInventoryFixture() {
        loanRepository.findAll().stream()
                .filter(loan -> loan.getBook().getId().equals(book.getId()))
                .forEach(loanRepository::delete);
        bookRepository.deleteById(book.getId());
        publisherRepository.deleteById(publisher.getId());
        userRepository.deleteById(user.getId());
    }

    @Test
    void failedLoanPersistenceRollsBackTheStockChange() {
        doThrow(new IllegalStateException("Simulated persistence failure"))
                .when(loanRepository).save(any(Loan.class));

        assertThrows(
                IllegalStateException.class,
                () -> loanService.create(request())
        );

        Book persistedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(1, persistedBook.getQuantity());
        assertEquals(BookStatus.AVAILABLE, persistedBook.getStatus());
        assertEquals(0, loanRepository.findAll().stream()
                .filter(loan -> loan.getBook().getId().equals(book.getId()))
                .count());
    }

    @Test
    void simultaneousRequestsCannotBorrowTheLastCopyTwice() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> tryToBorrow(ready, start));
            var second = executor.submit(() -> tryToBorrow(ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            int successfulLoans = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);
            assertEquals(1, successfulLoans);
            assertEquals(0, bookRepository.findById(book.getId())
                    .orElseThrow().getQuantity());
            assertEquals(BookStatus.RENTED, bookRepository.findById(book.getId())
                    .orElseThrow().getStatus());
        } finally {
            start.countDown();
        }
    }

    private boolean tryToBorrow(CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            loanService.create(request());
            return true;
        } catch (ConflictException exception) {
            return false;
        }
    }

    private LoanRequest request() {
        LoanRequest request = new LoanRequest();
        request.setUserId(user.getId());
        request.setBookId(book.getId());
        request.setLoanDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));
        return request;
    }
}
