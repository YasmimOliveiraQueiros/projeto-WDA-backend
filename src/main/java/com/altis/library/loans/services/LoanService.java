package com.altis.library.loans.services;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.mappers.LoanMapper;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import com.altis.library.loans.models.enums.LoanStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanMapper loanMapper;

    public LoanService(
            LoanRepository loanRepository,
            UserRepository userRepository,
            BookRepository bookRepository,
            LoanMapper loanMapper) {

        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanMapper = loanMapper;
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

        if (request.getReturnDate().isBefore(request.getLoanDate())) {
            throw new RuntimeException("Return date cannot be before loan date");
        }

        Loan loan = loanMapper.toEntity(request, user, book);

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);

        return loanMapper.toResponse(savedLoan);
    }

    // getAll
    public List<LoanResponse> getAll(String name) {

        List<Loan> loans = name == null || name.isBlank()
                ? loanRepository.findAll()
                : loanRepository.findByUser_NameContainingIgnoreCase(name);

        return loans.stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    public List<LoanResponse> getMyLoans() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<Loan> loans = loanRepository.findByUserId(user.getId());

        return loans.stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    // getById
    public LoanResponse getById(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        return loanMapper.toResponse(loan);
    }

    // update
    public LoanResponse update(Long id, LoanRequest request) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new RuntimeException("Returned loan cannot be updated");
        }

        if (request.getReturnDate().isBefore(request.getLoanDate())) {
            throw new RuntimeException("Return date cannot be before loan date");
        }

        loan.setLoanDate(request.getLoanDate());
        loan.setReturnDate(request.getReturnDate());
        loan.setObservations(request.getObservations());

        Loan updatedLoan = loanRepository.save(loan);

        return loanMapper.toResponse(updatedLoan);
    }


    // return - registra que o livro alugado pelo usuário foi devolvido
    public LoanResponse returnLoan(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new RuntimeException("Loan has already been returned");
        }

        Book book = loan.getBook();

        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(java.time.LocalDateTime.now());

        Loan returnedLoan = loanRepository.save(loan);

        return loanMapper.toResponse(returnedLoan);
    }


    // delete
    public void delete(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() == LoanStatus.PENDING) {
            Book book = loan.getBook();

            book.setQuantity(book.getQuantity() + 1);
            bookRepository.save(book);
        }

        loanRepository.delete(loan);
    }

}