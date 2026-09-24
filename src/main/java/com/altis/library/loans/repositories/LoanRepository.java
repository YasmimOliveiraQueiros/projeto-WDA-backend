package com.altis.library.loans.repositories;

import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanRepository extends JpaRepository<Loan, Long>,
        JpaSpecificationExecutor<Loan> {

    long countByUserIdAndStatus(Long userId, LoanStatus status);
    long countByStatus(LoanStatus status);
    List<Loan> findByUserId(Long userId);
    Page<Loan> findByUserId(Long userId, Pageable pageable);
    List<Loan> findByUser_NameContainingIgnoreCase(String name);
    List<Loan> findTop5ByOrderByLoanDateDesc();
}