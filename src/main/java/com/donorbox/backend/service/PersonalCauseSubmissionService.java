package com.donorbox.backend.service;

import com.donorbox.backend.entity.PersonalCauseSubmission;
import com.donorbox.backend.entity.Cause;
import com.donorbox.backend.dto.PersonalCauseSubmissionRequest;
import com.donorbox.backend.dto.PersonalCauseSubmissionResponse;
import com.donorbox.backend.dto.SubmissionActionRequest;
import com.donorbox.backend.repository.PersonalCauseSubmissionRepository;
import com.donorbox.backend.repository.CauseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalCauseSubmissionService {

    private final PersonalCauseSubmissionRepository submissionRepository;
    private final CauseRepository causeRepository;
    private final EmailService emailService;

    @Value("${admin.email:testing@alphaseam.com}")
    private String adminEmail;

    public List<PersonalCauseSubmissionResponse> getAllSubmissions() {
        return submissionRepository.findAllOrderByCreatedAtDesc().stream()
                .map(PersonalCauseSubmissionResponse::summaryFromEntity)
                .toList();
    }

    public Optional<PersonalCauseSubmissionResponse> getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .map(PersonalCauseSubmissionResponse::fromEntity);
    }

    public List<PersonalCauseSubmissionResponse> getSubmissionsByEmail(String email) {
        return submissionRepository.findBySubmitterEmailOrderByCreatedAtDesc(email).stream()
                .map(PersonalCauseSubmissionResponse::summaryFromEntity)
                .toList();
    }

    public List<PersonalCauseSubmissionResponse> getSubmissionsByStatus(PersonalCauseSubmission.SubmissionStatus status) {
        return submissionRepository.findByStatus(status).stream()
                .map(PersonalCauseSubmissionResponse::summaryFromEntity)
                .toList();
    }

    @Transactional
    public PersonalCauseSubmissionResponse createSubmission(PersonalCauseSubmissionRequest request) {
        return createSubmission(request, null);
    }

@Transactional
public PersonalCauseSubmissionResponse createSubmission(PersonalCauseSubmissionRequest request, String imageUrl) {
    return createSubmission(request, imageUrl, null, null, null, null);
}

@Transactional
public PersonalCauseSubmissionResponse createSubmission(PersonalCauseSubmissionRequest request, String imageUrl, String videoUrl) {
    return createSubmission(request, imageUrl, videoUrl, null, null, null);
}

