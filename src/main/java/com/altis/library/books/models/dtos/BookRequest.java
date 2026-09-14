package com.altis.library.books.models.dtos;

import com.altis.library.books.models.enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest {
    private String title;
    private String author;
    private Long publisherId;
    private BookStatus status;
    private String observations;
}
