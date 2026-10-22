package com.altis.library.loans.repositories;

import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class LoanSpecification {

    private LoanSpecification() {
    }

    public static Specification<Loan> searchSpecification(
            String name,
            LoanStatus status,
            LocalDate today) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                String searchPattern = "%" + name.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.join("user").get("name")),
                                searchPattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.join("book").get("title")),
                                searchPattern
                        )
                ));
            }

            if (status != null) {
                switch (status) {
                    case PENDING -> predicates.add(criteriaBuilder.and(
                            root.get("status").in(LoanStatus.PENDING, LoanStatus.OVERDUE),
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("returnDate"),
                                    today
                            )
                    ));
                    case OVERDUE -> predicates.add(criteriaBuilder.and(
                            root.get("status").in(LoanStatus.PENDING, LoanStatus.OVERDUE),
                            criteriaBuilder.lessThan(root.get("returnDate"), today)
                    ));
                    case RETURNED, RETURNED_LATE ->
                            predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(
                            Predicate[]::new
                    ));
        };
    }
}
