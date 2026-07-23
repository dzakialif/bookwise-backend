package com.dzaki.bookwise.model.comment;

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
public class CommentResponse {
    
    @JsonProperty("comment_id")
    private UUID commentId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("book_id")
    private UUID bookId;

    private String content;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
