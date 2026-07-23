package com.dzaki.bookwise.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class R2Service {

    private final S3Client r2Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public R2Service(S3Client r2Client) {
        this.r2Client = r2Client;
    }

    public String uploadCover(MultipartFile file) {
        String fileName = "covers/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        return upload(file, fileName);
    }

    public String uploadBookFile(MultipartFile file) {
        String fileName = "books/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        return upload(file, fileName);
    }

    private String upload(MultipartFile file, String fileName) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            r2Client.putObject(putRequest,
                    RequestBody.fromBytes(file.getBytes()));

            return publicUrl + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to R2", e);
        }
    }
}
