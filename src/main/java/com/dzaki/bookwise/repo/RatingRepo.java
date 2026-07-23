package com.dzaki.bookwise.repo;

import com.dzaki.bookwise.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepo extends JpaRepository<Rating, UUID>, JpaSpecificationExecutor<Rating> {
    Optional<Rating> findByUser_IdAndBook_BookId(UUID userId, UUID bookId);
    boolean existsByUser_IdAndBook_BookId(UUID userId, UUID bookId);
}
