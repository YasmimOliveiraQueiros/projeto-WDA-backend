package com.altis.library.dashboard.services;

import com.altis.library.books.models.enums.BookStatus;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.dashboard.models.dtos.DashboardResponse;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.mappers.LoanMapper;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    public DashboardService(
            UserRepository userRepository,
            BookRepository bookRepository,
            PublisherRepository publisherRepository,
            LoanRepository loanRepository,
            LoanMapper loanMapper) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        List<LoanStatus> activeStatuses = List.of(
                LoanStatus.PENDING,
                LoanStatus.OVERDUE
        );
        long pendingLoans = loanRepository.countActiveDueLoans(
                activeStatuses,
                today
        );
        long overdueLoans = loanRepository.countOverdueLoans(
                activeStatuses,
                today
        );
        long returnedLoans = loanRepository.countByStatus(LoanStatus.RETURNED)
                + loanRepository.countByStatus(LoanStatus.RETURNED_LATE);

        YearMonth currentMonth = YearMonth.now();
        List<LoanRepository.MonthlyLoanCount> monthlyLoanCounts =
                loanRepository.countLoansByMonth(
                        currentMonth.minusMonths(5).atDay(1),
                        currentMonth.plusMonths(1).atDay(1)
                );
        List<DashboardResponse.LoansByMonth> loansByMonth =
                buildLoansByMonth(currentMonth, monthlyLoanCounts);

        long availableBooks = 0;
        long rentedBooks = 0;
        for (BookRepository.BookStatusCount bookCount : bookRepository.countBooksByStatus()) {
            if (bookCount.getStatus() == BookStatus.AVAILABLE) {
                availableBooks = bookCount.getQuantity();
            } else if (bookCount.getStatus() == BookStatus.RENTED) {
                rentedBooks = bookCount.getQuantity();
            }
        }

        List<LoanResponse> latestLoans = loanRepository.findTop5ByOrderByLoanDateDesc()
                .stream()
                .map(loanMapper::toResponse)
                .toList();

        DashboardResponse response = new DashboardResponse();
        response.setTotalUsers(userRepository.countByIsAdminFalse());
        response.setTotalBooks(bookRepository.count());
        response.setTotalPublishers(publisherRepository.count());
        response.setActiveLoans(pendingLoans + overdueLoans);
        response.setOverdueLoans(overdueLoans);
        response.setReturnedLoans(returnedLoans);
        response.setPendingLoans(pendingLoans);
        response.setLatestLoans(latestLoans);
        response.setLoansByMonth(loansByMonth);
        response.setBooksByStatus(new DashboardResponse.BooksByStatus(
                availableBooks,
                rentedBooks
        ));

        return response;
    }

    static List<DashboardResponse.LoansByMonth> buildLoansByMonth(
            YearMonth currentMonth,
            List<LoanRepository.MonthlyLoanCount> monthlyCounts) {
        Map<YearMonth, Long> countsByMonth = new HashMap<>();
        for (LoanRepository.MonthlyLoanCount count : monthlyCounts) {
            countsByMonth.put(
                    YearMonth.of(count.getYearValue(), count.getMonthValue()),
                    count.getQuantity()
            );
        }

        List<DashboardResponse.LoansByMonth> result = new ArrayList<>(6);
        for (int offset = 0; offset < 6; offset++) {
            YearMonth month = currentMonth.minusMonths(5L - offset);
            result.add(new DashboardResponse.LoansByMonth(
                    month.getYear(),
                    month.getMonthValue(),
                    countsByMonth.getOrDefault(month, 0L)
            ));
        }
        return List.copyOf(result);
    }
}
