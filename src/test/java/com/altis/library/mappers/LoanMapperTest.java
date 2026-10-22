package com.altis.library.mappers;

import com.altis.library.books.models.entities.Book;
import com.altis.library.loans.models.dtos.LoanResponse;
import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import com.altis.library.users.models.entities.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanMapperTest {

    @Test
    void mapsBookTitleToLoanResponse() {
        User user = new User(
                "User",
                "user@example.com",
                "password",
                "555-0100",
                "123",
                LocalDate.of(2000, 1, 1),
                "Address"
        );
        Book book = new Book("The Hobbit", "J. R. R. Tolkien", null, 1, null);
        Loan loan = new Loan(
                user,
                book,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null
        );
        loan.setStatus(LoanStatus.PENDING);

        LoanResponse response = new LoanMapper().toResponse(loan);

        assertEquals("The Hobbit", response.getBookTitle());
        assertEquals(book.getId(), response.getBookId());
    }
}
