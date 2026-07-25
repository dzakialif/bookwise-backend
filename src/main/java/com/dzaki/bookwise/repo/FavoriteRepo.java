package com.dzaki.bookwise.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dzaki.bookwise.entity.Favorite;

@Repository
public interface FavoriteRepo extends JpaRepository<Favorite, UUID>, JpaSpecificationExecutor<Favorite> {
    
}
