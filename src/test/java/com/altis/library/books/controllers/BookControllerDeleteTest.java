package com.altis.library.books.controllers;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.books.services.BookService;
import com.altis.library.exceptions.GlobalExceptionHandler;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.mappers.BookMapper;
import com.altis.library.publishers.repositories.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerDeleteTest {

    private static final Long BOOK_ID = 1L;
    private static final String LINKED_LOANS_MESSAGE =
            "Não é possível excluir este livro porque existem empréstimos vinculados a ele.";

    private BookRepository bookRepository;
    private LoanRepository loanRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        loanRepository = mock(LoanRepository.class);
        BookService bookService = new BookService(
                bookRepository,
                loanRepository,
                mock(PublisherRepository.class),
                mock(BookMapper.class)
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BookController(bookService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deleteWithAnyLinkedLoanReturnsConflictAndApiErrorFormat() throws Exception {
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book()));
        when(loanRepository.existsByBook_Id(BOOK_ID)).thenReturn(true);

        mockMvc.perform(delete("/books/{id}", BOOK_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(LINKED_LOANS_MESSAGE))
                .andExpect(jsonPath("$.path").value("/books/1"));

        verify(bookRepository, never()).delete(org.mockito.ArgumentMatchers.any(Book.class));
    }

    @Test
    void deleteWithoutLinkedLoansReturnsNoContent() throws Exception {
        Book book = book();
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book));
        when(loanRepository.existsByBook_Id(BOOK_ID)).thenReturn(false);

        mockMvc.perform(delete("/books/{id}", BOOK_ID))
                .andExpect(status().isNoContent());

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteOfMissingBookStillReturnsNotFound() throws Exception {
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/books/{id}", BOOK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/books/1"));

        verify(loanRepository, never()).existsByBook_Id(BOOK_ID);
    }

    private Book book() {
        return new Book("Book", "Author", null, 1, null);
    }
}
