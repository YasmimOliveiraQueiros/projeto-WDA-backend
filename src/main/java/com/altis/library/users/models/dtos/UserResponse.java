package com.altis.library.users.models.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String cpf;
    private LocalDate birthDate;
    private String address;
    private boolean isAdmin;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public UserResponse() {
    }
}