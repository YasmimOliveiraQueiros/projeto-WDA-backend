package com.altis.library.books.models.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest {
    private String title;
    private String author;
    private Long publisherId;
    private Integer quantity;
    private String observations;
}
