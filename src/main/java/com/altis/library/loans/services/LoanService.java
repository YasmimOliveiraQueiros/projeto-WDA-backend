package com.altis.library.loans.services;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import com.altis.library.loans.models.enums.LoanStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository, BookRepository bookRepository) {

        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // create
    public LoanResponse create(LoanRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        long activeLoans = loanRepository.countByUserIdAndStatus(
                user.getId(),
                LoanStatus.PENDING
        );

        if (activeLoans >= 5) {
            throw new RuntimeException("User has reached the maximum of 5 active loans");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getQuantity() <= 0) {
            throw new RuntimeException("Book is unavailable");
        }

        Loan loan = new Loan(user, book, request.getLoanDate(), request.getReturnDate(), request.getObservations()
        );

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);

        return toResponse(savedLoan);
    }

    // getAll
    public List<LoanResponse> getAll() {

        List<Loan> loans = loanRepository.findAll();

        return loans.stream()
                .map(this::toResponse)
                .toList();
    }

    // getById
    public LoanResponse getById(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        return toResponse(loan);
    }

    // update
    public LoanResponse update(Long id, LoanRequest request) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(request.getLoanDate());
        loan.setReturnDate(request.getReturnDate());
        loan.setObservations(request.getObservations());

        Loan updatedLoan = loanRepository.save(loan);

        return toResponse(updatedLoan);
    }

    // delete
    public void delete(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loanRepository.delete(loan);
    }

    private LoanResponse toResponse(Loan loan) {

        LoanResponse response = new LoanResponse();

        response.setId(loan.getId());
        response.setUserId(loan.getUser().getId());
        response.setBookId(loan.getBook().getId());
        response.setLoanDate(loan.getLoanDate());
        response.setReturnDate(loan.getReturnDate());
        response.setReturnedAt(loan.getReturnedAt());
        response.setStatus(loan.getStatus());
        response.setObservations(loan.getObservations());

        return response;
    }
}