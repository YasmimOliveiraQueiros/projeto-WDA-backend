package com.altis.library.loans.services;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.models.enums.BookStatus;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.exceptions.ConflictException;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.mappers.LoanMapper;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoanServiceInventoryTest {

    private static final Long BOOK_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long LOAN_ID = 3L;

    private LoanRepository loanRepository;
    private UserRepository userRepository;
    private BookRepository bookRepository;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanRepository = mock(LoanRepository.class);
        userRepository = mock(UserRepository.class);
        bookRepository = mock(BookRepository.class);
        loanService = new LoanService(
                loanRepository,
                userRepository,
                bookRepository,
                new LoanMapper()
        );
    }

    @Test
    void loanOfLastCopyChangesQuantityToZeroAndStatusToRented() {
        User user = user();
        Book book = book(1);
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
        when(loanRepository.countByUserIdAndStatusIn(
                USER_ID,
                java.util.List.of(LoanStatus.PENDING, LoanStatus.OVERDUE)
        ))
                .thenReturn(0L);
        when(bookRepository.findByIdForUpdate(BOOK_ID))
                .thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        loanService.create(request());

        assertEquals(0, book.getQuantity());
        assertEquals(BookStatus.RENTED, book.getStatus());
        verify(bookRepository).save(book);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void loanIsBlockedWhenNoCopiesAreAvailable() {
        User user = user();
        Book book = book(0);
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
        when(loanRepository.countByUserIdAndStatusIn(
                USER_ID,
                java.util.List.of(LoanStatus.PENDING, LoanStatus.OVERDUE)
        ))
                .thenReturn(0L);
        when(bookRepository.findByIdForUpdate(BOOK_ID))
                .thenReturn(Optional.of(book));

        assertThrows(ConflictException.class, () -> loanService.create(request()));

        assertEquals(BookStatus.RENTED, book.getStatus());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnRestoresQuantityAndAvailableStatus() {
        Book book = book(0);
        Loan loan = loan(book, LoanStatus.PENDING);
        when(loanRepository.findByIdForUpdate(LOAN_ID))
                .thenReturn(Optional.of(loan));
        when(bookRepository.findByIdForUpdate(BOOK_ID))
                .thenReturn(Optional.of(book));
        when(loanRepository.save(loan)).thenReturn(loan);

        loanService.returnLoan(LOAN_ID);

        assertEquals(1, book.getQuantity());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        verify(bookRepository).save(book);
    }

    @Test
    void inventoryOperationsAreTransactionalAndUsePessimisticLocks() throws Exception {
        assertTrue(LoanService.class
                .getMethod("create", LoanRequest.class)
                .isAnnotationPresent(Transactional.class));
        assertTrue(LoanService.class
                .getMethod("returnLoan", Long.class)
                .isAnnotationPresent(Transactional.class));
        assertTrue(com.altis.library.books.services.BookService.class
                .getMethod("update", Long.class, com.altis.library.books.models.dtos.BookRequest.class)
                .isAnnotationPresent(Transactional.class));
        assertTrue(com.altis.library.books.services.BookService.class
                .getMethod("delete", Long.class)
                .isAnnotationPresent(Transactional.class));

        Lock bookLock = BookRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        Lock loanLock = LoanRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, bookLock.value());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, loanLock.value());
        Lock userLock = UserRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, userLock.value());
    }

    @Test
    void updateRejectsChangingOriginalUserOrBook() {
        Book book = book(1);
        Loan loan = loan(book, LoanStatus.PENDING);
        when(loanRepository.findByIdForUpdate(LOAN_ID))
                .thenReturn(Optional.of(loan));

        LoanRequest differentBook = request();
        differentBook.setBookId(99L);
        ConflictException bookException = assertThrows(
                ConflictException.class,
                () -> loanService.update(LOAN_ID, differentBook)
        );
        assertEquals(
                "Não é permitido alterar o usuário ou o livro do empréstimo.",
                bookException.getMessage()
        );

        LoanRequest differentUser = request();
        differentUser.setUserId(99L);
        assertThrows(
                ConflictException.class,
                () -> loanService.update(LOAN_ID, differentUser)
        );
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void activeLoanCanBeEditedAndStatusIsDerivedFromReturnDate() {
        Loan loan = loan(book(0), LoanStatus.PENDING);
        Loan overdueLoan = loan(book(0), LoanStatus.OVERDUE);
        when(loanRepository.findByIdForUpdate(LOAN_ID))
                .thenReturn(Optional.of(loan), Optional.of(overdueLoan));
        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanRequest request = request();
        request.setLoanDate(LocalDate.now().minusDays(3));
        request.setReturnDate(LocalDate.now().minusDays(1));
        request.setObservations("Updated");

        var response = loanService.update(LOAN_ID, request);

        assertEquals(request.getLoanDate(), loan.getLoanDate());
        assertEquals(request.getReturnDate(), loan.getReturnDate());
        assertEquals("Updated", loan.getObservations());
        assertEquals(LoanStatus.OVERDUE, response.getStatus());

        LoanRequest futureDatesRequest = request();
        var futureDatesResponse = loanService.update(LOAN_ID, futureDatesRequest);

        assertEquals(futureDatesRequest.getLoanDate(), overdueLoan.getLoanDate());
        assertEquals(futureDatesRequest.getReturnDate(), overdueLoan.getReturnDate());
        assertEquals(LoanStatus.PENDING, futureDatesResponse.getStatus());
        verify(loanRepository, times(2)).save(any(Loan.class));
        verifyNoInteractions(bookRepository);
    }

    @Test
    void returnedLoanCanBeEditedAndStatusIsRecalculatedWithoutChangingReturnedAt() {
        LocalDateTime returnedAt = LocalDateTime.of(2025, 3, 15, 12, 30);
        Loan onTimeLoan = loan(book(0), LoanStatus.RETURNED);
        onTimeLoan.setReturnedAt(returnedAt);
        Loan lateLoan = loan(book(0), LoanStatus.RETURNED_LATE);
        lateLoan.setReturnedAt(returnedAt);
        when(loanRepository.findByIdForUpdate(LOAN_ID))
                .thenReturn(Optional.of(lateLoan), Optional.of(onTimeLoan));
        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanRequest lateRequest = request();
        lateRequest.setLoanDate(LocalDate.of(2025, 3, 1));
        lateRequest.setReturnDate(LocalDate.of(2025, 3, 14));
        lateRequest.setObservations("Updated late loan");
        var lateResponse = loanService.update(LOAN_ID, lateRequest);

        LoanRequest onTimeRequest = request();
        onTimeRequest.setLoanDate(LocalDate.of(2025, 3, 1));
        onTimeRequest.setReturnDate(LocalDate.of(2025, 3, 15));
        onTimeRequest.setObservations("Updated returned loan");
        var onTimeResponse = loanService.update(LOAN_ID, onTimeRequest);

        assertEquals(LoanStatus.RETURNED_LATE, lateResponse.getStatus());
        assertEquals(lateRequest.getLoanDate(), lateLoan.getLoanDate());
        assertEquals(lateRequest.getReturnDate(), lateLoan.getReturnDate());
        assertEquals(returnedAt, lateResponse.getReturnedAt());
        assertEquals("Updated late loan", lateLoan.getObservations());
        assertEquals(LoanStatus.RETURNED, onTimeResponse.getStatus());
        assertEquals(onTimeRequest.getLoanDate(), onTimeLoan.getLoanDate());
        assertEquals(onTimeRequest.getReturnDate(), onTimeLoan.getReturnDate());
        assertEquals(returnedAt, onTimeResponse.getReturnedAt());
        assertEquals("Updated returned loan", onTimeLoan.getObservations());
        verify(loanRepository, times(2)).save(any(Loan.class));
        verifyNoInteractions(bookRepository);
    }

    @Test
    void returnedLateLoanCannotBeReturnedAgain() {
        Loan loan = loan(book(0), LoanStatus.RETURNED_LATE);
        when(loanRepository.findByIdForUpdate(LOAN_ID))
                .thenReturn(Optional.of(loan));

        assertThrows(
                ConflictException.class,
                () -> loanService.returnLoan(LOAN_ID)
        );
    }

    private User user() {
        User user = new User(
                "User",
                "user@example.com",
                "password",
                "555-0100",
                "123",
                LocalDate.of(2000, 1, 1),
                "Address"
        );
        user.setId(USER_ID);
        return user;
    }

    private Book book(int quantity) {
        Book book = new Book("Book", "Author", null, quantity, null);
        book.setId(BOOK_ID);
        return book;
    }

    private Loan loan(Book book, LoanStatus status) {
        Loan loan = new Loan(user(), book, LocalDate.now(), LocalDate.now().plusDays(7), null);
        loan.setId(LOAN_ID);
        loan.setStatus(status);
        return loan;
    }

    private LoanRequest request() {
        LoanRequest request = new LoanRequest();
        request.setUserId(USER_ID);
        request.setBookId(BOOK_ID);
        request.setLoanDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));
        return request;
    }
}
