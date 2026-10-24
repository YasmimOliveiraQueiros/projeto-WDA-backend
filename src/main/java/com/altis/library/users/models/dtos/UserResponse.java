package com.altis.library.users.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isAdmin")
    private Boolean isAdmin;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public UserResponse() {
    }
}