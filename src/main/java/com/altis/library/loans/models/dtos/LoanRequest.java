package com.altis.library.loans.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LoanRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Loan date is required")
    private LocalDate loanDate;

    @NotNull(message = "Return date is required")
    private LocalDate returnDate;

    private String observations;
}