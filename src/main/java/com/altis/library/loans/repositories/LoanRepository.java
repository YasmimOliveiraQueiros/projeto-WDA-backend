package com.altis.library.loans.repositories;

import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByUserIdAndStatus(Long userId, LoanStatus status);
    long countByStatus(LoanStatus status);
    List<Loan> findByUserId(Long userId);
    List<Loan> findByUser_NameContainingIgnoreCase(String name);
    List<Loan> findTop5ByOrderByLoanDateDesc();
}