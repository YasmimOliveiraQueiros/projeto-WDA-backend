package com.altis.library.publishers.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublisherRequest {

    @NotBlank(message = "Nome é obrigatório.")
    private String name;

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotBlank(message = "CNPJ é obrigatório.")
    private String cnpj;

    @NotBlank(message = "Cidade é obrigatória.")
    private String city;

    @NotBlank(message = "Estado é obrigatório.")
    private String state;
}