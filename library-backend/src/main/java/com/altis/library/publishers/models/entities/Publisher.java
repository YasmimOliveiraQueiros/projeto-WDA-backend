package com.altis.library.publishers.models.entities;


import com.altis.library.publishers.models.enums.PublisherStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "publishers")
@Getter
@Setter
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String cnpj;

    private String city;

    private String state;

    @Enumerated(EnumType.STRING)
    private PublisherStatus status;
}
