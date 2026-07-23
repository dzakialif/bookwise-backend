package com.dzaki.bookwise.repo;

import com.dzaki.bookwise.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepo extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {
    boolean existsByIsbn(String isbn);
}
