package com.altis.library.publishers.services;

import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.models.dtos.PublisherRequest;
import com.altis.library.publishers.models.dtos.PublisherResponse;
import com.altis.library.publishers.repositories.PublisherRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    //find all
    public List<PublisherResponse> findAll() {
        return publisherRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    //find by id
    public PublisherResponse findById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow();

        return convertToResponse(publisher);
    }

    //save
    public PublisherResponse save(PublisherRequest request) {

        Publisher publisher = new Publisher(
                request.getName(),
                request.getEmail(),
                request.getCnpj(),
                request.getCity(),
                request.getState()
        );

        Publisher savedPublisher = publisherRepository.save(publisher);

        return convertToResponse(savedPublisher);
    }
    //update
    public PublisherResponse update(Long id, PublisherRequest request) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow();

        publisher.setName(request.getName());
        publisher.setEmail(request.getEmail());
        publisher.setCnpj(request.getCnpj());
        publisher.setCity(request.getCity());
        publisher.setState(request.getState());

        Publisher updatedPublisher = publisherRepository.save(publisher);

        return convertToResponse(updatedPublisher);
    }

    //delete
    public void delete(Long id) {
        publisherRepository.deleteById(id);
    }

    private PublisherResponse convertToResponse(Publisher publisher) {

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
}