@Transactional
    public PersonalCauseSubmissionResponse createSubmission(PersonalCauseSubmissionRequest request, String imageUrl, String videoUrl, String proofDocumentUrl, String proofDocumentName, String proofDocumentType) {
        PersonalCauseSubmission submission = PersonalCauseSubmission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .targetAmount(request.getTargetAmount())
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .proofDocumentUrl(proofDocumentUrl)
                .proofDocumentName(proofDocumentName)
.proofDocumentType(proofDocumentType)
                .submitterName(request.getSubmitterName())
                .submitterEmail(request.getSubmitterEmail())
                .submitterPhone(request.getSubmitterPhone())
                .submitterMessage(request.getSubmitterMessage())
                .category(request.getCategory())
                .location(request.getLocation())
                .endDate(request.getEndDate())
                .status(PersonalCauseSubmission.SubmissionStatus.PENDING)
                .build();
        PersonalCauseSubmission savedSubmission = submissionRepository.save(submission);

        // Send email to submitter
        String subject = "Your Cause Submission is Under Review";
        String htmlContent = "<p>Thank you, " + request.getSubmitterName() + 
    ", for submitting your cause titled '<strong>" + request.getTitle() + 
    "</strong>'. Your submission is under review. We will notify you upon approval or rejection.</p>"
    + "<br>"
    + "Best regards,<br>"
    + "GreenDharti";
        emailService.sendSubmissionStatusEmail(request.getSubmitterEmail(), subject, htmlContent);

        // Send notification to admin/organization
        String adminSubject = "New Personal Cause Submission - " + request.getTitle();
String adminHtmlContent = "<h3>New Personal Cause Submission</h3>"
        + "<p>A new personal cause has been submitted for review:</p>"
        + "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>"
        + "<p><strong>Title:</strong> " + request.getTitle() + "</p>"
        + "<p><strong>Submitter:</strong> " + request.getSubmitterName() + " (" + request.getSubmitterEmail() + ")</p>"
        + "<p><strong>Mobile Number:</strong> " + request.getSubmitterPhone() + "</p>"   // ✅ Phone added here
        + "<p><strong>Target Amount:</strong> INR " + request.getTargetAmount() + "</p>"
        + "<p><strong>Category:</strong> " + (request.getCategory() != null ? request.getCategory() : "Not specified") + "</p>"
        + "<p><strong>Location:</strong> " + (request.getLocation() != null ? request.getLocation() : "Not specified") + "</p>"
        + "<p><strong>Description:</strong> " + request.getDescription() + "</p>"
        + (proofDocumentUrl != null ? "<p><strong>Proof Document:</strong> " + proofDocumentName + " (" + proofDocumentType + ")</p>" : "<p><strong>Proof Document:</strong> Not provided</p>")
        + "</div>"
        + "<p>Please review this submission in the admin dashboard.</p>";
emailService.sendSubmissionStatusEmail(adminEmail, adminSubject, adminHtmlContent);

return PersonalCauseSubmissionResponse.fromEntity(savedSubmission);

    }

    @Transactional
    public PersonalCauseSubmissionResponse approveSubmission(Long id, SubmissionActionRequest actionRequest) {
        PersonalCauseSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        submission.setStatus(PersonalCauseSubmission.SubmissionStatus.APPROVED);
        submission.setApprovedBy(actionRequest.getApprovedBy());
        submission.setAdminNotes(actionRequest.getAdminNotes());

// Create Cause entity
        // Determine media type based on what's available
        Cause.MediaType mediaType = Cause.MediaType.NONE;
        if (submission.getImageUrl() != null && submission.getVideoUrl() != null) {
            mediaType = Cause.MediaType.BOTH;
        } else if (submission.getImageUrl() != null) {
            mediaType = Cause.MediaType.IMAGE;
        } else if (submission.getVideoUrl() != null) {
            mediaType = Cause.MediaType.VIDEO;
        }
        
        Cause cause = Cause.builder()
                .title(actionRequest.getModifiedTitle() != null ? actionRequest.getModifiedTitle() : submission.getTitle())
                .description(actionRequest.getModifiedDescription() != null ? actionRequest.getModifiedDescription() : submission.getDescription())
                .shortDescription(actionRequest.getModifiedShortDescription() != null ? actionRequest.getModifiedShortDescription() : submission.getShortDescription())
                .targetAmount(submission.getTargetAmount())
                .imageUrl(submission.getImageUrl())
                .videoUrl(submission.getVideoUrl())
                .mediaType(mediaType)
                .category(actionRequest.getModifiedCategory() != null ? actionRequest.getModifiedCategory() : submission.getCategory())
                .location(actionRequest.getModifiedLocation() != null ? actionRequest.getModifiedLocation() : submission.getLocation())
                .endDate(submission.getEndDate())
                .build();
        Cause savedCause = causeRepository.save(cause);

        // Link Cause to Submission
        submission.setCauseId(savedCause.getId());
        submissionRepository.save(submission);

        // Notify submitter of approval
        String subject = "Your Cause Submission Has Been Approved";
        String htmlContent = "<p>Congratulations, " + submission.getSubmitterName() + ", your cause titled '<strong>" 
                + submission.getTitle() + "</strong>' has been approved and is now live on our platform!</p>"
                 + "<br>"
    + "Best regards,<br>"
    + "GreenDharti";
        emailService.sendSubmissionStatusEmail(submission.getSubmitterEmail(), subject, htmlContent);

        return PersonalCauseSubmissionResponse.fromEntity(submission);
    }

    @Transactional
    public PersonalCauseSubmissionResponse rejectSubmission(Long id, SubmissionActionRequest actionRequest) {
        PersonalCauseSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        submission.setStatus(PersonalCauseSubmission.SubmissionStatus.REJECTED);
        submission.setAdminNotes(actionRequest.getAdminNotes());
        submissionRepository.save(submission);

        // Notify submitter of rejection
        String subject = "Your Cause Submission Has Been Rejected";
        String htmlContent = "<p>Hello, " + submission.getSubmitterName() + ". We regret to inform you that your cause titled '<strong>"
                + submission.getTitle() + "</strong>' has been rejected. Admin Notes: " + actionRequest.getAdminNotes() + "</p>"
                 + "<br>"
    + "Best regards,<br>"
    + "GreenDharti";
        emailService.sendSubmissionStatusEmail(submission.getSubmitterEmail(), subject, htmlContent);

        return PersonalCauseSubmissionResponse.fromEntity(submission);
    }

    @Transactional
    public void deleteSubmission(Long id) {
        PersonalCauseSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with id: " + id));
        
        submissionRepository.delete(submission);
    }
}
