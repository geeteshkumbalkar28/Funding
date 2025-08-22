package com.donorbox.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/documents")
@Slf4j
@Tag(name = "Document API", description = "API for serving uploaded documents")
public class DocumentController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/{category}/{filename:.+}")
    @Operation(summary = "Get document", description = "Retrieve a document by category and filename")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<Resource> getDocument(
            @Parameter(description = "Document category") @PathVariable String category,
            @Parameter(description = "Document filename") @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(uploadDir).resolve(category).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                String contentType = determineContentType(filename);
                
                // Set appropriate headers for document download/viewing
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .headers(headers)
                        .body(resource);
            } else {
                log.warn("Document not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL for document: {}/{}", category, filename, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error serving document: {}/{}", category, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{category}/{filename}/download")
    @Operation(summary = "Download document", description = "Download a document as attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<Resource> downloadDocument(
            @Parameter(description = "Document category") @PathVariable String category,
            @Parameter(description = "Document filename") @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(uploadDir).resolve(category).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                String contentType = determineContentType(filename);
                
                // Set headers for download
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .headers(headers)
                        .body(resource);
            } else {
                log.warn("Document not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL for document download: {}/{}", category, filename, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error downloading document: {}/{}", category, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
