package com.dzaki.bookwise.model.rating;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RatingResponse {

    @JsonProperty("rating_id")
    private UUID ratingId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("book_id")
    private UUID bookId;

    private Integer rating;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
