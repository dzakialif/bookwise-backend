package com.dzaki.bookwise.model.category;

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
public class CategoryResponse {
    
    @JsonProperty("category_id")
    private UUID categoryId;

    private String name;

    private String description;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}
