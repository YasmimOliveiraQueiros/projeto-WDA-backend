package com.altis.library.loans.models.dtos;

import com.altis.library.loans.models.enums.LoanStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class LoanResponse {

    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDate loanDate;
    private LocalDate returnDate;
    private LocalDateTime returnedAt;
    private LoanStatus status;
    private String observations;
}