package com.altis.library.mappers;

import com.altis.library.books.models.entities.Book;
import com.altis.library.loans.models.dtos.LoanRequest;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.users.models.entities.User;
import org.springframework.stereotype.Component;

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
        response.setStatus(loan.getStatus());
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
