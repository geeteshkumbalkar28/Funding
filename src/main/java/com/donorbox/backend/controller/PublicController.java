package com.donorbox.backend.controller;
 
import com.donorbox.backend.dto.*;
import com.donorbox.backend.entity.*;
import com.donorbox.backend.service.*;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public API", description = "Public endpoints for frontend")
public class PublicController {
 
    private final DonationService donationService;
    private final CauseService causeService;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final ContactService contactService;
    private final StatsService statsService;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final BlogService blogService;
 
    // Donation Endpoints
    @PostMapping("/donate")
    @Operation(summary = "Make a donation", description = "Create a new donation record")
@ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created successfully", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Donation.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Donation> makeDonation(@Valid @RequestBody DonationRequest request) {
        Donation donation = donationService.createDonation(request);
        return new ResponseEntity<>(donation, HttpStatus.CREATED);
    }
 
    @PostMapping("/donate-with-notifications")
    @Operation(summary = "Make a donation with email notifications",
               description = "Create a donation record and send confirmation emails to donor and organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created and notifications sent successfully",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Donation.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Donation> makeDonationWithNotifications(@Valid @RequestBody DonationRequest request) {
        try {
            // Create the donation
            Donation donation = donationService.createDonation(request);
           
            // Send email notifications using centralized EmailService
            // emailService.sendDonationEmails(donation, "testing@alphaseam.com");
           
            log.info("Donation created with ID: {} and notifications sent", donation.getId());
           
            return new ResponseEntity<>(donation, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating donation with notifications", e);
            throw new RuntimeException("Failed to process donation with notifications: " + e.getMessage());
        }
    }
 
    @GetMapping("/donations")
    @Operation(summary = "Get all donations", description = "Retrieve all donation records")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved donations",
                 content = @Content(mediaType = "application/json",
                                  array = @ArraySchema(schema = @Schema(implementation = Donation.class))))
    public ResponseEntity<List<Donation>> getAllDonations() {
        List<Donation> donations = donationService.getAllDonations();
        return ResponseEntity.ok(donations);
    }
 
    // Causes Endpoints
    @GetMapping("/causes")
    @Operation(summary = "Get all causes", description = "Retrieve all active causes")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved causes")
    public ResponseEntity<List<Cause>> getAllCauses() {
        List<Cause> causes = causeService.getAllCausesEntities();
        return ResponseEntity.ok(causes);
    }
 
    @GetMapping("/causes/{id}")
    @Operation(summary = "Get cause by ID", description = "Retrieve detailed information about a specific cause")
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
 
    // Events Endpoints
    @GetMapping("/events")
    @Operation(summary = "Get all events", description = "Retrieve all events")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved events")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }
 
    @GetMapping("/events/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieve detailed information about a specific event")
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
 
    // Volunteer Endpoints
    @PostMapping("/volunteer/register")
    @Operation(summary = "Register as volunteer", description = "Register a new volunteer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Volunteer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Volunteer> registerVolunteer(@Valid @RequestBody VolunteerRequest request) {
        Volunteer volunteer = volunteerService.registerVolunteer(request);
        return new ResponseEntity<>(volunteer, HttpStatus.CREATED);
    }
 
    // Contact Endpoints
    @PostMapping("/contact/send")
    @Operation(summary = "Send contact message", description = "Submit a contact form message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Message> sendContactMessage(@Valid @RequestBody ContactRequest request) {
        Message message = contactService.sendMessage(request);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
 
    // Homepage Stats Endpoint
    @GetMapping("/homepage-stats")
    @Operation(summary = "Get homepage statistics", description = "Retrieve real-time statistics for the homepage")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    public ResponseEntity<HomepageStatsResponse> getHomepageStats() {
        HomepageStatsResponse stats = statsService.getHomepageStats();
        return ResponseEntity.ok(stats);
    }
   
    // Payment Endpoints
    @GetMapping("/payment/currencies")
    @Operation(summary = "Get supported currencies", description = "Retrieve list of supported currencies for international payments")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supported currencies")
    public ResponseEntity<java.util.Map<String, String>> getSupportedCurrencies() {
        java.util.Map<String, String> currencies = paymentService.getSupportedCurrencies();
        return ResponseEntity.ok(currencies);
    }
 
    @PostMapping("/payment/create-order")
    @Operation(summary = "Create payment order", description = "Create a Razorpay order for processing payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<java.util.Map<String, Object>> createPaymentOrder(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "INR") String currency,
            @RequestParam String receiptId) {
        try {
            com.razorpay.Order order = paymentService.createOrder(amount, currency, receiptId);
           
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("status", order.get("status"));
           
            // log.info("Payment order created successfully: {}", order.get("id"));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating payment order", e);
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Failed to create payment order: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
 
    @PostMapping("/donate-and-pay")
    @Operation(summary = "Create donation and payment order",
               description = "Create a donation record and corresponding Razorpay payment order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created and payment order generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<java.util.Map<String, Object>> createDonationAndPaymentOrder(@Valid @RequestBody DonationRequest request) {
        try {
            // Create the donation
            Donation donation = donationService.createDonation(request);
           
            // Generate a unique receipt ID using donation ID
            String receiptId = "DON_" + donation.getId() + "_" + System.currentTimeMillis();
           
            // Create Razorpay order
            com.razorpay.Order order = paymentService.createOrder(request.getAmount(), request.getCurrency(), receiptId);
           
            // Update donation with order ID
            donationService.updateDonationWithOrderId(donation.getId(), order.get("id").toString());
           
            // Prepare response
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("donationId", donation.getId());
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("status", order.get("status"));
            response.put("donorName", donation.getDonorName());
            response.put("donorEmail", donation.getDonorEmail());
            response.put("causeName", donation.getCause() != null ? donation.getCause().getTitle() : "General Donation");
             
            log.info("Donation created with ID: {} and payment order: {}", donation.getId(), order.get("id"));
           
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating donation and payment order", e);
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Failed to create donation and payment order: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
 
    @PostMapping("/payment/verify")
    @Operation(summary = "Verify payment", description = "Verify payment transaction and send notification emails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment verification completed",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment verification data")
    })
    public ResponseEntity<Boolean> verifyPayment(@Valid @RequestBody PaymentVerificationRequest request) {
        boolean isVerified = paymentService.verifyPaymentAndSendNotifications(
            request.getOrderId(),
            request.getPaymentId(),
            request.getSignature(),
            request.getDonorEmail(),
            request.getDonorName(),
            request.getAmount(),
            request.getCurrency(),
            request.getCauseName()
        );
       
        // Update donation status if payment verification is successful
        if (isVerified) {
            try {
                // Find donation by order ID and update status
                Donation donation = donationService.findByOrderId(request.getOrderId());
                if (donation != null) {
                    donationService.updateDonationStatus(
                        donation.getId(),
                        Donation.DonationStatus.COMPLETED,
                        request.getPaymentId(),
                        request.getOrderId()
                    );
                   
                    log.info("Donation status updated to COMPLETED for order: {}", request.getOrderId());
                }
            } catch (Exception e) {
                log.error("Error updating donation status for order: {}", request.getOrderId(), e);
                // Don't fail payment verification due to status update issues
            }
        }
       
        return ResponseEntity.ok(isVerified);
    }
   
    // ====================================
    // Public Blog Endpoints
    // ====================================

    @GetMapping("/blogs")
    @Operation(summary = "Get published blogs", description = "Retrieve all published blog posts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved published blogs")
    public ResponseEntity<List<BlogResponse>> getPublishedBlogs() {
        try {
            List<Blog> blogs = blogService.getPublishedBlogs();
            List<BlogResponse> responses = blogs.stream()
                    .map(BlogResponse::summaryFromEntity)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved {} published blogs", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error retrieving published blogs", e);
            // Return empty list instead of error to prevent frontend issues
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/blogs/{slug}")
    @Operation(summary = "Get blog by slug", description = "Retrieve a specific blog post by slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved blog"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<BlogResponse> getBlogBySlug(
            @Parameter(description = "Blog slug") @PathVariable String slug) {
        try {
            Blog blog = blogService.getBlogBySlug(slug, true); // increment view count
            
            // Only return published blogs for public access
            if (!blog.isPublished()) {
                return ResponseEntity.notFound().build();
            }
            
            BlogResponse response = BlogResponse.fromEntity(blog);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Blog not found with slug: {}", slug);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/blogs/featured")
    @Operation(summary = "Get featured blogs", description = "Retrieve featured blog posts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved featured blogs")
    public ResponseEntity<List<BlogResponse>> getFeaturedBlogs() {
        try {
            List<Blog> blogs = blogService.getFeaturedBlogs();
            List<BlogResponse> responses = blogs.stream()
                    .map(BlogResponse::summaryFromEntity)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved {} featured blogs", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error retrieving featured blogs", e);
            // Return empty list instead of error to prevent frontend issues
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}