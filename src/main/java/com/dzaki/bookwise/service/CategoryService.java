package com.dzaki.bookwise.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dzaki.bookwise.entity.Category;
import com.dzaki.bookwise.exception.ResourceAlreadyExistsException;
import com.dzaki.bookwise.exception.ResourceNotFoundException;
import com.dzaki.bookwise.model.category.CategoryResponse;
import com.dzaki.bookwise.model.category.CreateCategoryRequest;
import com.dzaki.bookwise.model.category.GetAllCategoryRequest;
import com.dzaki.bookwise.model.category.UpdateCategoryRequest;
import com.dzaki.bookwise.repo.CategoryRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepo categoryRepo;

    private final ValidationService validationService;

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        validationService.validate(request);

        String name = request.getName().trim().toLowerCase();
        if (categoryRepo.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Category with name '" + name + "' already exists.");
        }

        Category cat = new Category();
        cat.setName(request.getName().trim().toLowerCase());
        cat.setDescription(request.getDescription());
        cat.setCreatedAt(OffsetDateTime.now());
        cat.setUpdatedAt(OffsetDateTime.now());

        categoryRepo.save(cat);
        return toCategoryResponse(cat);
    }

    public CategoryResponse getCategoryById(UUID categoryId) {
        Category cat = categoryRepo.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
        return toCategoryResponse(cat);
    }

    @Transactional
    public Page<CategoryResponse> getAllCategory(GetAllCategoryRequest request) {
        validationService.validate(request);

        Specification<Category> spec = (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getName())) {
                String likePattern = "%" + request.getName().toLowerCase() + "%";
                predicates.add(builder.like(builder.lower(root.get("name")), likePattern));
            }

            if (StringUtils.hasText(request.getDescription())) {
                String likePattern = "%" + request.getDescription().toLowerCase() + "%";
                predicates.add(builder.like(builder.lower(root.get("description")), likePattern));
            }


            if (predicates.isEmpty()) {
                return builder.conjunction();
            }

            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // setup sort
        Sort sort = Sort.unsorted();
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;
            if ("desc".equalsIgnoreCase(request.getSortDirection())) {
                direction = Sort.Direction.DESC;
            }
            sort = Sort.by(direction, request.getSortBy());
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Category> cat = categoryRepo.findAll(spec, pageable);
        List<CategoryResponse> categoryResponse = cat.getContent().stream()
            .map(this::toCategoryResponse)
            .collect(Collectors.toList());

        return new PageImpl<>(categoryResponse, pageable, cat.getTotalElements());
    }

    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        validationService.validate(request);

        Category cat = categoryRepo.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category with ID " + categoryId + " not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            cat.setName(request.getName().trim().toLowerCase());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            cat.setDescription(request.getDescription().trim().toLowerCase());
        }

        cat.setUpdatedAt(OffsetDateTime.now());

        categoryRepo.save(cat);

        return toCategoryResponse(cat);
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category cat = categoryRepo.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + categoryId + " not found"));
        categoryRepo.delete(cat);
    }

    
}
