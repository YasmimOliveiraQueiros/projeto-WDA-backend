package com.altis.library.loans.repositories;

import com.altis.library.loans.models.entities.Loan;
import org.springframework.data.jpa.domain.Specification;

public final class LoanSpecification {

    private LoanSpecification() {
    }

    public static Specification<Loan> searchSpecification(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String searchPattern = "%" + name.trim().toLowerCase() + "%";

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("user").get("name")),
                        searchPattern
                );
    }
}
