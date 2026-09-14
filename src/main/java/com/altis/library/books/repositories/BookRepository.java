package com.altis.library.books.repositories;

import com.altis.library.books.models.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

}