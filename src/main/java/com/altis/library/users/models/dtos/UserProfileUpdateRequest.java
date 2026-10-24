package com.altis.library.users.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres.")
    private String name;

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres.")
    private String email;

    @NotBlank(message = "Telefone é obrigatório.")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
    private String phone;

    @NotBlank(message = "Endereço é obrigatório.")
    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres.")
    private String address;
}
