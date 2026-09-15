package com.altis.library.loans.repositories;

import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByUserIdAndStatus(Long userId, LoanStatus status);
}