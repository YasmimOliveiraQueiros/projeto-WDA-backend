package com.altis.library.books.repositories;

import com.altis.library.books.models.entities.Book;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecification {

    private BookSpecification() {
    }

    public static Specification<Book> searchSpecification(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("observations")), searchPattern)
        );
    }
}
