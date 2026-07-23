package com.dzaki.bookwise.repo;

import com.dzaki.bookwise.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepo extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {
}
