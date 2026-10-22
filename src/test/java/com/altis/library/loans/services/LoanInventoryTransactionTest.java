package com.altis.library.loans.services;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.models.enums.BookStatus;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.books.repositories.BookRepository.BookStatusCount;
import com.altis.library.dashboard.models.dtos.DashboardResponse;
import com.altis.library.dashboard.services.DashboardService;
import com.altis.library.exceptions.ConflictException;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.enums.LoanStatus;
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
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    private DashboardService dashboardService;

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
    private String userSearchToken;
    private String bookSearchToken;

    @BeforeEach
    void createInventoryFixture() {
        String unique = UUID.randomUUID().toString();
        userSearchToken = unique.replace("-", "").substring(0, 8);
        bookSearchToken = unique.replace("-", "").substring(8, 16);
        publisher = publisherRepository.save(new Publisher(
                "Inventory test " + unique,
                unique + "@publisher.example",
                unique.replace("-", "").substring(0, 18),
                "City",
                "ST"
        ));
        user = userRepository.save(new User(
                "Borrower " + userSearchToken,
                unique + "@user.example",
                "password",
                "555-0100",
                unique.replace("-", "").substring(0, 14),
                LocalDate.of(2000, 1, 1),
                "Test address"
        ));
        book = bookRepository.save(new Book(
                "Catalog " + bookSearchToken,
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

    @Test
    void overdueStatusIsDerivedFromReturnDateAndReturnedStatusUsesActualDate() {
        DashboardResponse dashboardBefore = dashboardService.getDashboard();
        book.setQuantity(2);
        bookRepository.save(book);

        LoanRequest dueToday = request();
        dueToday.setReturnDate(LocalDate.now());
        LoanResponse onTime = loanService.create(dueToday);

        LoanRequest overdue = request();
        overdue.setLoanDate(LocalDate.now().minusDays(2));
        overdue.setReturnDate(LocalDate.now().minusDays(1));
        LoanResponse late = loanService.create(overdue);

        assertEquals(LoanStatus.PENDING, onTime.getStatus());
        assertEquals(LoanStatus.OVERDUE, late.getStatus());
        assertEquals(1, loanService.getAll(
                bookSearchToken,
                LoanStatus.OVERDUE,
                0,
                10,
                "id",
                "ASC"
        ).getTotalElements());

        DashboardResponse dashboardWithActiveLoans = dashboardService.getDashboard();
        assertEquals(dashboardBefore.getPendingLoans() + 1,
                dashboardWithActiveLoans.getPendingLoans());
        assertEquals(dashboardBefore.getOverdueLoans() + 1,
                dashboardWithActiveLoans.getOverdueLoans());

        assertEquals(
                LoanStatus.RETURNED,
                loanService.returnLoan(onTime.getId()).getStatus()
        );
        assertEquals(
                LoanStatus.RETURNED_LATE,
                loanService.returnLoan(late.getId()).getStatus()
        );

        DashboardResponse dashboardAfterReturns = dashboardService.getDashboard();
        assertEquals(dashboardBefore.getPendingLoans(),
                dashboardAfterReturns.getPendingLoans());
        assertEquals(dashboardBefore.getOverdueLoans(),
                dashboardAfterReturns.getOverdueLoans());
        assertEquals(dashboardBefore.getReturnedLoans() + 2,
                dashboardAfterReturns.getReturnedLoans());
    }

    @Test
    void searchMatchesBorrowerOrBookAndStatusFilterUsesDatabasePaging() {
        book.setQuantity(2);
        bookRepository.save(book);
        loanService.create(request());
        loanService.create(request());

        Page<LoanResponse> borrowerMatches = loanService.getAll(
                userSearchToken,
                LoanStatus.PENDING,
                0,
                1,
                "id",
                "ASC"
        );
        Page<LoanResponse> bookMatches = loanService.getAll(
                bookSearchToken,
                LoanStatus.PENDING,
                0,
                10,
                "id",
                "ASC"
        );

        assertEquals(2, borrowerMatches.getTotalElements());
        assertEquals(1, borrowerMatches.getNumberOfElements());
        assertEquals(2, bookMatches.getTotalElements());
        assertTrue(bookMatches.getContent().stream()
                .allMatch(loan -> loan.getStatus() == LoanStatus.PENDING));
    }

    @Test
    void fifthActiveLoanIsAllowedButSixthIsRejected() {
        book.setQuantity(6);
        bookRepository.save(book);

        for (int count = 0; count < 5; count++) {
            loanService.create(request());
        }

        assertThrows(ConflictException.class, () -> loanService.create(request()));
        assertEquals(1, bookRepository.findById(book.getId())
                .orElseThrow().getQuantity());
    }

    @Test
    void monthlyLoanCountsGroupByYearAndIncludeEveryStatus() {
        saveLoan(LocalDate.of(2024, 12, 15), LoanStatus.PENDING);
        saveLoan(LocalDate.of(2025, 1, 5), LoanStatus.OVERDUE);
        saveLoan(LocalDate.of(2025, 1, 20), LoanStatus.RETURNED_LATE);

        List<LoanRepository.MonthlyLoanCount> counts =
                loanRepository.countLoansByMonth(
                        LocalDate.of(2024, 12, 1),
                        LocalDate.of(2025, 2, 1)
                );

        assertEquals(2, counts.size());
        assertEquals(2024, counts.get(0).getYearValue());
        assertEquals(12, counts.get(0).getMonthValue());
        assertEquals(1, counts.get(0).getQuantity());
        assertEquals(2025, counts.get(1).getYearValue());
        assertEquals(1, counts.get(1).getMonthValue());
        assertEquals(2, counts.get(1).getQuantity());
    }

    @Test
    void bookCountsAreGroupedByBookStatus() {
        Map<BookStatus, Long> before = bookStatusCounts();
        book.setQuantity(0);
        bookRepository.save(book);
        Map<BookStatus, Long> after = bookStatusCounts();

        assertEquals(before.get(BookStatus.AVAILABLE) - 1,
                after.get(BookStatus.AVAILABLE));
        assertEquals(before.getOrDefault(BookStatus.RENTED, 0L) + 1,
                after.get(BookStatus.RENTED));
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

    private Loan saveLoan(LocalDate loanDate, LoanStatus status) {
        Loan loan = new Loan(user, book, loanDate, loanDate.plusDays(7), null);
        loan.setStatus(status);
        return loanRepository.save(loan);
    }

    private Map<BookStatus, Long> bookStatusCounts() {
        Map<BookStatus, Long> counts = new EnumMap<>(BookStatus.class);
        for (BookStatusCount count : bookRepository.countBooksByStatus()) {
            counts.put(count.getStatus(), count.getQuantity());
        }
        return counts;
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
