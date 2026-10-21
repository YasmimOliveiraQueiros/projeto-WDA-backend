package com.altis.library.books.services;

import com.altis.library.books.models.dtos.BookRequest;
import com.altis.library.books.models.dtos.BookResponse;
import com.altis.library.books.models.entities.Book;
import com.altis.library.books.models.enums.BookStatus;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.loans.repositories.LoanRepository;
import com.altis.library.mappers.BookMapper;
import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.repositories.PublisherRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceInventoryTest {

    private static final Long BOOK_ID = 1L;
    private static final Long PUBLISHER_ID = 2L;

    private BookRepository bookRepository;
    private PublisherRepository publisherRepository;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        publisherRepository = mock(PublisherRepository.class);
        bookService = new BookService(
                bookRepository,
                mock(LoanRepository.class),
                publisherRepository,
                new BookMapper()
        );
    }

    @Test
    void createWithZeroQuantitySetsRentedStatus() {
        Publisher publisher = publisher();
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.of(publisher));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.create(request(0));

        assertEquals(0, response.getQuantity());
        assertEquals(BookStatus.RENTED, response.getStatus());
    }

    @Test
    void updateWithZeroQuantitySetsRentedStatus() {
        Publisher publisher = publisher();
        Book book = new Book("Book", "Author", publisher, 3, null);
        book.setId(BOOK_ID);
        when(bookRepository.findByIdForUpdate(BOOK_ID))
                .thenReturn(Optional.of(book));
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.of(publisher));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.update(BOOK_ID, request(0));

        assertEquals(0, response.getQuantity());
        assertEquals(BookStatus.RENTED, response.getStatus());
        verify(bookRepository).findByIdForUpdate(BOOK_ID);
    }

    @Test
    void negativeQuantityIsRejectedByRequestValidationAndEntity() {
        BookRequest request = request(-1);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertEquals(
                    "quantity",
                    validator.validate(request).iterator().next()
                            .getPropertyPath().toString()
            );
        }

        Book book = new Book("Book", "Author", publisher(), 1, null);
        assertThrows(IllegalArgumentException.class, () -> book.setQuantity(-1));
        assertEquals(1, book.getQuantity());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    private BookRequest request(int quantity) {
        BookRequest request = new BookRequest();
        request.setTitle("Book");
        request.setAuthor("Author");
        request.setPublisherId(PUBLISHER_ID);
        request.setQuantity(quantity);
        return request;
    }

    private Publisher publisher() {
        return new Publisher("Publisher", "publisher@example.com", "123", "City", "ST");
    }
}
