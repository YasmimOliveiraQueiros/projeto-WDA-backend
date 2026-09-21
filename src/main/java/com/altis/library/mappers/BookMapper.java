package com.altis.library.mappers;

import com.altis.library.books.models.dtos.BookRequest;
import com.altis.library.books.models.dtos.BookResponse;
import com.altis.library.books.models.entities.Book;
import com.altis.library.publishers.models.entities.Publisher;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        BookResponse response = new BookResponse();

        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setPublisherId(book.getPublisher().getId());
        response.setQuantity(book.getQuantity());
        response.setStatus(book.getStatus());
        response.setObservations(book.getObservations());

        return response;
    }

    public Book toEntity(BookRequest request, Publisher publisher) {
        return new Book(
                request.getTitle(),
                request.getAuthor(),
                publisher,
                request.getQuantity(),
                request.getObservations()
        );
    }
}
