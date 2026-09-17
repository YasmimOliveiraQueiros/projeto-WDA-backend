package com.altis.library.publishers.repositories;

import com.altis.library.publishers.models.entities.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    boolean existsByName(String name);
    boolean existsByCnpj(String cnpj);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}