package com.altis.library.publishers.services;

import com.altis.library.exceptions.ConflictException;
import com.altis.library.exceptions.ResourceNotFoundException;
import com.altis.library.mappers.PublisherMapper;
import com.altis.library.publishers.models.entities.Publisher;
import com.altis.library.publishers.models.dtos.PublisherRequest;
import com.altis.library.publishers.models.dtos.PublisherResponse;
import com.altis.library.publishers.repositories.PublisherRepository;
import com.altis.library.books.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.altis.library.publishers.repositories.PublisherSpecification;

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
    public Page<PublisherResponse> findAll(
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                resolveSort(sortBy, sortDirection)
        );

        Specification<Publisher> publisherSpecification =
                PublisherSpecification.searchSpecification(search);

        return publisherRepository.findAll(publisherSpecification, pageable)
                .map(this::convertToResponse);
    }

    private Sort resolveSort(String sortBy, String sortDirection) {
        String property = switch (sortBy == null ? "" : sortBy.trim()) {
            case "id", "name", "email", "city", "state", "status" ->
                    sortBy.trim();
            default -> throw new IllegalArgumentException(
                    "Campo de ordenação inválido."
            );
        };

        Sort.Direction direction;
        try {
            direction = Sort.Direction.valueOf(sortDirection.trim().toUpperCase());
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "A direção da ordenação deve ser ASC ou DESC."
            );
        }

        return Sort.by(direction, property);
    }

    // find by id
    public PublisherResponse findById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Editora não encontrada."));

        return convertToResponse(publisher);
    }

    // save
    public PublisherResponse save(PublisherRequest request) {

        if (publisherRepository.existsByName(request.getName())) {
            throw new ConflictException(
                    "O nome da editora informado já está cadastrado."
            );
        }

        if (publisherRepository.existsByCnpj(request.getCnpj())) {
            throw new ConflictException("O CNPJ informado já está cadastrado.");
        }

        Publisher publisher = publisherMapper.toEntity(request);

        Publisher savedPublisher = publisherRepository.save(publisher);

        return convertToResponse(savedPublisher);
    }

    // update
    public PublisherResponse update(Long id, PublisherRequest request) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Editora não encontrada."));

        if (publisherRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException(
                    "O nome da editora informado já está cadastrado."
            );
        }

        if (publisherRepository.existsByCnpjAndIdNot(request.getCnpj(), id)) {
            throw new ConflictException("O CNPJ informado já está cadastrado.");
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
        publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Editora não encontrada."
                ));
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
