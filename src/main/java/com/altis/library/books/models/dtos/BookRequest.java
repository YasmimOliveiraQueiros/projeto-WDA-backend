package com.altis.library.books.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest {

    @NotBlank(message = "Book title is required")
    private String title;

    @NotBlank(message = "Book author is required")
    private String author;

    @NotNull(message = "Publisher ID is required")
    private Long publisherId;

    @NotNull(message = "Book quantity is required")
    @Min(value = 1, message = "Book quantity must be at least 1")
    private Integer quantity;

    private String observations;
}