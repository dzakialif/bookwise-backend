package com.dzaki.bookwise.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dzaki.bookwise.model.PagingResponse;
import com.dzaki.bookwise.model.WebResponse;
import com.dzaki.bookwise.model.category.CategoryResponse;
import com.dzaki.bookwise.model.category.CreateCategoryRequest;
import com.dzaki.bookwise.model.category.GetAllCategoryRequest;
import com.dzaki.bookwise.model.category.UpdateCategoryRequest;
import com.dzaki.bookwise.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    
    private final CategoryService categoryService;

    @PostMapping(
        produces=MediaType.APPLICATION_JSON_VALUE,
        consumes=MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);

        return WebResponse.<CategoryResponse>builder()
            .data(response)
            .build();
    }

    @GetMapping(
        path = "/{categoryId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CategoryResponse> getCategoryById(@PathVariable UUID categoryId) {
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        
        return WebResponse.<CategoryResponse>builder()
            .data(response)
            .build();
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CategoryResponse>> getAllCategories(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection
    ) {
        GetAllCategoryRequest request = GetAllCategoryRequest.builder()
            .page(page)
            .size(size)
            .name(name)
            .description(description)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

            Page<CategoryResponse> response = categoryService.getAllCategory(request);

            return WebResponse.<List<CategoryResponse>>builder().data(response.getContent())
                .paging(PagingResponse.builder()
                    .currentPage(response.getNumber())
                    .totalPage(response.getTotalPages())
                    .size(response.getSize())
                    .totalItems((int) response.getTotalElements())
                    .build())
                .build();
    }

    @PutMapping(
        path = "/{categoryId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CategoryResponse> updateCategory(@PathVariable UUID categoryId, @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        
        return WebResponse.<CategoryResponse>builder()
            .data(response)
            .build();
    }

    @DeleteMapping(
        path = "/{categoryId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}
