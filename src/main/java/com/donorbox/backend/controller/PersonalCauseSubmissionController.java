package com.donorbox.backend.controller;

import com.donorbox.backend.dto.PersonalCauseSubmissionRequest;
import com.donorbox.backend.dto.PersonalCauseSubmissionResponse;
import com.donorbox.backend.service.PersonalCauseSubmissionService;
import com.donorbox.backend.service.ImageUploadService;
import com.donorbox.backend.service.DocumentUploadService;
import com.donorbox.backend.service.MediaUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/personal-cause-submissions")
@RequiredArgsConstructor
@Tag(name = "Personal Cause Submissions", description = "API for users to submit personal causes for approval")
public class PersonalCauseSubmissionController {

    private final PersonalCauseSubmissionService submissionService;
    private final ImageUploadService imageUploadService;
    private final DocumentUploadService documentUploadService;
    private final MediaUploadService mediaUploadService;

    @PostMapping
    @Operation(summary = "Submit personal cause (JSON only)", description = "Submit a personal cause for admin approval - JSON data only, no file uploads")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause submission created successfully",
                        content = @Content(mediaType = "application/json", 
                                         schema = @Schema(implementation = PersonalCauseSubmissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid submission data")
    })
    public ResponseEntity<PersonalCauseSubmissionResponse> submitPersonalCause(
            @Valid @RequestBody PersonalCauseSubmissionRequest request) {
        PersonalCauseSubmissionResponse response = submissionService.createSubmission(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit personal cause with media and document", 
               description = "Submit a personal cause with 2 file inputs: 'media' (image OR video - auto-detected) and 'proofDocument' (PDF, DOC, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause submission created successfully with media"),
            @ApiResponse(responseCode = "400", description = "Invalid submission data or media upload failed")
    })
    public ResponseEntity<PersonalCauseSubmissionResponse> submitPersonalCauseWithAllMedia(
            @Parameter(description = "Cause title") @RequestParam("title") String title,
            @Parameter(description = "Cause description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam("targetAmount") String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "End date (ISO format)") @RequestParam(value = "endDate", required = false) String endDate,
            @Parameter(description = "Submitter name") @RequestParam("submitterName") String submitterName,
            @Parameter(description = "Submitter email") @RequestParam("submitterEmail") String submitterEmail,
            @Parameter(description = "Submitter phone") @RequestParam(value = "submitterPhone", required = false) String submitterPhone,
            @Parameter(description = "Submitter message") @RequestParam(value = "submitterMessage", required = false) String submitterMessage,
            @Parameter(description = "Media file (image or video - auto-detected). Accepts only one file: JPG, PNG, GIF, WEBP, MP4, AVI, MOV, WEBM, etc.") @RequestParam(value = "media", required = false) MultipartFile media,
            @Parameter(description = "Proof document file (PDF, DOC, DOCX, JPG, PNG, etc.). Accepts only one file.") @RequestParam(value = "proofDocument", required = false) MultipartFile proofDocument) {
        
        try {
            String imageUrl = null;
            String videoUrl = null;
            String proofDocumentUrl = null;
            String proofDocumentName = null;
            String proofDocumentType = null;
            
            // Handle unified media upload if provided (auto-detects image or video)
            if (media != null && !media.isEmpty()) {
                String mediaPath = mediaUploadService.uploadMedia(media, "personal-causes");
                String fileName = media.getOriginalFilename();
                
                if (mediaUploadService.isImageFile(fileName)) {
                    imageUrl = mediaPath;
                } else if (mediaUploadService.isVideoFile(fileName)) {
                    videoUrl = mediaPath;
                }
            }
            
            // Handle proof document upload if provided (only one document)
            if (proofDocument != null && !proofDocument.isEmpty()) {
                proofDocumentUrl = documentUploadService.uploadDocument(proofDocument, "proof-documents");
                proofDocumentName = proofDocument.getOriginalFilename();
                proofDocumentType = documentUploadService.getFileExtension(proofDocumentUrl);
            }
            
            // Create request object
            PersonalCauseSubmissionRequest request = PersonalCauseSubmissionRequest.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .targetAmount(new BigDecimal(targetAmount))
                    .category(category)
                    .location(location)
                    .endDate(endDate != null ? LocalDateTime.parse(endDate) : null)
                    .submitterName(submitterName)
                    .submitterEmail(submitterEmail)
                    .submitterPhone(submitterPhone)
                    .submitterMessage(submitterMessage)
                    .build();
            
            PersonalCauseSubmissionResponse response = submissionService.createSubmission(
                    request, imageUrl, videoUrl, proofDocumentUrl, proofDocumentName, proofDocumentType);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/with-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit personal cause with media and documents", description = "Submit a personal cause with unified media upload (auto-detects image/video) and optional proof documents for admin approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause submission created successfully with media"),
            @ApiResponse(responseCode = "400", description = "Invalid submission data or media upload failed")
    })
    public ResponseEntity<PersonalCauseSubmissionResponse> submitPersonalCauseWithMedia(
            @Parameter(description = "Cause title") @RequestParam("title") String title,
            @Parameter(description = "Cause description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam("targetAmount") String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "End date (ISO format)") @RequestParam(value = "endDate", required = false) String endDate,
            @Parameter(description = "Submitter name") @RequestParam("submitterName") String submitterName,
            @Parameter(description = "Submitter email") @RequestParam("submitterEmail") String submitterEmail,
            @Parameter(description = "Submitter phone") @RequestParam(value = "submitterPhone", required = false) String submitterPhone,
            @Parameter(description = "Submitter message") @RequestParam(value = "submitterMessage", required = false) String submitterMessage,
            @Parameter(description = "Media file (image or video - auto-detected). Accepts only one file.") @RequestParam(value = "media", required = false) MultipartFile media,
            @Parameter(description = "Proof document file (PDF, DOC, DOCX, JPG, PNG, etc.). Accepts only one file.") @RequestParam(value = "proofDocument", required = false) MultipartFile proofDocument) {
        
        try {
            String imageUrl = null;
            String videoUrl = null;
            String proofDocumentUrl = null;
            String proofDocumentName = null;
            String proofDocumentType = null;
            
            // Handle unified media upload if provided
            if (media != null && !media.isEmpty()) {
                String mediaPath = mediaUploadService.uploadMedia(media, "personal-causes");
                String fileName = media.getOriginalFilename();
                
                if (mediaUploadService.isImageFile(fileName)) {
                    imageUrl = mediaPath;
                } else if (mediaUploadService.isVideoFile(fileName)) {
                    videoUrl = mediaPath;
                }
            }
            
            // Handle proof document upload if provided (only one document)
            if (proofDocument != null && !proofDocument.isEmpty()) {
                proofDocumentUrl = documentUploadService.uploadDocument(proofDocument, "proof-documents");
                proofDocumentName = proofDocument.getOriginalFilename();
                proofDocumentType = documentUploadService.getFileExtension(proofDocumentUrl);
            }
            
            // Create request object
            PersonalCauseSubmissionRequest request = PersonalCauseSubmissionRequest.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .targetAmount(new BigDecimal(targetAmount))
                    .category(category)
                    .location(location)
                    .endDate(endDate != null ? LocalDateTime.parse(endDate) : null)
                    .submitterName(submitterName)
                    .submitterEmail(submitterEmail)
                    .submitterPhone(submitterPhone)
                    .submitterMessage(submitterMessage)
                    .build();
            
            PersonalCauseSubmissionResponse response = submissionService.createSubmission(
                    request, imageUrl, videoUrl, proofDocumentUrl, proofDocumentName, proofDocumentType);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit personal cause with image (deprecated)", description = "Submit a personal cause with image upload for admin approval - use /with-media instead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause submission created successfully with image"),
            @ApiResponse(responseCode = "400", description = "Invalid submission data or image upload failed")
    })
    @Deprecated
    public ResponseEntity<PersonalCauseSubmissionResponse> submitPersonalCauseWithImage(
            @Parameter(description = "Cause title") @RequestParam("title") String title,
            @Parameter(description = "Cause description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam("targetAmount") String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "End date (ISO format)") @RequestParam(value = "endDate", required = false) String endDate,
            @Parameter(description = "Submitter name") @RequestParam("submitterName") String submitterName,
            @Parameter(description = "Submitter email") @RequestParam("submitterEmail") String submitterEmail,
            @Parameter(description = "Submitter phone") @RequestParam(value = "submitterPhone", required = false) String submitterPhone,
            @Parameter(description = "Submitter message") @RequestParam(value = "submitterMessage", required = false) String submitterMessage,
            @Parameter(description = "Image file (supports only one file)") @RequestParam(value = "image", required = false) MultipartFile image,
            @Parameter(description = "Video file (supports only one file)") @RequestParam(value = "video", required = false) MultipartFile video) {
        
        try {
            // Handle image upload if provided (only one image)
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = imageUploadService.uploadImage(image, "personal-causes");
            }
            
            // Handle video upload if provided (only one video)
            String videoUrl = null;
            if (video != null && !video.isEmpty()) {
                videoUrl = mediaUploadService.uploadVideo(video, "personal-causes");
            }
            
            // Create request object
            PersonalCauseSubmissionRequest request = PersonalCauseSubmissionRequest.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .targetAmount(new BigDecimal(targetAmount))
                    .category(category)
                    .location(location)
                    .endDate(endDate != null ? LocalDateTime.parse(endDate) : null)
                    .submitterName(submitterName)
                    .submitterEmail(submitterEmail)
                    .submitterPhone(submitterPhone)
                    .submitterMessage(submitterMessage)
                    .build();
            
            PersonalCauseSubmissionResponse response = submissionService.createSubmission(request, imageUrl, videoUrl);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit personal cause with image and proof document", 
               description = "Submit a personal cause with optional image and proof document upload for admin approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause submission created successfully with files"),
            @ApiResponse(responseCode = "400", description = "Invalid submission data or file upload failed")
    })
    public ResponseEntity<PersonalCauseSubmissionResponse> submitPersonalCauseWithFiles(
            @Parameter(description = "Cause title") @RequestParam("title") String title,
            @Parameter(description = "Cause description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam("targetAmount") String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "End date (ISO format)") @RequestParam(value = "endDate", required = false) String endDate,
            @Parameter(description = "Submitter name") @RequestParam("submitterName") String submitterName,
            @Parameter(description = "Submitter email") @RequestParam("submitterEmail") String submitterEmail,
            @Parameter(description = "Submitter phone") @RequestParam(value = "submitterPhone", required = false) String submitterPhone,
            @Parameter(description = "Submitter message") @RequestParam(value = "submitterMessage", required = false) String submitterMessage,
            @Parameter(description = "Cause image file (JPG, PNG, etc.). Accepts only one file.") @RequestParam(value = "image", required = false) MultipartFile image,
            @Parameter(description = "Proof document file (PDF, JPG, PNG, DOC, etc.). Accepts only one file.") @RequestParam(value = "proofDocument", required = false) MultipartFile proofDocument) {
        
        try {
            // Handle image upload if provided (only one image)
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = imageUploadService.uploadImage(image, "personal-causes");
            }
            
            // Handle proof document upload if provided (only one document)
            String proofDocumentUrl = null;
            String proofDocumentName = null;
            String proofDocumentType = null;
            if (proofDocument != null && !proofDocument.isEmpty()) {
                proofDocumentUrl = documentUploadService.uploadDocument(proofDocument, "proof-documents");
                proofDocumentName = proofDocument.getOriginalFilename();
                proofDocumentType = documentUploadService.getFileExtension(proofDocumentUrl);
            }
            
            // Create request object
            PersonalCauseSubmissionRequest request = PersonalCauseSubmissionRequest.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .targetAmount(new BigDecimal(targetAmount))
                    .category(category)
                    .location(location)
                    .endDate(endDate != null ? LocalDateTime.parse(endDate) : null)
                    .submitterName(submitterName)
                    .submitterEmail(submitterEmail)
                    .submitterPhone(submitterPhone)
                    .submitterMessage(submitterMessage)
                    .build();
            
            PersonalCauseSubmissionResponse response = submissionService.createSubmission(
                    request, imageUrl, null, proofDocumentUrl, proofDocumentName, proofDocumentType);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all personal cause submissions", description = "Get all personal cause submissions (admin endpoint)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all submissions")
    public ResponseEntity<List<PersonalCauseSubmissionResponse>> getAllSubmissions() {
        List<PersonalCauseSubmissionResponse> submissions = submissionService.getAllSubmissions();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/by-email/{email}")
    @Operation(summary = "Get submissions by email", description = "Get all submissions by a specific submitter email")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions")
    public ResponseEntity<List<PersonalCauseSubmissionResponse>> getSubmissionsByEmail(
            @Parameter(description = "Submitter email") @PathVariable String email) {
        List<PersonalCauseSubmissionResponse> submissions = submissionService.getSubmissionsByEmail(email);
        return ResponseEntity.ok(submissions);
    }
}
