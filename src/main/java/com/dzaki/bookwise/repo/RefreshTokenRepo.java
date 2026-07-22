package com.dzaki.bookwise.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dzaki.bookwise.entity.RefreshToken;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {
    
    List<RefreshToken> findByUser_Id(UUID userId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
