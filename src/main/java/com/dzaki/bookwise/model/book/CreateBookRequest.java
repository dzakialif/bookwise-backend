package com.dzaki.bookwise.model.book;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookRequest {

    @NotBlank(message = "Title is required")
    @Schema(example = "Dune")
    private String title;

    @NotBlank(message = "Author name is required")
    @Schema(example = "Frank Herbert")
    private String authorName;

    @NotNull(message = "Category is required")
    @Schema(example = "0190c3d4-...")
    private UUID categoryId;

    @NotBlank(message = "ISBN is required")
    @Schema(example = "9780441013593")
    private String isbn;

    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Schema(example = "1965")
    private Integer publicationYear;

    @Schema(example = "A classic science fiction novel set in the desert planet Arrakis.")
    private String description;

    @Schema(example = "Ace Books")
    private String publisher;
}
