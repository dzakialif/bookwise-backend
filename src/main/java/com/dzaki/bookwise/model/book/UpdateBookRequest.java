package com.dzaki.bookwise.model.book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookRequest {

    @Schema(example = "Dune (Updated Edition)")
    private String title;

    @Schema(example = "Frank Herbert")
    private String authorName;

    @Schema(example = "0190c3d4-...")
    private UUID categoryId;

    @Schema(example = "DRAFT")
    private String status;

    @Schema(example = "Updated description")
    private String description;

    @Schema(example = "9780441013593")
    private String isbn;

    @Schema(example = "1965")
    private Integer publicationYear;

    @Schema(example = "Ace Books")
    private String publisher;

    @Schema(example = "https://r2.cloudflare.com/bookwise/covers/uuid.webp")
    private String coverUrl;

    @Schema(example = "https://r2.cloudflare.com/bookwise/books/uuid.pdf")
    private String fileUrl;

    @Schema(example = "412")
    private Integer totalPages;
}
