package com.dzaki.bookwise.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dzaki.bookwise.entity.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    
    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}
