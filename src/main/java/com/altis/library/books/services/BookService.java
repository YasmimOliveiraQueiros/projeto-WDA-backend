package com.altis.library.books.services;

import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.books.repositories.BookRepository;
import com.altis.library.books.models.dtos.BookRequest;
import com.altis.library.books.models.dtos.BookResponse;
import com.altis.library.books.models.entities.Book;
import com.altis.library.books.models.enums.BookStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    //consulta a editora pelo id
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;

    public BookService(
            BookRepository bookRepository,
            PublisherRepository publisherRepository) {

        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
    }

    //create
    public BookResponse create(BookRequest request) {

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        Book book = new Book(request.getTitle(), request.getAuthor(), publisher, request.getObservations());

        Book savedBook = bookRepository.save(book);

        return toResponse(savedBook);
    }

    //getAll
    public List<BookResponse> getAll() {

        List<Book> books = bookRepository.findAll();

        return books.stream()
                .map(this::toResponse)
                .toList();
    }

    //get pelo id
    public BookResponse getById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return toResponse(book);
    }

    //update
    public BookResponse update(Long id, BookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(publisher);
        book.setObservations(request.getObservations());

        Book updatedBook = bookRepository.save(book);

        return toResponse(updatedBook);
    }

    //delete
    public void delete(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookRepository.delete(book);
    }

    private BookResponse toResponse(Book book) {

        BookResponse response = new BookResponse();

        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setPublisherId(book.getPublisher().getId());
        response.setStatus(book.getStatus());
        response.setObservations(book.getObservations());

        return response;
    }

}