package com.altis.library.publishers.services;

import com.altis.library.books.repositories.BookRepository;
import com.altis.library.exceptions.ConflictException;
import com.altis.library.exceptions.ResourceNotFoundException;
import com.altis.library.mappers.PublisherMapper;
import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.repositories.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublisherServiceTest {

    private static final Long PUBLISHER_ID = 1L;
    private static final String BOOKS_LINKED_MESSAGE =
            "A editora não pode ser excluída porque possui livros vinculados.";

    private PublisherRepository publisherRepository;
    private BookRepository bookRepository;
    private PublisherService publisherService;

    @BeforeEach
    void setUp() {
        publisherRepository = mock(PublisherRepository.class);
        bookRepository = mock(BookRepository.class);
        publisherService = new PublisherService(
                publisherRepository,
                bookRepository,
                mock(PublisherMapper.class)
        );
    }

    @Test
    void deleteAllowsPublisherWithoutBooks() {
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.of(publisher()));
        when(bookRepository.countByPublisherId(PUBLISHER_ID)).thenReturn(0L);

        publisherService.delete(PUBLISHER_ID);

        verify(bookRepository).countByPublisherId(PUBLISHER_ID);
        verify(publisherRepository).deleteById(PUBLISHER_ID);
    }

    @Test
    void deleteRejectsPublisherWhenAnyBookIsLinkedRegardlessOfBookStatus() {
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.of(publisher()));
        when(bookRepository.countByPublisherId(PUBLISHER_ID)).thenReturn(1L);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> publisherService.delete(PUBLISHER_ID)
        );

        assertEquals(BOOKS_LINKED_MESSAGE, exception.getMessage());
        verify(bookRepository).countByPublisherId(PUBLISHER_ID);
        verify(publisherRepository, never()).deleteById(PUBLISHER_ID);
    }

    @Test
    void deletePreservesNotFoundWhenPublisherDoesNotExist() {
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> publisherService.delete(PUBLISHER_ID)
        );

        verify(bookRepository, never()).countByPublisherId(PUBLISHER_ID);
        verify(publisherRepository, never()).deleteById(PUBLISHER_ID);
    }

    @Test
    void deleteTranslatesDatabaseIntegrityViolationToConflict() {
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.of(publisher()));
        when(bookRepository.countByPublisherId(PUBLISHER_ID)).thenReturn(0L);
        doThrow(new DataIntegrityViolationException("foreign key"))
                .when(publisherRepository).deleteById(PUBLISHER_ID);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> publisherService.delete(PUBLISHER_ID)
        );

        assertEquals(BOOKS_LINKED_MESSAGE, exception.getMessage());
    }

    private Publisher publisher() {
        return new Publisher("Publisher", "publisher@example.com", "123", "City", "ST");
    }
}
