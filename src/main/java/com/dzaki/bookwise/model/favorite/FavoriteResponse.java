package com.dzaki.bookwise.model.favorite;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavoriteResponse {
    
    @JsonProperty("favorite_id")
    private UUID favoriteId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("book_id")
    private UUID bookId;
    
    private OffsetDateTime createdAt;
}
