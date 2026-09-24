package com.altis.library.loans.services;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.exceptions.ConflictException;
import com.altis.library.exceptions.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.altis.library.loans.repositories.LoanSpecification;

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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado."));

        long activeLoans = loanRepository.countByUserIdAndStatus(
                user.getId(),
                LoanStatus.PENDING
        );

        if (activeLoans >= 5) {
            throw new ConflictException(
                    "O usuário atingiu o limite de empréstimos ativos."
            );
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Livro não encontrado."));

        if (book.getQuantity() <= 0) {
            throw new ConflictException(
                    "O livro não possui exemplares disponíveis."
            );
        }

        if (request.getReturnDate().isBefore(request.getLoanDate())) {
            throw new IllegalArgumentException(
                    "A data de devolução não pode ser anterior à data do empréstimo."
            );
        }

        Loan loan = loanMapper.toEntity(request, user, book);

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);

        return loanMapper.toResponse(savedLoan);
    }

    // getAll
    public Page<LoanResponse> getAll(
            String name,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                resolveSort(sortBy, sortDirection)
        );

        Specification<Loan> loanSpecification =
                LoanSpecification.searchSpecification(name);

        return loanRepository.findAll(loanSpecification, pageable)
                .map(loanMapper::toResponse);
    }

    public Page<LoanResponse> getMyLoans(
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                resolveSort(sortBy, sortDirection)
        );

        return loanRepository.findByUserId(user.getId(), pageable)
                .map(loanMapper::toResponse);
    }

    private Sort resolveSort(String sortBy, String sortDirection) {
        String property = switch (sortBy == null ? "" : sortBy.trim()) {
            case "id", "loanDate", "returnDate", "returnedAt", "status" ->
                    sortBy.trim();
            default -> throw new IllegalArgumentException(
                    "Campo de ordenação inválido."
            );
        };

        Sort.Direction direction;
        try {
            direction = Sort.Direction.valueOf(sortDirection.trim().toUpperCase());
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "A direção da ordenação deve ser ASC ou DESC."
            );
        }

        return Sort.by(direction, property);
    }

    // getById
    public LoanResponse getById(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Empréstimo não encontrado."));

        return loanMapper.toResponse(loan);
    }

    // update
    public LoanResponse update(Long id, LoanRequest request) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Empréstimo não encontrado."));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new ConflictException(
                    "Empréstimos devolvidos não podem ser alterados."
            );
        }

        if (request.getReturnDate().isBefore(request.getLoanDate())) {
            throw new IllegalArgumentException(
                    "A data de devolução não pode ser anterior à data do empréstimo."
            );
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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Empréstimo não encontrado."));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new ConflictException("O empréstimo já foi devolvido.");
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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Empréstimo não encontrado."));

        if (loan.getStatus() == LoanStatus.PENDING) {
            Book book = loan.getBook();

            book.setQuantity(book.getQuantity() + 1);
            bookRepository.save(book);
        }

        loanRepository.delete(loan);
    }

}