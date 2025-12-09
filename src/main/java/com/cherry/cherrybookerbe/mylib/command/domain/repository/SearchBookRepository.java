package com.cherry.cherrybookerbe.mylib.command.domain.repository;

import com.cherry.cherrybookerbe.mylib.command.domain.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchBookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findFirstByTitleIgnoreCase(String title);
}
