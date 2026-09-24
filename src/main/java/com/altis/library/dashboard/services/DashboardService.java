package com.altis.library.dashboard.services;

import com.altis.library.books.repositories.BookRepository;
import com.altis.library.dashboard.models.dtos.DashboardResponse;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.mappers.LoanMapper;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
        long pendingLoans = loanRepository.countByStatus(LoanStatus.PENDING);
        long overdueLoans = loanRepository.countByStatus(LoanStatus.OVERDUE);
        long returnedLoans = loanRepository.countByStatus(LoanStatus.RETURNED)
                + loanRepository.countByStatus(LoanStatus.RETURNED_LATE);

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

        return response;
    }
}
