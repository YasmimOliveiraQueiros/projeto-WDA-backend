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
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String cnpj;
    private String city;
    private String state;

    @Enumerated(EnumType.STRING)
    private PublisherStatus status;

    protected Publisher() {
    }

    public Publisher(String name, String email, String cnpj, String city, String state) {
        this.name = name;
        this.email = email;
        this.cnpj = cnpj;
        this.city = city;
        this.state = state;
        this.status = PublisherStatus.ACTIVE;
    }
}
