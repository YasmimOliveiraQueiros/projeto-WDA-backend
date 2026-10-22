package com.altis.library.users.repositories;

import com.altis.library.users.models.entities.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    boolean existsByIsAdminTrue(); // verifica se existe algum admin
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByCpfAndIdNot(String cpf, Long id);

    User findByEmail(String email); // busca um usuário pelo email
    User findByCpf(String cpf);
    Optional<User> findByEmailAndCpf(String email, String cpf);
    long countByIsAdminFalse();
    List<User> findByIsAdminFalse();
    Optional<User> findByIdAndIsAdminFalse(Long id);
}