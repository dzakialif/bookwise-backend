package com.dzaki.bookwise.model.book;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.dzaki.bookwise.model.category.CategoryResponse;
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
public class BookResponse {
    
    @JsonProperty("book_id")
    private UUID bookId;

    private String title;

    private String authorName;

    private CategoryResponse category;

    private String status;

    private String description;

    private String isbn;

    private Integer publicationYear;

    private String publisher;

    private String coverUrl;

    private String fileUrl;

    private Integer totalPages;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
    
}
