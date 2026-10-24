package com.altis.library.users.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {

    @NotBlank(message = "Senha atual é obrigatória.")
    private String currentPassword;

    @NotBlank(message = "Nova senha é obrigatória.")
    @Size(min = 8, message = "A nova senha deve ter pelo menos 8 caracteres.")
    private String newPassword;

    @NotBlank(message = "Confirmação de senha é obrigatória.")
    private String confirmPassword;
}
