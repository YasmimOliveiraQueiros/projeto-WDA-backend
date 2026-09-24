package com.altis.library.books.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest {

    @NotBlank(message = "Título é obrigatório.")
    private String title;

    @NotBlank(message = "Autor é obrigatório.")
    private String author;

    @NotNull(message = "Editora é obrigatória.")
    private Long publisherId;

    @NotNull(message = "Quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade deve ser maior ou igual a 1.")
    private Integer quantity;

    private String observations;
}