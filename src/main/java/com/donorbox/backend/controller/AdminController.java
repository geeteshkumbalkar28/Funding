package com.donorbox.backend.controller;

import com.donorbox.backend.entity.*;
import com.donorbox.backend.service.*;
import com.donorbox.backend.dto.BlogRequest;
import com.donorbox.backend.dto.BlogResponse;
import com.donorbox.backend.dto.PersonalCauseSubmissionResponse;
import com.donorbox.backend.dto.SubmissionActionRequest;
import com.donorbox.backend.dto.CauseRequest;
import com.donorbox.backend.dto.CauseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import com.donorbox.backend.util.DateTimeUtil;

//blog controller

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin API", description = "Admin endpoints for managing content")
@SecurityRequirement(name = "basicAuth")
public class AdminController {

    private final CauseService causeService;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final ImageUploadService imageUploadService;
    private final BlogService blogService;
    private final PersonalCauseSubmissionService personalCauseSubmissionService;
    private final MediaUploadService mediaUploadService;
    private final DonationService donationService;
    private final EmailService emailService;
    private final DonationStatusMonitoringService monitoringService;

    @Value("${admin.email:testing@alphaseam.com}")
    private String adminEmail;

    // Admin Causes Management
    @GetMapping("/causes")
    @Operation(summary = "Admin - Get all causes", description = "Retrieve all causes for admin management")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved causes", 
                 content = @Content(mediaType = "application/json", 
                                  array = @ArraySchema(schema = @Schema(implementation = Cause.class))))
    public ResponseEntity<List<Cause>> getAllCauses() {
        List<Cause> causes = causeService.getAllCausesEntities();
        return ResponseEntity.ok(causes);
    }

    @PutMapping(value = "/blogs/{id}/update-with-content-and-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update blog with content and image", description = "Update an existing blog post with both content changes and optional featured image replacement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog updated successfully with content and image"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or image upload failed")
    })
    public ResponseEntity<BlogResponse> updateBlogWithContentAndImage(
            @Parameter(description = "Blog ID") @PathVariable Long id,
            @Parameter(description = "Blog title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Blog subtitle") @RequestParam(value = "subtitle", required = false) String subtitle,
            @Parameter(description = "Blog slug") @RequestParam(value = "slug", required = false) String slug,
            @Parameter(description = "Blog content") @RequestParam(value = "content", required = false) String content,
            @Parameter(description = "Blog excerpt") @RequestParam(value = "excerpt", required = false) String excerpt,
            @Parameter(description = "Author name") @RequestParam(value = "author", required = false) String author,
            @Parameter(description = "Author email") @RequestParam(value = "authorEmail", required = false) String authorEmail,
            @Parameter(description = "Blog status") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Tags (comma-separated)") @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "Meta title") @RequestParam(value = "metaTitle", required = false) String metaTitle,
            @Parameter(description = "Meta description") @RequestParam(value = "metaDescription", required = false) String metaDescription,
            @Parameter(description = "Is featured") @RequestParam(value = "isFeatured", required = false) Boolean isFeatured,
            @Parameter(description = "Allow comments") @RequestParam(value = "allowComments", required = false) Boolean allowComments,
            @Parameter(description = "Featured image file") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Create blog request with only non-null values
            BlogRequest.BlogRequestBuilder requestBuilder = BlogRequest.builder();
            
            if (title != null) requestBuilder.title(title);
            if (subtitle != null) requestBuilder.subtitle(subtitle);
            if (slug != null) requestBuilder.slug(slug);
            if (content != null) {
                requestBuilder.content(content);
                // Auto-calculate reading time when content is updated
                requestBuilder.readingTime(blogService.calculateReadingTime(content));
            }
            if (excerpt != null) requestBuilder.excerpt(excerpt);
            if (author != null) requestBuilder.author(author);
            if (authorEmail != null) requestBuilder.authorEmail(authorEmail);
            if (status != null) requestBuilder.status(Blog.BlogStatus.valueOf(status.toUpperCase()));
            if (tags != null) requestBuilder.tags(tags);
            if (metaTitle != null) requestBuilder.metaTitle(metaTitle);
            if (metaDescription != null) requestBuilder.metaDescription(metaDescription);
            if (isFeatured != null) requestBuilder.isFeatured(isFeatured);
            if (allowComments != null) requestBuilder.allowComments(allowComments);
            
            BlogRequest request = requestBuilder.build();
            
            BlogResponse response = blogService.updateBlogWithImageAndContent(id, request, image);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/causes/{id}")
    @Operation(summary = "Admin - Get cause by ID", description = "Retrieve specific cause for admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cause",
                        content = @Content(mediaType = "application/json", 
                                         schema = @Schema(implementation = Cause.class))),
            @ApiResponse(responseCode = "404", description = "Cause not found")
    })
    public ResponseEntity<Cause> getCauseById(
            @Parameter(description = "ID of the cause to retrieve")
            @PathVariable Long id) {
        Cause cause = causeService.getCauseEntityById(id);
        return ResponseEntity.ok(cause);
    }

