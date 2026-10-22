package com.altis.library.loans.repositories;

import com.altis.library.loans.models.entities.Loan;
import com.altis.library.loans.models.enums.LoanStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanRepository extends JpaRepository<Loan, Long>,
        JpaSpecificationExecutor<Loan> {

    interface MonthlyLoanCount {
        Integer getYearValue();
        Integer getMonthValue();
        Long getQuantity();
    }

    boolean existsByBook_Id(Long bookId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select loan from Loan loan where loan.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);

    long countByUserIdAndStatusIn(Long userId, List<LoanStatus> statuses);
    long countByStatus(LoanStatus status);

    @Query("""
            select year(loan.loanDate) as yearValue,
                   month(loan.loanDate) as monthValue,
                   count(loan) as quantity
            from Loan loan
            where loan.loanDate >= :startDate and loan.loanDate < :endDate
            group by year(loan.loanDate), month(loan.loanDate)
            order by year(loan.loanDate), month(loan.loanDate)
            """)
    List<MonthlyLoanCount> countLoansByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
            select count(loan) from Loan loan
            where loan.status in :activeStatuses and loan.returnDate >= :today
            """)
    long countActiveDueLoans(
            @Param("activeStatuses") List<LoanStatus> activeStatuses,
            @Param("today") LocalDate today);

    @Query("""
            select count(loan) from Loan loan
            where loan.status in :activeStatuses and loan.returnDate < :today
            """)
    long countOverdueLoans(
            @Param("activeStatuses") List<LoanStatus> activeStatuses,
            @Param("today") LocalDate today);

    List<Loan> findByUserId(Long userId);
    @EntityGraph(attributePaths = "book")
    Page<Loan> findByUserId(Long userId, Pageable pageable);
    List<Loan> findByUser_NameContainingIgnoreCase(String name);
    List<Loan> findTop5ByOrderByLoanDateDesc();
}