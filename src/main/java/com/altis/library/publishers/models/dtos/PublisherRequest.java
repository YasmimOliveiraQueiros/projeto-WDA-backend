package com.altis.library.publishers.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublisherRequest {

    @NotBlank(message = "Publisher name is required")
    private String name;

    @NotBlank(message = "Publisher email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "CNPJ is required")
    private String cnpj;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;
}