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
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @Enumerated(EnumType.STRING)
    private BookStatus status; //vai definir o status do livro
    private String observations;

    protected Book() {
    }

    public Book(String title, String author, Publisher publisher, Integer quantity, String observations) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        setQuantity(quantity);
        this.observations = observations;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException(
                    "A quantidade disponível não pode ser negativa."
            );
        }

        this.quantity = quantity;
        this.status = quantity > 0 ? BookStatus.AVAILABLE : BookStatus.RENTED;
    }
}