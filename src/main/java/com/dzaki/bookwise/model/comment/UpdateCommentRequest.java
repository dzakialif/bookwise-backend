package com.dzaki.bookwise.model.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentRequest {

    @NotNull(message = "User is required")
    @Schema(example = "0190a1b2-...")
    private UUID userId;

    @NotNull(message = "Book is required")
    @Schema(example = "0190d4e5-...")
    private UUID bookId;

    @Schema(example = "Updated comment text")
    private String content;
}
