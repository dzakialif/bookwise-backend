package com.dzaki.bookwise.model.book;

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
public class GetAllBookRequest {
    
    private String title;

    private String authorName;

    private UUID categoryId;
    
    private String status;

    private String description;

    private String isbn;

    private Integer publicationYear;

    private String publisher;

    private Integer totalPages;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    private String sortBy;

    private String sortDirection;
}
