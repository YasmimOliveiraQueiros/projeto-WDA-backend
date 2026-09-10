package com.altis.library.publishers.repositories;

import com.altis.library.publishers.models.entities.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
}