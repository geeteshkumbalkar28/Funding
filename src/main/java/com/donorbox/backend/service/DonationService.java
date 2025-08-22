package com.donorbox.backend.service;

import com.donorbox.backend.dto.DonationRequest;
import com.donorbox.backend.entity.Cause;
import com.donorbox.backend.entity.Donation;
import com.donorbox.backend.repository.CauseRepository;
import com.donorbox.backend.repository.DonationRepository;
import com.donorbox.backend.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final CauseRepository causeRepository;
    private final EmailSchedulerService emailSchedulerService;
    private final EmailService emailService;

    @Value("${admin.email:testing@alphaseam.com}")
    private String adminEmail;

    @Transactional
    public Donation createDonation(DonationRequest request) {
        Cause cause = null;
        if (request.getCauseId() != null) {
            cause = causeRepository.findById(request.getCauseId())
                    .orElseThrow(() -> new IllegalArgumentException("Cause not found"));
        }

        Donation donation = Donation.builder()
                .donorName(request.getDonorName())
                .donorEmail(request.getDonorEmail())
                .donorPhone(request.getDonorPhone())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .cause(cause)
                .message(request.getMessage())
                .paymentMethod(request.getPaymentMethod())
                .status(Donation.DonationStatus.PENDING)
                .build();

        return donationRepository.save(donation);
    }

    @Transactional(readOnly = true)
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Donation getDonationById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + id));
    }

    @Transactional
    public Donation updateDonationStatus(Long donationId, Donation.DonationStatus status, String paymentId, String orderId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));

        donation.setStatus(status);
        if (paymentId != null) {
            donation.setPaymentId(paymentId);
        }
        if (orderId != null) {
            donation.setOrderId(orderId);
        }

        Donation updatedDonation = donationRepository.save(donation);

        // ✅ Update Cause currentAmount when donation is successful
        if (status == Donation.DonationStatus.COMPLETED && donation.getCause() != null) {
            Cause cause = donation.getCause();

            if (cause.getCurrentAmount() == null) {
                cause.setCurrentAmount(BigDecimal.ZERO);
            }

            cause.setCurrentAmount(cause.getCurrentAmount().add(donation.getAmount()));

            causeRepository.save(cause);
        }

        // ✅ Send/schedule email
        emailSchedulerService.scheduleDonationEmail(updatedDonation.getId(), adminEmail);

        return updatedDonation;
    }

    @Transactional(readOnly = true)
    public Donation findByOrderId(String orderId) {
        return donationRepository.findByOrderId(orderId)
                .orElse(null);
    }

    @Transactional
    public Donation updateDonationWithOrderId(Long donationId, String orderId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));

        donation.setOrderId(orderId);
        return donationRepository.save(donation);
    }

    /**
     * Update donation status with email notifications - can be called from admin or webhook
     */
    @Transactional
    public Donation updateDonationStatusWithNotification(Long donationId, Donation.DonationStatus status, String paymentId, String orderId, String orgEmail) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));

        Donation.DonationStatus oldStatus = donation.getStatus();
        donation.setStatus(status);
        if (paymentId != null) {
            donation.setPaymentId(paymentId);
        }
        if (orderId != null) {
            donation.setOrderId(orderId);
        }

        Donation updatedDonation = donationRepository.save(donation);

        // ✅ Update Cause currentAmount when donation is successful
        if (status == Donation.DonationStatus.COMPLETED && donation.getCause() != null) {
            Cause cause = donation.getCause();

            if (cause.getCurrentAmount() == null) {
                cause.setCurrentAmount(BigDecimal.ZERO);
            }

            cause.setCurrentAmount(cause.getCurrentAmount().add(donation.getAmount()));
            causeRepository.save(cause);
        }

        // ✅ Always send email notification when status changes
        if (oldStatus != status || status == Donation.DonationStatus.PENDING) {
            emailSchedulerService.scheduleDonationEmail(updatedDonation.getId(), orgEmail != null ? orgEmail : adminEmail);
        }

        return updatedDonation;
    }

    // Methods for automated monitoring
    @Transactional(readOnly = true)
    public List<Donation> getPendingDonations() {
        return donationRepository.findByStatus(Donation.DonationStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Donation> getRecentDonations(int hoursBack) {
        java.time.LocalDateTime cutoffTime = DateTimeUtil.getCurrentKolkataTime().minusHours(hoursBack);
        return donationRepository.findByCreatedAtAfter(cutoffTime);
    }

    @Transactional(readOnly = true)
    public List<Donation> getOldPendingDonations(int hoursBack) {
        java.time.LocalDateTime cutoffTime = DateTimeUtil.getCurrentKolkataTime().minusHours(hoursBack);
        return donationRepository.findByStatusAndCreatedAtBefore(Donation.DonationStatus.PENDING, cutoffTime);
    }

    @Transactional(readOnly = true)
    public List<Donation> getOldPendingDonationsForFollowup(int hoursBack, int maxFollowupCount) {
        java.time.LocalDateTime cutoffTime = DateTimeUtil.getCurrentKolkataTime().minusHours(hoursBack);
        return donationRepository.findByStatusAndCreatedAtBeforeAndFollowupEmailCountLessThan(
            Donation.DonationStatus.PENDING, cutoffTime, maxFollowupCount);
    }

    @Transactional
    public void sendFollowUpEmail(Donation donation, String orgEmail) {
        try {
            emailService.sendDonationEmails(donation, orgEmail);
            System.out.println("Follow-up email sent for donation " + donation.getId());
        } catch (Exception e) {
            System.err.println("Error sending follow-up email for donation " + donation.getId() + ": " + e.getMessage());
        }
    }

    @Transactional
    public void sendFollowUpEmailWithCount(Donation donation, String orgEmail) {
        try {
            // Increment follow-up email count
            donation.setFollowupEmailCount(donation.getFollowupEmailCount() + 1);
            donationRepository.save(donation);
            
            // Send the follow-up email
            emailService.sendDonationEmails(donation, orgEmail);
            System.out.println("Follow-up email #" + donation.getFollowupEmailCount() + " sent for donation " + donation.getId());
        } catch (Exception e) {
            System.err.println("Error sending follow-up email for donation " + donation.getId() + ": " + e.getMessage());
        }
    }
}