@PostMapping("/causes")
@Operation(summary = "Admin - Create cause", description = "Create a new cause")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cause created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
})
public ResponseEntity<CauseResponse> createCause(@Valid @RequestBody CauseRequest request) {
    if (request.getTitle() == null || request.getDescription() == null || request.getTargetAmount() == null) {
        return ResponseEntity.badRequest().build();
    }
    CauseResponse createdCause = causeService.createCause(request);
    return new ResponseEntity<>(createdCause, HttpStatus.CREATED);
}

    @PutMapping("/causes/{id}")
    @Operation(summary = "Admin - Update cause", description = "Update an existing cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cause updated successfully",
                        content = @Content(mediaType = "application/json", 
                                         schema = @Schema(implementation = Cause.class))),
            @ApiResponse(responseCode = "404", description = "Cause not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<CauseResponse> updateCause(
            @Parameter(description = "ID of the cause to update")
            @PathVariable Long id,
            @Valid @RequestBody CauseRequest request) {
        CauseResponse updatedCause = causeService.updateCause(id, request);
        return ResponseEntity.ok(updatedCause);
    }

    @DeleteMapping("/causes/{id}")
    @Operation(summary = "Admin - Delete cause", description = "Delete a cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cause deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Cause not found")
    })
    public ResponseEntity<Void> deleteCause(
            @Parameter(description = "ID of the cause to delete")
            @PathVariable Long id) {
        causeService.deleteCause(id);
        return ResponseEntity.noContent().build();
    }

    // Create cause with video upload (multipart form)
    @PostMapping(value = "/causes/with-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Create cause with video", description = "Create a new cause with video upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause created successfully with video"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or video upload failed")
    })
    public ResponseEntity<Cause> createCauseWithVideo(
            @Parameter(description = "Cause title") @RequestParam("title") String title,
            @Parameter(description = "Cause description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam("targetAmount") String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Video file. Accepts only one file.") @RequestParam(value = "video", required = false) MultipartFile video) {

        try {
            // Create cause object
            Cause cause = Cause.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .targetAmount(new java.math.BigDecimal(targetAmount))
                    .category(category)
                    .location(location)
                    .build();

            // Handle video upload if provided
            if (video != null && !video.isEmpty()) {
                // Upload single video
                String videoPath = mediaUploadService.uploadVideo(video, "causes");
                cause.setVideoUrl(videoPath);
                    cause.setMediaType(Cause.MediaType.VIDEO);
                // Store single video URL in the videoUrls list for compatibility
                List<String> videoUrls = new ArrayList<>();
                videoUrls.add(videoPath);
                cause.setVideoUrls(videoUrls);
            }

            Cause createdCause = causeService.createCause(cause);
            return new ResponseEntity<>(createdCause, HttpStatus.CREATED);

        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

     // Update cause with video upload (multipart form)
    @PutMapping(value = "/causes/{id}/with-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update cause with video", description = "Update an existing cause with optional video upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cause updated successfully"),
            @ApiResponse(responseCode = "404", description = "Cause not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Cause> updateCauseWithVideo(
            @Parameter(description = "ID of the cause to update") @PathVariable Long id,
            @Parameter(description = "Cause title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Cause description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam(value = "targetAmount", required = false) String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Video file. Accepts only one file.") @RequestParam(value = "video", required = false) MultipartFile video) {

        try {
            // Get existing cause
            Cause existingCause = causeService.getCauseEntityById(id);

            // Update fields if provided
            if (title != null) existingCause.setTitle(title);
            if (description != null) existingCause.setDescription(description);
            if (shortDescription != null) existingCause.setShortDescription(shortDescription);
            if (targetAmount != null) existingCause.setTargetAmount(new java.math.BigDecimal(targetAmount));
            if (category != null) existingCause.setCategory(category);
            if (location != null) existingCause.setLocation(location);

            // Handle video upload if provided
            if (video != null && !video.isEmpty()) {
                // Delete old video if exists
                if (existingCause.getVideoUrl() != null) {
                    mediaUploadService.deleteMedia(existingCause.getVideoUrl());
                }

                // Upload single video
                String videoPath = mediaUploadService.uploadVideo(video, "causes");
                existingCause.setVideoUrl(videoPath);
                    existingCause.setMediaType(Cause.MediaType.VIDEO);
                // Store single video URL in the videoUrls list for compatibility
                List<String> videoUrls = new ArrayList<>();
                videoUrls.add(videoPath);
                existingCause.setVideoUrls(videoUrls);
            }

            Cause updatedCause = causeService.updateCause(id, existingCause);
            return ResponseEntity.ok(updatedCause);

        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin Events Management
    @GetMapping("/events")
    @Operation(summary = "Admin - Get all events", description = "Retrieve all events for admin management")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved events")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Admin - Get event by ID", description = "Retrieve specific event for admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved event"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Event> getEventById(
            @Parameter(description = "ID of the event to retrieve")
            @PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/events")
    @Operation(summary = "Admin - Create event", description = "Create a new event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PutMapping("/events/{id}")
    @Operation(summary = "Admin - Update event", description = "Update an existing event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Event> updateEvent(
            @Parameter(description = "ID of the event to update")
            @PathVariable Long id,
            @Valid @RequestBody Event event) {
        Event updatedEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{id}")
    @Operation(summary = "Admin - Delete event", description = "Delete an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID of the event to delete")
            @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    
    // Create cause with image upload (multipart form)
     
    @PostMapping(value = "/causes/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Create cause with image", description = "Create a new cause with image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause created successfully with image"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or image upload failed")
    })
    public ResponseEntity<Cause> createCauseWithImage(
            @Parameter(description = "Cause title") @RequestParam("title") String title,
            @Parameter(description = "Cause description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam("targetAmount") String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Image file. Accepts only one file.") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Create cause object
            Cause cause = Cause.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .targetAmount(new java.math.BigDecimal(targetAmount))
                    .category(category)
                    .location(location)
                    .build();
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                // Upload single image
                String imagePath = mediaUploadService.uploadImage(image, "causes");
                cause.setImageUrl(imagePath);
                // Store single image URL in the imageUrls list for compatibility
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add(imagePath);
                cause.setImageUrls(imageUrls);
            }
            
            Cause createdCause = causeService.createCause(cause);
            return new ResponseEntity<>(createdCause, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

     // Update cause with image upload (multipart form)
    @PutMapping(value = "/causes/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update cause with image", description = "Update an existing cause with optional image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cause updated successfully"),
            @ApiResponse(responseCode = "404", description = "Cause not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Cause> updateCauseWithImage(
            @Parameter(description = "ID of the cause to update") @PathVariable Long id,
            @Parameter(description = "Cause title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Cause description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam(value = "targetAmount", required = false) String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Image file. Accepts only one file.") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Get existing cause
            Cause existingCause = causeService.getCauseEntityById(id);
            
            // Update fields if provided
            if (title != null) existingCause.setTitle(title);
            if (description != null) existingCause.setDescription(description);
            if (shortDescription != null) existingCause.setShortDescription(shortDescription);
            if (targetAmount != null) existingCause.setTargetAmount(new java.math.BigDecimal(targetAmount));
            if (category != null) existingCause.setCategory(category);
            if (location != null) existingCause.setLocation(location);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                // Delete old image if exists
                if (existingCause.getImageUrl() != null) {
                    imageUploadService.deleteImage(existingCause.getImageUrl());
                }
                
                // Upload single image
                String imagePath = mediaUploadService.uploadImage(image, "causes");
                existingCause.setImageUrl(imagePath);
                // Store single image URL in the imageUrls list for compatibility
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add(imagePath);
                existingCause.setImageUrls(imageUrls);
            }
            
            Cause updatedCause = causeService.updateCause(id, existingCause);
            return ResponseEntity.ok(updatedCause);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

     // Create event with image upload (multipart form)
    
    @PostMapping(value = "/events/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Create event with image", description = "Create a new event with image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully with image"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or image upload failed")
    })
    public ResponseEntity<Event> createEventWithImage(
            @Parameter(description = "Event title") @RequestParam("title") String title,
            @Parameter(description = "Event description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Event date (ISO format)") @RequestParam("eventDate") String eventDate,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Max participants") @RequestParam(value = "maxParticipants", required = false) String maxParticipants,
            @Parameter(description = "Current participants") @RequestParam(value = "currentParticipants", required = false) String currentParticipants,
            @Parameter(description = "Image file") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Create event object
            Event event = Event.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .eventDate(java.time.LocalDateTime.parse(eventDate))
                    .location(location)
                    .build();
            
            if (maxParticipants != null) {
                event.setMaxParticipants(Integer.parseInt(maxParticipants));
            }
            
            // Set current participants (default to 0 if not provided)
            if (currentParticipants != null && !currentParticipants.trim().isEmpty()) {
                event.setCurrentParticipants(Integer.parseInt(currentParticipants));
            } else {
                event.setCurrentParticipants(0);
            }
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = imageUploadService.uploadImage(image, "events");
                event.setImageUrl(imagePath); // Store relative path, not full URL
            }
            
            Event createdEvent = eventService.createEvent(event);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

     // Update event with image upload (multipart form)
    @PutMapping(value = "/events/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update event with image", description = "Update an existing event with optional image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Event> updateEventWithImage(
            @Parameter(description = "ID of the event to update") @PathVariable Long id,
            @Parameter(description = "Event title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Event description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Event date (ISO format)") @RequestParam(value = "eventDate", required = false) String eventDate,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Max participants") @RequestParam(value = "maxParticipants", required = false) String maxParticipants,
            @Parameter(description = "Image file (supports only one file)") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Get existing event
            Event existingEvent = eventService.getEventById(id);
            
            // Update fields if provided
            if (title != null) existingEvent.setTitle(title);
            if (description != null) existingEvent.setDescription(description);
            if (shortDescription != null) existingEvent.setShortDescription(shortDescription);
            if (eventDate != null) existingEvent.setEventDate(java.time.LocalDateTime.parse(eventDate));
            if (location != null) existingEvent.setLocation(location);
            if (maxParticipants != null) existingEvent.setMaxParticipants(Integer.parseInt(maxParticipants));
            
            // Handle image upload if provided (only one image)
            if (image != null && !image.isEmpty()) {
                // Delete old image if exists
                if (existingEvent.getImageUrl() != null) {
                    imageUploadService.deleteImage(existingEvent.getImageUrl());
                }
                
                String imagePath = imageUploadService.uploadImage(image, "events");
                existingEvent.setImageUrl(imagePath);
            }
            
            Event updatedEvent = eventService.updateEvent(id, existingEvent);
            return ResponseEntity.ok(updatedEvent);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin Volunteers Management
    @GetMapping("/volunteers")
    @Operation(summary = "Admin - Get all volunteers", description = "Retrieve all volunteer registrations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved volunteers")
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerService.getAllVolunteers();
        return ResponseEntity.ok(volunteers);
    }

    // ====================================
// Admin Blog Management
    // ====================================

    // Admin Donations Management
    @GetMapping("/donations")
    @Operation(summary = "Admin - Get all donations", description = "Retrieve all donations for admin management")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved donations",
                 content = @Content(mediaType = "application/json", 
                                 array = @ArraySchema(schema = @Schema(implementation = Donation.class))))
    public ResponseEntity<List<Donation>> getAllDonationsForAdmin() {
        List<Donation> donations = donationService.getAllDonations();
        return ResponseEntity.ok(donations);
    }

    @PostMapping("/blogs")
    @Operation(summary = "Admin - Create blog", description = "Create a new blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blog created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid blog data"),
            @ApiResponse(responseCode = "409", description = "Blog with slug already exists")
    })
    public ResponseEntity<BlogResponse> createBlog(@Valid @RequestBody BlogRequest request) {
        try {
            // Auto-calculate reading time if not provided
            if (request.getReadingTime() == null) {
                request.setReadingTime(blogService.calculateReadingTime(request.getContent()));
            }

            Blog blog = blogService.createBlog(request);
            BlogResponse response = BlogResponse.fromEntity(blog);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to create blog: " + e.getMessage());
        }
    }

    @GetMapping("/blogs")
    @Operation(summary = "Admin - Get all blogs", description = "Retrieve all blog posts for admin management")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved blogs")
    public ResponseEntity<List<BlogResponse>> getAllBlogs() {
        try {
            List<Blog> blogs = blogService.getAllBlogs();
            List<BlogResponse> responses = blogs.stream()
                    .map(BlogResponse::summaryFromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            // Enhanced error logging for production debugging
            System.err.println("ERROR in getAllBlogs: " + e.getMessage());
            e.printStackTrace();
            
            // Return empty list instead of 500 error to prevent frontend crashes
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/blogs/paginated")
    @Operation(summary = "Admin - Get paginated blogs", description = "Retrieve blogs with pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated blogs")
    public ResponseEntity<Page<BlogResponse>> getBlogsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Page<Blog> blogPage = blogService.getBlogsPaginated(page, size, sortBy, sortDir);
        Page<BlogResponse> responsePage = blogPage.map(BlogResponse::summaryFromEntity);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/blogs/{id}")
    @Operation(summary = "Admin - Get blog by ID", description = "Retrieve a specific blog post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved blog"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<BlogResponse> getBlogById(
            @Parameter(description = "Blog ID") @PathVariable Long id) {
        try {
            Blog blog = blogService.getBlogById(id);
            BlogResponse response = BlogResponse.fromEntity(blog);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/blogs/{id}")
    @Operation(summary = "Admin - Update blog", description = "Update an existing blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "400", description = "Invalid blog data")
    })
    public ResponseEntity<BlogResponse> updateBlog(
            @Parameter(description = "Blog ID") @PathVariable Long id,
            @Valid @RequestBody BlogRequest request) {
        try {
            // Auto-calculate reading time if not provided
            if (request.getReadingTime() == null) {
                request.setReadingTime(blogService.calculateReadingTime(request.getContent()));
            }

            Blog blog = blogService.updateBlog(id, request);
            BlogResponse response = BlogResponse.fromEntity(blog);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/blogs/{id}/update-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update blog image", description = "Update the featured image of a blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog image updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<BlogResponse> updateBlogImage(
            @Parameter(description = "Blog ID") @PathVariable Long id,
            @Parameter(description = "Featured image file (supports only one file)") @RequestParam("image") MultipartFile image) {
        try {
            BlogResponse response = blogService.updateBlogWithImage(id, image);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/blogs/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update blog with image", description = "Update an existing blog post with a new featured image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog updated successfully with image"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or image upload failed")
    })
    public ResponseEntity<BlogResponse> updateBlogWithImage(
            @Parameter(description = "Blog ID") @PathVariable Long id,
            @Parameter(description = "Featured image file (supports only one file)") @RequestParam(value = "image", required = true) MultipartFile image) {
        try {
            BlogResponse response = blogService.updateBlogWithImage(id, image);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/blogs/{id}")
    @Operation(summary = "Admin - Delete blog", description = "Delete a blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Blog deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<Void> deleteBlog(
            @Parameter(description = "Blog ID") @PathVariable Long id) {
        try {
            blogService.deleteBlog(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/blogs/{id}/publish")
    @Operation(summary = "Admin - Publish blog", description = "Publish a draft blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog published successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<BlogResponse> publishBlog(
            @Parameter(description = "Blog ID") @PathVariable Long id) {
        try {
            Blog blog = blogService.publishBlog(id);
            BlogResponse response = BlogResponse.fromEntity(blog);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/blogs/{id}/unpublish")
    @Operation(summary = "Admin - Unpublish blog", description = "Unpublish a published blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog unpublished successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<BlogResponse> unpublishBlog(
            @Parameter(description = "Blog ID") @PathVariable Long id) {
        try {
            Blog blog = blogService.unpublishBlog(id);
            BlogResponse response = BlogResponse.fromEntity(blog);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/blogs/by-status/{status}")
    @Operation(summary = "Admin - Get blogs by status", description = "Retrieve blogs filtered by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved blogs by status")
    public ResponseEntity<List<BlogResponse>> getBlogsByStatus(
            @Parameter(description = "Blog status") @PathVariable Blog.BlogStatus status) {
        List<Blog> blogs = blogService.getBlogsByStatus(status);
        List<BlogResponse> responses = blogs.stream()
                .map(BlogResponse::summaryFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/blogs/generate-slug")
    @Operation(summary = "Admin - Generate slug from title", description = "Generate a URL-friendly slug from blog title")
    @ApiResponse(responseCode = "200", description = "Slug generated successfully")
    public ResponseEntity<Map<String, String>> generateSlug(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        if (title == null || title.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Title is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String slug = blogService.generateSlug(title);
        Map<String, String> response = new HashMap<>();
        response.put("slug", slug);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/blogs/calculate-reading-time")
    @Operation(summary = "Admin - Calculate reading time", description = "Calculate estimated reading time for blog content")
    @ApiResponse(responseCode = "200", description = "Reading time calculated successfully")
    public ResponseEntity<Map<String, Integer>> calculateReadingTime(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            Map<String, Integer> errorResponse = new HashMap<>();
            errorResponse.put("error", 1);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        int readingTime = blogService.calculateReadingTime(content);
        Map<String, Integer> response = new HashMap<>();
        response.put("readingTime", readingTime);
        return ResponseEntity.ok(response);
    }

    // Blog with image upload
    @PostMapping(value = "/blogs/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Create blog with image", description = "Create a new blog post with featured image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blog created successfully with image"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or image upload failed")
    })
    public ResponseEntity<?> createBlogWithImage(
            @Parameter(description = "Blog title") @RequestParam("title") String title,
            @Parameter(description = "Blog subtitle") @RequestParam(value = "subtitle", required = false) String subtitle,
            @Parameter(description = "Blog slug") @RequestParam("slug") String slug,
            @Parameter(description = "Blog content") @RequestParam("content") String content,
            @Parameter(description = "Blog excerpt") @RequestParam(value = "excerpt", required = false) String excerpt,
            @Parameter(description = "Author name") @RequestParam("author") String author,
            @Parameter(description = "Author email") @RequestParam(value = "authorEmail", required = false) String authorEmail,
            @Parameter(description = "Blog status") @RequestParam("status") String status,
            @Parameter(description = "Tags (comma-separated)") @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "Meta title") @RequestParam(value = "metaTitle", required = false) String metaTitle,
            @Parameter(description = "Meta description") @RequestParam(value = "metaDescription", required = false) String metaDescription,
            @Parameter(description = "Is featured") @RequestParam(value = "isFeatured", defaultValue = "false") Boolean isFeatured,
            @Parameter(description = "Allow comments") @RequestParam(value = "allowComments", defaultValue = "true") Boolean allowComments,
            @Parameter(description = "Featured image file (supports only one file)") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title is required.");
        }
        if (slug == null || slug.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Slug is required.");
        }
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Content is required.");
        }

        try {
            // Handle image upload if provided (only one image)
            String featuredImage = null;
            if (image != null && !image.isEmpty()) {
                featuredImage = imageUploadService.uploadImage(image, "blogs");
            }
            
            // Create blog request
            BlogRequest request = BlogRequest.builder()
                    .title(title)
                    .subtitle(subtitle)
                    .slug(slug)
                    .content(content)
                    .excerpt(excerpt)
                    .featuredImage(featuredImage)
                    .author(author)
                    .authorEmail(authorEmail)
                    .status(Blog.BlogStatus.valueOf(status.toUpperCase()))
                    .readingTime(blogService.calculateReadingTime(content))
                    .tags(tags)
                    .metaTitle(metaTitle)
                    .metaDescription(metaDescription)
                    .isFeatured(isFeatured)
                    .allowComments(allowComments)
                    .build();
            
            Blog blog = blogService.createBlog(request);
            BlogResponse response = BlogResponse.fromEntity(blog);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Image upload failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ====================================
    // Admin Personal Cause Submissions Management
    // ====================================

    @DeleteMapping("/personal-cause-submissions/{id}")
    @Operation(summary = "Admin - Delete personal cause submission", description = "Delete a personal cause submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Personal cause submission deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    public ResponseEntity<Void> deletePersonalCauseSubmission(@Parameter(description = "ID of the personal cause submission to delete") @PathVariable Long id) {
        try {
            personalCauseSubmissionService.deleteSubmission(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/personal-cause-submissions")
    @Operation(summary = "Admin - Get all personal cause submissions", description = "Retrieve all personal cause submissions for admin review")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions",
                 content = @Content(mediaType = "application/json", 
                                  array = @ArraySchema(schema = @Schema(implementation = PersonalCauseSubmissionResponse.class))))
    public ResponseEntity<List<PersonalCauseSubmissionResponse>> getAllPersonalCauseSubmissions() {
        List<PersonalCauseSubmissionResponse> submissions = personalCauseSubmissionService.getAllSubmissions();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/personal-cause-submissions/{id}")
    @Operation(summary = "Admin - Get personal cause submission by ID", description = "Retrieve specific personal cause submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submission",
                        content = @Content(mediaType = "application/json", 
                                         schema = @Schema(implementation = PersonalCauseSubmissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    public ResponseEntity<PersonalCauseSubmissionResponse> getPersonalCauseSubmissionById(
            @Parameter(description = "ID of the submission to retrieve")
            @PathVariable Long id) {
        return personalCauseSubmissionService.getSubmissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/personal-cause-submissions/by-status/{status}")
    @Operation(summary = "Admin - Get submissions by status", description = "Retrieve personal cause submissions filtered by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions by status")
    public ResponseEntity<List<PersonalCauseSubmissionResponse>> getSubmissionsByStatus(
            @Parameter(description = "Submission status") @PathVariable PersonalCauseSubmission.SubmissionStatus status) {
        List<PersonalCauseSubmissionResponse> submissions = personalCauseSubmissionService.getSubmissionsByStatus(status);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/personal-cause-submissions/{id}/approve")
    @Operation(summary = "Admin - Approve personal cause submission", description = "Approve a personal cause submission and create corresponding cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission approved successfully"),
            @ApiResponse(responseCode = "404", description = "Submission not found"),
            @ApiResponse(responseCode = "400", description = "Invalid action request")
    })
    public ResponseEntity<PersonalCauseSubmissionResponse> approveSubmission(
            @Parameter(description = "ID of the submission to approve")
            @PathVariable Long id,
            @Valid @RequestBody SubmissionActionRequest actionRequest) {
        try {
            PersonalCauseSubmissionResponse response = personalCauseSubmissionService.approveSubmission(id, actionRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/personal-cause-submissions/{id}/reject")
    @Operation(summary = "Admin - Reject personal cause submission", description = "Reject a personal cause submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Submission not found"),
            @ApiResponse(responseCode = "400", description = "Invalid action request")
    })
    public ResponseEntity<PersonalCauseSubmissionResponse> rejectSubmission(
            @Parameter(description = "ID of the submission to reject")
            @PathVariable Long id,
            @Valid @RequestBody SubmissionActionRequest actionRequest) {
        try {
            PersonalCauseSubmissionResponse response = personalCauseSubmissionService.rejectSubmission(id, actionRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ====================================
    // Admin Donation Status Management
    // ====================================

    @PutMapping("/donations/{id}/status")
    @Operation(summary = "Admin - Update donation status", description = "Update donation status and send notification emails to donor and organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donation status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Donation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status or request data")
    })
    public ResponseEntity<Map<String, Object>> updateDonationStatus(
            @Parameter(description = "Donation ID") @PathVariable Long id,
            @Parameter(description = "New donation status") @RequestParam("status") String status,
            @Parameter(description = "Payment ID (optional)") @RequestParam(value = "paymentId", required = false) String paymentId,
            @Parameter(description = "Order ID (optional)") @RequestParam(value = "orderId", required = false) String orderId,
            @Parameter(description = "Organization email (optional)") @RequestParam(value = "orgEmail", required = false) String orgEmail) {
        
        try {
            // Validate status
            Donation.DonationStatus donationStatus;
            try {
                donationStatus = Donation.DonationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid status. Valid values: COMPLETED, FAILED, PENDING, REFUNDED");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Update donation status with email notifications
            Donation updatedDonation = donationService.updateDonationStatusWithNotification(
                    id, donationStatus, paymentId, orderId, orgEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Donation status updated to " + status + " and notification emails sent");
            response.put("donationId", updatedDonation.getId());
            response.put("newStatus", updatedDonation.getStatus());
            response.put("donorEmail", updatedDonation.getDonorEmail());
            response.put("donorName", updatedDonation.getDonorName());
            response.put("amount", updatedDonation.getAmount());
            response.put("currency", updatedDonation.getCurrency());
            response.put("causeName", updatedDonation.getCause() != null ? updatedDonation.getCause().getTitle() : "General Fund");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update donation status: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/donations/{id}/resend-notification")
    @Operation(summary = "Admin - Resend donation notification emails", description = "Manually resend notification emails for a donation to donor and organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification emails sent successfully"),
            @ApiResponse(responseCode = "404", description = "Donation not found"),
            @ApiResponse(responseCode = "400", description = "Error sending emails")
    })
    public ResponseEntity<Map<String, Object>> resendDonationNotification(
            @Parameter(description = "Donation ID") @PathVariable Long id,
            @Parameter(description = "Organization email (optional)") @RequestParam(value = "orgEmail", required = false) String orgEmail) {
        
        try {
            // Find the donation
            List<Donation> donations = donationService.getAllDonations();
            Donation donation = donations.stream()
                    .filter(d -> d.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

            // Send emails directly
            emailService.sendDonationEmails(donation, orgEmail != null ? orgEmail : adminEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification emails sent successfully");
            response.put("donationId", donation.getId());
            response.put("status", donation.getStatus());
            response.put("donorEmail", donation.getDonorEmail());
            response.put("sentToOrg", orgEmail != null ? orgEmail : adminEmail);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to send notification emails: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/donations/force-check-status")
    @Operation(summary = "Admin - Force check all donation statuses", description = "Manually trigger status checking for all donations with payment gateway and send notifications for any status changes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Force check initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Error initiating force check")
    })
    public ResponseEntity<Map<String, Object>> forceCheckDonationStatuses() {
        try {
            // Trigger the manual check
            monitoringService.forceCheckAllDonations();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Force check of all donation statuses has been initiated. Email notifications will be sent for any status changes found.");
            response.put("timestamp", DateTimeUtil.getCurrentKolkataTime());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to initiate force check: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
