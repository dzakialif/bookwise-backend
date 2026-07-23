package com.dzaki.bookwise.service;

import com.dzaki.bookwise.entity.Book;
import com.dzaki.bookwise.entity.Rating;
import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.exception.BadRequestException;
import com.dzaki.bookwise.exception.ResourceNotFoundException;
import com.dzaki.bookwise.model.rating.CreateRatingRequest;
import com.dzaki.bookwise.model.rating.GetAllRatingRequest;
import com.dzaki.bookwise.model.rating.RatingResponse;
import com.dzaki.bookwise.model.rating.UpdateRatingRequest;
import com.dzaki.bookwise.repo.BookRepo;
import com.dzaki.bookwise.repo.RatingRepo;
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
public class RatingService {

    private final RatingRepo ratingRepo;
    private final UserRepo userRepo;
    private final BookRepo bookRepo;
    private final ValidationService validationService;

    private RatingResponse toRatingResponse(Rating rating) {
        return RatingResponse.builder()
                .ratingId(rating.getRatingId())
                .userId(rating.getUser().getId())
                .bookId(rating.getBook().getBookId())
                .rating(rating.getRating())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }

    @Transactional
    public RatingResponse createRating(CreateRatingRequest request) {
        validationService.validate(request);

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Book book = bookRepo.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (!"PUBLISHED".equals(book.getStatus())) {
            throw new BadRequestException("Only published books can be rated");
        }

        Rating rating = ratingRepo.findByUser_IdAndBook_BookId(request.getUserId(), request.getBookId())
                .orElse(new Rating());

        rating.setUser(user);
        rating.setBook(book);
        rating.setRating(request.getRating());

        if (rating.getCreatedAt() == null) {
            rating.setCreatedAt(OffsetDateTime.now());
        }
        rating.setUpdatedAt(OffsetDateTime.now());

        ratingRepo.save(rating);
        return toRatingResponse(rating);
    }

    public RatingResponse getRatingById(UUID ratingId) {
        Rating rating = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
        return toRatingResponse(rating);
    }

    @Transactional
    public Page<RatingResponse> getAllRating(GetAllRatingRequest request) {
        validationService.validate(request);

        Specification<Rating> spec = (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (request.getBookId() != null) {
                predicates.add(builder.equal(root.get("book").get("bookId"), request.getBookId()));
            }
            if (request.getUserId() != null) {
                predicates.add(builder.equal(root.get("user").get("id"), request.getUserId()));
            }
            if (request.getRating() != null) {
                predicates.add(builder.equal(root.get("rating"), request.getRating()));
            }

            if (predicates.isEmpty()) return builder.conjunction();
            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (StringUtils.hasText(request.getSortBy())) {
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, request.getSortBy());
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Rating> ratings = ratingRepo.findAll(spec, pageable);
        List<RatingResponse> responses = ratings.getContent().stream()
                .map(this::toRatingResponse).collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, ratings.getTotalElements());
    }

    @Transactional
    public RatingResponse updateRating(UUID ratingId, UpdateRatingRequest request) {
        validationService.validate(request);

        Rating rating = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        if (!rating.getUser().getId().equals(request.getUserId())) {
            throw new BadRequestException("Cannot update another user's rating");
        }

        if (request.getRating() != null) {
            rating.setRating(request.getRating());
        }

        rating.setUpdatedAt(OffsetDateTime.now());
        ratingRepo.save(rating);
        return toRatingResponse(rating);
    }

    @Transactional
    public void deleteRating(UUID ratingId) {
        Rating rating = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
        ratingRepo.delete(rating);
    }
}
