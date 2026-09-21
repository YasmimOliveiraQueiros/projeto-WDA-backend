package com.altis.library.publishers.services;

import com.altis.library.mappers.PublisherMapper;
import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.models.dtos.PublisherRequest;
import com.altis.library.publishers.models.dtos.PublisherResponse;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.books.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.expression.ExpressionException;

import java.util.List;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final PublisherMapper publisherMapper;

    // mostra/puxa os títulos que existem vinculados a uma editora
    public PublisherService(
            PublisherRepository publisherRepository,
            BookRepository bookRepository,
            PublisherMapper publisherMapper) {
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
        this.publisherMapper = publisherMapper;
    }

    // find all
    public List<PublisherResponse> findAll() {
        return publisherRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // find by id
    public PublisherResponse findById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ExpressionException("Editora não encontrada"));

        return convertToResponse(publisher);
    }

    // save
    public PublisherResponse save(PublisherRequest request) {

        if (publisherRepository.existsByName(request.getName())) {
            throw new RuntimeException("Publisher name already exists");
        }

        if (publisherRepository.existsByCnpj(request.getCnpj())) {
            throw new RuntimeException("CNPJ already exists");
        }

        Publisher publisher = publisherMapper.toEntity(request);

        Publisher savedPublisher = publisherRepository.save(publisher);

        return convertToResponse(savedPublisher);
    }

    // update
    public PublisherResponse update(Long id, PublisherRequest request) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ExpressionException("Editora não encontrada"));

        if (publisherRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Publisher name already exists");
        }

        if (publisherRepository.existsByCnpjAndIdNot(request.getCnpj(), id)) {
            throw new RuntimeException("CNPJ already exists");
        }

        Publisher mappedPublisher = publisherMapper.toEntity(request);

        publisher.setName(mappedPublisher.getName());
        publisher.setEmail(mappedPublisher.getEmail());
        publisher.setCnpj(mappedPublisher.getCnpj());
        publisher.setCity(mappedPublisher.getCity());
        publisher.setState(mappedPublisher.getState());

        Publisher updatedPublisher = publisherRepository.save(publisher);

        return convertToResponse(updatedPublisher);
    }

    // delete
    public void delete(Long id) {
        publisherRepository.deleteById(id);
    }

    private PublisherResponse convertToResponse(Publisher publisher) {
        PublisherResponse response = publisherMapper.toResponse(publisher);

        response.setBookCount(
                bookRepository.countByPublisherId(publisher.getId())
        );

        return response;
    }
}