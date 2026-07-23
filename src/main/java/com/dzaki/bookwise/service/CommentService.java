package com.dzaki.bookwise.service;

import com.dzaki.bookwise.entity.Book;
import com.dzaki.bookwise.entity.Comment;
import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.exception.BadRequestException;
import com.dzaki.bookwise.exception.ResourceNotFoundException;
import com.dzaki.bookwise.model.comment.CommentResponse;
import com.dzaki.bookwise.model.comment.CreateCommentRequest;
import com.dzaki.bookwise.model.comment.GetAllCommentRequest;
import com.dzaki.bookwise.model.comment.UpdateCommentRequest;
import com.dzaki.bookwise.repo.BookRepo;
import com.dzaki.bookwise.repo.CommentRepo;
import com.dzaki.bookwise.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepo commentRepo;
    private final UserRepo userRepo;
    private final BookRepo bookRepo;
    private final ValidationService validationService;

    private CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .bookId(comment.getBook().getBookId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        validationService.validate(request);

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Book book = bookRepo.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (!"PUBLISHED".equals(book.getStatus())) {
            throw new BadRequestException("Only published books can be commented");
        }

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setBook(book);
        comment.setContent(request.getContent());
        comment.setCreatedAt(OffsetDateTime.now());
        comment.setUpdatedAt(OffsetDateTime.now());

        commentRepo.save(comment);
        return toCommentResponse(comment);
    }

    public CommentResponse getCommentById(UUID commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (comment.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Comment not found");
        }
        return toCommentResponse(comment);
    }

    @Transactional
    public Page<CommentResponse> getAllComment(GetAllCommentRequest request) {
        validationService.validate(request);

        Specification<Comment> spec = (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(builder.isNull(root.get("deletedAt")));

            if (request.getBookId() != null) {
                predicates.add(builder.equal(root.get("book").get("bookId"), request.getBookId()));
            }
            if (request.getUserId() != null) {
                predicates.add(builder.equal(root.get("user").get("id"), request.getUserId()));
            }
            if (StringUtils.hasText(request.getContent())) {
                predicates.add(builder.like(builder.lower(root.get("content")),
                        "%" + request.getContent().toLowerCase() + "%"));
            }

            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (StringUtils.hasText(request.getSortBy())) {
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, request.getSortBy());
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Comment> comments = commentRepo.findAll(spec, pageable);
        List<CommentResponse> responses = comments.getContent().stream()
                .map(this::toCommentResponse).collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, comments.getTotalElements());
    }

    @Transactional
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest request) {
        validationService.validate(request);

        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (comment.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Comment not found");
        }

        if (!comment.getUser().getId().equals(request.getUserId())) {
            throw new BadRequestException("Cannot update another user's comment");
        }

        if (StringUtils.hasText(request.getContent())) {
            comment.setContent(request.getContent());
        }

        comment.setUpdatedAt(OffsetDateTime.now());
        commentRepo.save(comment);
        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        comment.setDeletedAt(OffsetDateTime.now());
        commentRepo.save(comment);
    }

    @Transactional
    public void moderateComment(UUID commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        comment.setDeletedAt(OffsetDateTime.now());
        commentRepo.save(comment);
    }
}
