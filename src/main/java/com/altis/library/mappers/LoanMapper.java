package com.altis.library.mappers;

import com.altis.library.books.models.entities.Book;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.users.models.entities.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LoanMapper {

    public LoanResponse toResponse(Loan loan) {
        LoanResponse response = new LoanResponse();

        response.setId(loan.getId());
        response.setUserId(loan.getUser().getId());
        response.setBookId(loan.getBook().getId());
        response.setLoanDate(loan.getLoanDate());
        response.setReturnDate(loan.getReturnDate());
        response.setReturnedAt(loan.getReturnedAt());
        LoanStatus status = loan.getStatus();
        if (status == LoanStatus.PENDING || status == LoanStatus.OVERDUE) {
            status = loan.getReturnDate().isBefore(LocalDate.now())
                    ? LoanStatus.OVERDUE
                    : LoanStatus.PENDING;
        }
        response.setStatus(status);
        response.setObservations(loan.getObservations());

        return response;
    }

    public Loan toEntity(LoanRequest request, User user, Book book) {
        return new Loan(
                user,
                book,
                request.getLoanDate(),
                request.getReturnDate(),
                request.getObservations()
        );
    }
}
