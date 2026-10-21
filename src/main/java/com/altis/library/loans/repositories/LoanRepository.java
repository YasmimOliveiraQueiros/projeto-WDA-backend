package com.altis.library.loans.repositories;

import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanRepository extends JpaRepository<Loan, Long>,
        JpaSpecificationExecutor<Loan> {

    boolean existsByBook_Id(Long bookId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select loan from Loan loan where loan.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);

    long countByUserIdAndStatus(Long userId, LoanStatus status);
    long countByStatus(LoanStatus status);
    List<Loan> findByUserId(Long userId);
    Page<Loan> findByUserId(Long userId, Pageable pageable);
    List<Loan> findByUser_NameContainingIgnoreCase(String name);
    List<Loan> findTop5ByOrderByLoanDateDesc();
}