package com.altis.library.books.models.entities;

import jakarta.persistence.*;
import com.altis.library.books.models.enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
public class book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;


    @Enumerated(EnumType.STRING)
    private BookStatus status; //vai definir o status do livro
    private String observations;
}