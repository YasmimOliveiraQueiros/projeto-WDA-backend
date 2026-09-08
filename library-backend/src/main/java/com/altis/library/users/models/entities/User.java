package com.altis.library.users.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
//futuramente um import Role

@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String phone; // não é long ou int por ter caracteres como: (+, - e (85) )
    private String cpf; // segue a mesma lógica do string phone
    private LocalDate birthDate;
    private String address;
    private boolean isAdmin;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



    //constructors

    public User() {
    }

    public User(Long id, String name, String email, String password, String phone, String cpf, LocalDate birthDate, String address, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.cpf = cpf;
        this.birthDate = birthDate;
        this.address = address;

    }


}
