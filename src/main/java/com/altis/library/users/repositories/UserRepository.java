package com.altis.library.users.repositories;

import com.altis.library.users.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByIsAdminTrue();
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByCpfAndIdNot(String cpf, Long id);

    User findByEmail(String email);
    User findByCpf(String cpf);
    Optional<User> findByEmailAndCpf(String email, String cpf);
    long countByIsAdminFalse();
    List<User> findByIsAdminFalse();
    Optional<User> findByIdAndIsAdminFalse(Long id);
}