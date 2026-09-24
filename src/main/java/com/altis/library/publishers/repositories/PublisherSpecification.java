package com.altis.library.publishers.repositories;

import com.altis.library.publishers.models.entities.Publisher;
import org.springframework.data.jpa.domain.Specification;

public final class PublisherSpecification {

    private PublisherSpecification() {
    }

    public static Specification<Publisher> searchSpecification(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("cnpj")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), searchPattern)
        );
    }
}
