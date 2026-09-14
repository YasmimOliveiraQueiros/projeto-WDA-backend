package com.altis.library.books.models.entities;

import com.altis.library.publishers.models.entities.Publisher;
import jakarta.persistence.*;
import com.altis.library.books.models.enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;

    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @Enumerated(EnumType.STRING)
    private BookStatus status; //vai definir o status do livro
    private String observations;

    protected Book() {
    }

    public Book(String title, String author, Publisher publisher, String observations
    ) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.observations = observations;
        this.status = BookStatus.AVAILABLE;
    }
}