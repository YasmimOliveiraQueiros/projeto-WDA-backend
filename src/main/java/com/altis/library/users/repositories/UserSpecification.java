package com.altis.library.users.repositories;

import com.altis.library.users.models.entities.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> searchSpecification(String search) {
        Specification<User> specification = // cria uma especificação para filtrar usuários
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("isAdmin"), false); // garante que apenas usuários comuns sejam retornados, excluindo administradores

        if (search == null || search.isBlank()) {  // se não tiver pesquisa vai retornar apenas usuários comuns
            return specification;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%"; // pega o texto pesquisado para encontrar o termo em qualquer parte do campo, ignorando maiúsculas/minúsculas

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
