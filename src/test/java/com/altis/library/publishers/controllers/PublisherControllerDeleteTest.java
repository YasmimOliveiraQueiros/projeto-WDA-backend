package com.altis.library.publishers.controllers;

import com.altis.library.books.repositories.BookRepository;
import com.altis.library.exceptions.GlobalExceptionHandler;
import com.altis.library.mappers.PublisherMapper;
import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.publishers.services.PublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublisherControllerDeleteTest {

    private static final Long PUBLISHER_ID = 1L;
    private static final String BOOKS_LINKED_MESSAGE =
            "A editora não pode ser excluída porque possui livros vinculados.";

    private PublisherRepository publisherRepository;
    private BookRepository bookRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        publisherRepository = mock(PublisherRepository.class);
        bookRepository = mock(BookRepository.class);
        PublisherService publisherService = new PublisherService(
                publisherRepository,
                bookRepository,
                mock(PublisherMapper.class)
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PublisherController(publisherService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deleteWithLinkedBooksReturnsConflictAndApiErrorFormat() throws Exception {
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.of(publisher()));
        when(bookRepository.countByPublisherId(PUBLISHER_ID)).thenReturn(1L);

        mockMvc.perform(delete("/publishers/{id}", PUBLISHER_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(BOOKS_LINKED_MESSAGE))
                .andExpect(jsonPath("$.path").value("/publishers/1"));
    }

    @Test
    void deleteWithoutLinkedBooksStillReturnsNoContent() throws Exception {
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.of(publisher()));
        when(bookRepository.countByPublisherId(PUBLISHER_ID)).thenReturn(0L);

        mockMvc.perform(delete("/publishers/{id}", PUBLISHER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOfMissingPublisherStillReturnsNotFound() throws Exception {
        when(publisherRepository.findById(PUBLISHER_ID))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/publishers/{id}", PUBLISHER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/publishers/1"));
    }

    private Publisher publisher() {
        return new Publisher("Publisher", "publisher@example.com", "123", "City", "ST");
    }
}
