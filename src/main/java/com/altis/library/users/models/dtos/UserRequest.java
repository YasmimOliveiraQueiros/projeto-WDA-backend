package com.altis.library.users.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "User name is required")
    private String name;

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "User password is required")
    private String password;

    @NotBlank(message = "User phone is required")
    private String phone;

    @NotBlank(message = "User CPF is required")
    private String cpf;

    @NotNull(message = "User birth date is required")
    private LocalDate birthDate;

    @NotBlank(message = "User address is required")
    private String address;

    public UserRequest() {
    }
}