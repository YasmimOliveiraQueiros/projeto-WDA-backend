package com.altis.library.users.repositories;

import com.altis.library.users.models.entities.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> searchSpecification(String search) {
        Specification<User> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("isAdmin"), false);

        if (search == null || search.isBlank()) {
            return specification;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        return specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("name")),
                                        searchPattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("email")),
                                        searchPattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("phone")),
                                        searchPattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("cpf")),
                                        searchPattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("address")),
                                        searchPattern
                                )
                        )
        );
    }
}
