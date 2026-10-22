package com.altis.library.books.repositories;

import com.altis.library.books.models.entities.Book;
import com.altis.library.books.models.enums.BookStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {

    interface BookStatusCount {
        BookStatus getStatus();
        Long getQuantity();
    }

    long countByPublisherId(Long publisherId);

    @Query("""
            select book.status as status, count(book) as quantity
            from Book book
            group by book.status
            """)
    List<BookStatusCount> countBooksByStatus();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select book from Book book where book.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}