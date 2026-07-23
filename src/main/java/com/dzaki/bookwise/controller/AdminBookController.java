package com.dzaki.bookwise.controller;

import com.dzaki.bookwise.model.PagingResponse;
import com.dzaki.bookwise.model.WebResponse;
import com.dzaki.bookwise.model.book.BookResponse;
import com.dzaki.bookwise.model.book.CreateBookRequest;
import com.dzaki.bookwise.model.book.GetAllBookRequest;
import com.dzaki.bookwise.model.book.UpdateBookRequest;
import com.dzaki.bookwise.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<BookResponse> createBook(
            @Valid @RequestPart("book") CreateBookRequest request,
            @RequestPart("cover") MultipartFile coverFile,
            @RequestPart("file") MultipartFile bookFile) {
        BookResponse response = bookService.createBook(request, coverFile, bookFile);
        return WebResponse.<BookResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping(
            path = "/{bookId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<BookResponse> getBookById(@PathVariable UUID bookId) {
        BookResponse response = bookService.getBookById(bookId);
        return WebResponse.<BookResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<BookResponse>> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        GetAllBookRequest request = GetAllBookRequest.builder()
                .title(title)
                .authorName(authorName)
                .categoryId(categoryId)
                .status(status)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<BookResponse> response = bookService.getAllBook(request);

        return WebResponse.<List<BookResponse>>builder()
                .data(response.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(response.getNumber())
                        .totalPage(response.getTotalPages())
                        .size(response.getSize())
                        .totalItems((int) response.getTotalElements())
                        .build())
                .build();
    }

    @PutMapping(
            path = "/{bookId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<BookResponse> updateBook(
            @PathVariable UUID bookId,
            @Valid @RequestPart("book") UpdateBookRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile,
            @RequestPart(value = "file", required = false) MultipartFile bookFile) {
        BookResponse response = bookService.updateBook(bookId, request, coverFile, bookFile);
        return WebResponse.<BookResponse>builder()
                .data(response)
                .build();
    }

    @DeleteMapping(
            path = "/{bookId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Void> deleteBook(@PathVariable UUID bookId) {
        bookService.deleteBook(bookId);
        return WebResponse.<Void>builder()
                .message("Book deleted")
                .build();
    }

    @PostMapping(
            path = "/{bookId}/publish",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<BookResponse> publishBook(@PathVariable UUID bookId) {
        BookResponse response = bookService.publishBook(bookId);
        return WebResponse.<BookResponse>builder()
                .data(response)
                .message("Book published")
                .build();
    }

    @PostMapping(
            path = "/{bookId}/archive",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<BookResponse> archiveBook(@PathVariable UUID bookId) {
        BookResponse response = bookService.archiveBook(bookId);
        return WebResponse.<BookResponse>builder()
                .data(response)
                .message("Book archived")
                .build();
    }

    @PostMapping(
            path = "/{bookId}/restore",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<BookResponse> restoreBook(@PathVariable UUID bookId) {
        BookResponse response = bookService.restoreBook(bookId);
        return WebResponse.<BookResponse>builder()
                .data(response)
                .message("Book restored to draft")
                .build();
    }
}
