package com.altis.library.loans.models.entities;

import com.altis.library.books.models.entities.Book;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.users.models.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate loanDate;

    @Column(nullable = false)
    private LocalDate returnDate;
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;
    private String observations;

    //construtor
    protected Loan() {
    }

    public Loan(User user, Book book, LocalDate loanDate, LocalDate returnDate, String observations) {
        this.user = user;
        this.book = book;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.observations = observations;
        this.status = LoanStatus.PENDING;
    }
}