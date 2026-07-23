package com.dzaki.bookwise.controller;

import com.dzaki.bookwise.model.PagingResponse;
import com.dzaki.bookwise.model.WebResponse;
import com.dzaki.bookwise.model.rating.CreateRatingRequest;
import com.dzaki.bookwise.model.rating.GetAllRatingRequest;
import com.dzaki.bookwise.model.rating.RatingResponse;
import com.dzaki.bookwise.model.rating.UpdateRatingRequest;
import com.dzaki.bookwise.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/ratings")
@RequiredArgsConstructor
public class AdminRatingController {

    private final RatingService ratingService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RatingResponse> createRating(@Valid @RequestBody CreateRatingRequest request) {
        RatingResponse response = ratingService.createRating(request);
        return WebResponse.<RatingResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping(
            path = "/{ratingId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RatingResponse> getRatingById(@PathVariable UUID ratingId) {
        RatingResponse response = ratingService.getRatingById(ratingId);
        return WebResponse.<RatingResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<RatingResponse>> getAllRatings(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        GetAllRatingRequest request = GetAllRatingRequest.builder()
                .userId(userId)
                .bookId(bookId)
                .rating(rating)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<RatingResponse> response = ratingService.getAllRating(request);

        return WebResponse.<List<RatingResponse>>builder()
                .data(response.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(response.getNumber())
                        .totalPage(response.getTotalPages())
                        .size(response.getSize())
                        .totalItems((int) response.getTotalElements())
                        .build())
                .build();
    }

    @PutMapping(
            path = "/{ratingId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RatingResponse> updateRating(@PathVariable UUID ratingId, @Valid @RequestBody UpdateRatingRequest request) {
        RatingResponse response = ratingService.updateRating(ratingId, request);
        return WebResponse.<RatingResponse>builder()
                .data(response)
                .build();
    }

    @DeleteMapping(
            path = "/{ratingId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Void> deleteRating(@PathVariable UUID ratingId) {
        ratingService.deleteRating(ratingId);
        return WebResponse.<Void>builder()
                .message("Rating deleted")
                .build();
    }
}
