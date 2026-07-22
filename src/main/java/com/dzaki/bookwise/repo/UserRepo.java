package com.dzaki.bookwise.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dzaki.bookwise.entity.User;

@Repository
public interface UserRepo extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    
    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
    
}
