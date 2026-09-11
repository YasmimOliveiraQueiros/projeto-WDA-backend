package com.altis.library.publishers.models.dtos;

import com.altis.library.publishers.models.enums.PublisherStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublisherResponse {

    private Long id;
    private String name;
    private String email;
    private String cnpj;
    private String city;
    private String state;
    private PublisherStatus status;
}