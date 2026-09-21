package com.altis.library.mappers;

import com.altis.library.publishers.models.dtos.PublisherRequest;
import com.altis.library.publishers.models.dtos.PublisherResponse;
import com.altis.library.publishers.models.entities.Publisher;
import org.springframework.stereotype.Component;

@Component
public class PublisherMapper {

    public PublisherResponse toResponse(Publisher publisher) {
        PublisherResponse response = new PublisherResponse();

        response.setId(publisher.getId());
        response.setName(publisher.getName());
        response.setEmail(publisher.getEmail());
        response.setCnpj(publisher.getCnpj());
        response.setCity(publisher.getCity());
        response.setState(publisher.getState());
        response.setStatus(publisher.getStatus());

        return response;
    }

    public Publisher toEntity(PublisherRequest request) {
        return new Publisher(
                request.getName(),
                request.getEmail(),
                request.getCnpj(),
                request.getCity(),
                request.getState()
        );
    }
}
