package com.altis.library.dashboard.services;

import com.altis.library.books.models.enums.BookStatus;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.dashboard.models.dtos.DashboardResponse;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.mappers.LoanMapper;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.users.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private UserRepository userRepository;
    private BookRepository bookRepository;
    private PublisherRepository publisherRepository;
    private LoanRepository loanRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        bookRepository = mock(BookRepository.class);
        publisherRepository = mock(PublisherRepository.class);
        loanRepository = mock(LoanRepository.class);
        dashboardService = new DashboardService(
                userRepository,
                bookRepository,
                publisherRepository,
                loanRepository,
                mock(LoanMapper.class)
        );

        when(userRepository.countByIsAdminFalse()).thenReturn(12L);
        when(bookRepository.count()).thenReturn(8L);
        when(publisherRepository.count()).thenReturn(3L);
        when(loanRepository.countActiveDueLoans(anyList(), any(LocalDate.class)))
                .thenReturn(5L);
        when(loanRepository.countOverdueLoans(anyList(), any(LocalDate.class)))
                .thenReturn(2L);
        when(loanRepository.countByStatus(LoanStatus.RETURNED)).thenReturn(4L);
        when(loanRepository.countByStatus(LoanStatus.RETURNED_LATE)).thenReturn(1L);
        when(loanRepository.findTop5ByOrderByLoanDateDesc()).thenReturn(List.of());
    }

    @Test
    void monthlyCountsIncludeYearBoundaryAndMissingMonthsInOrder() {
        List<LoanRepository.MonthlyLoanCount> counts = List.of(
                monthlyCount(2024, 8, 2),
                monthlyCount(2025, 1, 3)
        );

        List<DashboardResponse.LoansByMonth> result =
                DashboardService.buildLoansByMonth(YearMonth.of(2025, 1), counts);

        assertEquals(List.of(
                new DashboardResponse.LoansByMonth(2024, 8, 2),
                new DashboardResponse.LoansByMonth(2024, 9, 0),
                new DashboardResponse.LoansByMonth(2024, 10, 0),
                new DashboardResponse.LoansByMonth(2024, 11, 0),
                new DashboardResponse.LoansByMonth(2024, 12, 0),
                new DashboardResponse.LoansByMonth(2025, 1, 3)
        ), result);
    }

    @Test
    void dashboardPreservesExistingFieldsAndAddsBookStatusCounts() {
        List<BookRepository.BookStatusCount> counts = List.of(
                bookCount(BookStatus.AVAILABLE, 6),
                bookCount(BookStatus.RENTED, 2)
        );
        when(bookRepository.countBooksByStatus()).thenReturn(counts);
        when(loanRepository.countLoansByMonth(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(12, response.getTotalUsers());
        assertEquals(8, response.getTotalBooks());
        assertEquals(3, response.getTotalPublishers());
        assertEquals(7, response.getActiveLoans());
        assertEquals(5, response.getPendingLoans());
        assertEquals(2, response.getOverdueLoans());
        assertEquals(5, response.getReturnedLoans());
        assertEquals(List.of(), response.getLatestLoans());
        assertEquals(new DashboardResponse.BooksByStatus(6, 2),
                response.getBooksByStatus());
        assertEquals(6, response.getLoansByMonth().size());
        assertEquals(0, response.getLoansByMonth().stream()
                .mapToLong(DashboardResponse.LoansByMonth::quantity)
                .sum());

        YearMonth currentMonth = YearMonth.now();
        verify(loanRepository).countLoansByMonth(
                currentMonth.minusMonths(5).atDay(1),
                currentMonth.plusMonths(1).atDay(1)
        );
    }

    @Test
    void dashboardReturnsZeroForBookStatusWithoutRecords() {
        List<BookRepository.BookStatusCount> counts =
                List.of(bookCount(BookStatus.AVAILABLE, 6));
        when(bookRepository.countBooksByStatus())
                .thenReturn(counts);
        when(loanRepository.countLoansByMonth(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(new DashboardResponse.BooksByStatus(6, 0),
                response.getBooksByStatus());
    }

    private LoanRepository.MonthlyLoanCount monthlyCount(
            int year,
            int month,
            long quantity) {
        LoanRepository.MonthlyLoanCount count =
                mock(LoanRepository.MonthlyLoanCount.class);
        when(count.getYearValue()).thenReturn(year);
        when(count.getMonthValue()).thenReturn(month);
        when(count.getQuantity()).thenReturn(quantity);
        return count;
    }

    private BookRepository.BookStatusCount bookCount(BookStatus status, long quantity) {
        BookRepository.BookStatusCount count = mock(BookRepository.BookStatusCount.class);
        when(count.getStatus()).thenReturn(status);
        when(count.getQuantity()).thenReturn(quantity);
        return count;
    }
}
