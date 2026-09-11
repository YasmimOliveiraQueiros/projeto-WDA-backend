package com.altis.library.users.models.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserRequest {

    private String name;
    private String email;
    private String password;
    private String phone;
    private String cpf;
    private LocalDate birthDate;
    private String address;

    public UserRequest() {
    }

}