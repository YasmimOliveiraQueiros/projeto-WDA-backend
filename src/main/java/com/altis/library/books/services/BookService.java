package com.altis.library.books.services;

import com.altis.library.mappers.BookMapper;
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
    private final BookMapper bookMapper;

    public BookService(
            BookRepository bookRepository,
            PublisherRepository publisherRepository,
            BookMapper bookMapper) {

        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.bookMapper = bookMapper;
    }

    //create
    public BookResponse create(BookRequest request) {

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        Book book = bookMapper.toEntity(request, publisher);

        Book savedBook = bookRepository.save(book);

        return bookMapper.toResponse(savedBook);
    }

    //getAll
    public List<BookResponse> getAll() {

        List<Book> books = bookRepository.findAll();

        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    //get pelo id
    public BookResponse getById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return bookMapper.toResponse(book);
    }

    //update
    public BookResponse update(Long id, BookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        Book mappedBook = bookMapper.toEntity(request, publisher);

        book.setTitle(mappedBook.getTitle());
        book.setAuthor(mappedBook.getAuthor());
        book.setPublisher(mappedBook.getPublisher());
        book.setQuantity(mappedBook.getQuantity());
        book.setObservations(mappedBook.getObservations());

        Book updatedBook = bookRepository.save(book);

        return bookMapper.toResponse(updatedBook);
    }

    //delete
    public void delete(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookRepository.delete(book);
    }

}