package com.altis.library.books.models.dtos;

import com.altis.library.books.models.enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private Long publisherId;
    private Integer quantity;
    private BookStatus status;
    private String observations;
}
