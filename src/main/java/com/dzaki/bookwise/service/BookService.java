package com.dzaki.bookwise.service;

import com.dzaki.bookwise.entity.Book;
import com.dzaki.bookwise.entity.Category;
import com.dzaki.bookwise.exception.BadRequestException;
import com.dzaki.bookwise.exception.ResourceAlreadyExistsException;
import com.dzaki.bookwise.exception.ResourceNotFoundException;
import com.dzaki.bookwise.model.book.*;
import com.dzaki.bookwise.model.category.CategoryResponse;
import com.dzaki.bookwise.repo.BookRepo;
import com.dzaki.bookwise.repo.CategoryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepo bookRepo;
    private final CategoryRepo categoryRepo;
    private final ValidationService validationService;
    private final R2Service r2Service;

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private static final Set<String> ALLOWED_COVER_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_COVER_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String ALLOWED_FILE_TYPE = "application/pdf";
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB

    public void validateCoverFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cover file is empty");
        }
        if (!ALLOWED_COVER_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Cover must be JPG, PNG, or WebP");
        }
        if (file.getSize() > MAX_COVER_SIZE) {
            throw new BadRequestException("Cover max size is 2MB");
        }
    }

    public void validateBookFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Book file is empty");
        }
        if (!ALLOWED_FILE_TYPE.equals(file.getContentType())) {
            throw new BadRequestException("Book file must be PDF");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Book file max size is 15MB");
        }
    }

    private BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .category(toCategoryResponse(book.getCategory()))
                .status(book.getStatus())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .publicationYear(book.getPublicationYear())
                .publisher(book.getPublisher())
                .coverUrl(book.getCoverUrl())
                .fileUrl(book.getFileUrl())
                .totalPages(book.getTotalPages())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request, MultipartFile coverFile, MultipartFile bookFile) {
        validationService.validate(request);

        validateCoverFile(coverFile);
        validateBookFile(bookFile);

        if (bookRepo.existsByIsbn(request.getIsbn())) {
            throw new ResourceAlreadyExistsException("Book with ISBN " + request.getIsbn() + " already exists.");
        }

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        String coverUrl = r2Service.uploadCover(coverFile);
        String fileUrl = r2Service.uploadBookFile(bookFile);

        Book book = new Book();
        book.setTitle(request.getTitle().trim());
        book.setAuthorName(request.getAuthorName().trim());
        book.setCategory(category);
        book.setStatus("DRAFT");
        book.setIsbn(request.getIsbn().trim());
        book.setPublicationYear(request.getPublicationYear());
        book.setDescription(request.getDescription());
        book.setPublisher(request.getPublisher());
        book.setCoverUrl(coverUrl);
        book.setFileUrl(fileUrl);
        book.setCreatedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());

        bookRepo.save(book);
        return toBookResponse(book);
    }

    public BookResponse getBookById(UUID bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        return toBookResponse(book);
    }

    @Transactional
    public Page<BookResponse> getAllBook(GetAllBookRequest request) {
        validationService.validate(request);

        Specification<Book> spec = (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getTitle())) {
                predicates.add(builder.like(builder.lower(root.get("title")),
                        "%" + request.getTitle().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(request.getAuthorName())) {
                predicates.add(builder.like(builder.lower(root.get("authorName")),
                        "%" + request.getAuthorName().toLowerCase() + "%"));
            }
            if (request.getCategoryId() != null) {
                predicates.add(builder.equal(root.get("category").get("categoryId"), request.getCategoryId()));
            }
            if (StringUtils.hasText(request.getStatus())) {
                predicates.add(builder.equal(root.get("status"), request.getStatus()));
            }

            if (predicates.isEmpty()) return builder.conjunction();
            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (StringUtils.hasText(request.getSortBy())) {
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, request.getSortBy());
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Book> books = bookRepo.findAll(spec, pageable);
        List<BookResponse> responses = books.getContent().stream()
                .map(this::toBookResponse).collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, books.getTotalElements());
    }

    @Transactional
    public BookResponse updateBook(UUID bookId, UpdateBookRequest request) {
        return updateBook(bookId, request, null, null);
    }

    @Transactional
    public BookResponse updateBook(UUID bookId, UpdateBookRequest request, MultipartFile coverFile, MultipartFile bookFile) {
        validationService.validate(request);

        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (StringUtils.hasText(request.getTitle())) book.setTitle(request.getTitle().trim());
        if (StringUtils.hasText(request.getAuthorName())) book.setAuthorName(request.getAuthorName().trim());
        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            book.setCategory(category);
        }
        if (StringUtils.hasText(request.getDescription())) book.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getIsbn())) {
            if (!request.getIsbn().equals(book.getIsbn()) && bookRepo.existsByIsbn(request.getIsbn())) {
                throw new ResourceAlreadyExistsException("ISBN " + request.getIsbn() + " already exists.");
            }
            book.setIsbn(request.getIsbn().trim());
        }
        if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
        if (StringUtils.hasText(request.getPublisher())) book.setPublisher(request.getPublisher());
        if (coverFile != null && !coverFile.isEmpty()) {
            validateCoverFile(coverFile);
            book.setCoverUrl(r2Service.uploadCover(coverFile));
        } else if (request.getCoverUrl() != null) {
            book.setCoverUrl(request.getCoverUrl());
        }
        if (bookFile != null && !bookFile.isEmpty()) {
            validateBookFile(bookFile);
            book.setFileUrl(r2Service.uploadBookFile(bookFile));
        } else if (request.getFileUrl() != null) {
            book.setFileUrl(request.getFileUrl());
        }
        if (request.getTotalPages() != null) book.setTotalPages(request.getTotalPages());

        book.setUpdatedAt(OffsetDateTime.now());
        bookRepo.save(book);
        return toBookResponse(book);
    }

    @Transactional
    public void deleteBook(UUID bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        if (!"DRAFT".equals(book.getStatus())) {
            throw new BadRequestException("Only draft books can be deleted.");
        }
        bookRepo.delete(book);
    }

    @Transactional
    public BookResponse publishBook(UUID bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        if (!"DRAFT".equals(book.getStatus())) {
            throw new BadRequestException("Only draft books can be published.");
        }
        if (book.getFileUrl() == null || book.getTotalPages() == null) {
            throw new BadRequestException("File URL and total pages must be set before publishing.");
        }
        book.setStatus("PUBLISHED");
        if (book.getPublishedAt() == null) {
            book.setPublishedAt(OffsetDateTime.now());
        }
        book.setUpdatedAt(OffsetDateTime.now());
        bookRepo.save(book);
        return toBookResponse(book);
    }

    @Transactional
    public BookResponse archiveBook(UUID bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        if (!"PUBLISHED".equals(book.getStatus())) {
            throw new BadRequestException("Only published books can be archived.");
        }
        book.setStatus("ARCHIVED");
        book.setArchivedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());
        bookRepo.save(book);
        return toBookResponse(book);
    }

    @Transactional
    public BookResponse restoreBook(UUID bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        if (!"ARCHIVED".equals(book.getStatus())) {
            throw new BadRequestException("Only archived books can be restored.");
        }
        book.setStatus("DRAFT");
        book.setUpdatedAt(OffsetDateTime.now());
        bookRepo.save(book);
        return toBookResponse(book);
    }
}
