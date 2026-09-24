package com.altis.library.loans.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LoanRequest {

    @NotNull(message = "Usuário é obrigatório.")
    private Long userId;

    @NotNull(message = "Livro é obrigatório.")
    private Long bookId;

    @NotNull(message = "A data do empréstimo é obrigatória.")
    private LocalDate loanDate;

    @NotNull(message = "A data de devolução é obrigatória.")
    private LocalDate returnDate;

    private String observations;
}