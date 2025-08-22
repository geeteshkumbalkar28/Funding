package com.donorbox.backend.service;

import com.donorbox.backend.entity.Donation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationStatusMonitoringService {
    private final DonationService donationService;
    private final PaymentService paymentService;

    @Value("${admin.email:testing@alphaseam.com}")
    private String adminEmail;

    /**
     * Monitor and update donation statuses automatically
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void monitorDonationStatuses() {
        try {
            log.info("Starting automatic donation status monitoring...");
            
            // Get pending donations
            List<Donation> pendingDonations = donationService.getPendingDonations();
            
            if (!pendingDonations.isEmpty()) {
                log.info("Found {} pending donations. Checking statuses...", pendingDonations.size());
                processPendingDonations(pendingDonations);
            } else {
                log.debug("No pending donations found.");
            }
            
            // Also check recent donations (last 24 hours) for any status changes
            List<Donation> recentDonations = donationService.getRecentDonations(24);
            if (!recentDonations.isEmpty()) {
                log.info("Checking {} recent donations for status updates...", recentDonations.size());
                processRecentDonations(recentDonations);
            }
            
        } catch (Exception e) {
            log.error("Error during donation status monitoring", e);
        }
    }

    /**
     * Process pending donations for status updates
     */
    private void processPendingDonations(List<Donation> pendingDonations) {
        for (Donation donation : pendingDonations) {
            try {
                // Only check donations that have order IDs
                if (donation.getOrderId() != null && !donation.getOrderId().trim().isEmpty()) {
                    String currentStatus = checkPaymentStatusWithGateway(donation);
                    
                    if (currentStatus != null && !currentStatus.equals(donation.getStatus().name())) {
                        // Status has changed, update with notifications
                        Donation.DonationStatus newStatus = mapToStatus(currentStatus);
                        
                        donationService.updateDonationStatusWithNotification(
                            donation.getId(), 
                            newStatus, 
                            donation.getPaymentId(), 
                            donation.getOrderId(),
                            adminEmail
                        );
                        
                        log.info("Auto-updated donation {} from {} to {} with notifications", 
                                donation.getId(), donation.getStatus(), newStatus);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing pending donation {}", donation.getId(), e);
            }
        }
    }

    /**
     * Process recent donations for any status changes
     */
    private void processRecentDonations(List<Donation> recentDonations) {
        for (Donation donation : recentDonations) {
            try {
                // Only check donations that have order IDs
                if (donation.getOrderId() != null && !donation.getOrderId().trim().isEmpty()) {
                    String currentStatus = checkPaymentStatusWithGateway(donation);
                    
                    if (currentStatus != null && !currentStatus.equals(donation.getStatus().name())) {
                        // Status has changed, update with notifications
                        Donation.DonationStatus newStatus = mapToStatus(currentStatus);
                        
                        donationService.updateDonationStatusWithNotification(
                            donation.getId(), 
                            newStatus, 
                            donation.getPaymentId(), 
                            donation.getOrderId(),
                            adminEmail
                        );
                        
                        log.info("Auto-updated donation {} from {} to {} with notifications", 
                                donation.getId(), donation.getStatus(), newStatus);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing recent donation {}", donation.getId(), e);
            }
        }
    }

    /**
     * Check payment status with payment gateway
     */
    private String checkPaymentStatusWithGateway(Donation donation) {
        try {
            // Use PaymentService to check status with Razorpay or other gateway
            if (donation.getOrderId() != null) {
                return paymentService.getPaymentStatus(donation.getOrderId());
            }
        } catch (Exception e) {
            log.error("Error checking payment status for donation {}", donation.getId(), e);
        }
        return null;
    }

    /**
     * Map payment gateway status to our donation status
     */
    private Donation.DonationStatus mapToStatus(String gatewayStatus) {
        if (gatewayStatus == null) return Donation.DonationStatus.PENDING;
        
        switch (gatewayStatus.toLowerCase()) {
            case "paid":
            case "completed":
            case "success":
                return Donation.DonationStatus.COMPLETED;
            case "failed":
            case "error":
                return Donation.DonationStatus.FAILED;
            case "refunded":
                return Donation.DonationStatus.REFUNDED;
            default:
                return Donation.DonationStatus.PENDING;
        }
    }

    /**
     * Force check all donations manually (can be called via admin endpoint)
     */
    @Transactional
    public void forceCheckAllDonations() {
        log.info("Force checking all donation statuses...");
        
        CompletableFuture.runAsync(() -> {
            try {
                List<Donation> allDonations = donationService.getAllDonations();
                processRecentDonations(allDonations);
            } catch (Exception e) {
                log.error("Error during force check of all donations", e);
            }
        });
    }

    /**
     * Send follow-up emails for pending donations older than specified hours
     * LIMITED TO MAXIMUM 2 FOLLOW-UP EMAILS PER DONATION
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes = 1,800,000 milliseconds
    @Transactional
    public void sendFollowUpEmails() {
        try {
            log.info("Sending follow-up emails for old pending donations (max 2 per donation)...");
            
            // Get pending donations older than 2 hours that haven't exceeded follow-up limit
            List<Donation> oldPendingDonations = donationService.getOldPendingDonationsForFollowup(2, 2); // 2+ hours old, less than 2 follow-ups
            
            int emailsSent = 0;
            for (Donation donation : oldPendingDonations) {
                try {
                    // Send follow-up email and increment count
                    donationService.sendFollowUpEmailWithCount(donation, adminEmail);
                    emailsSent++;
                    
                    log.info("Sent follow-up email #{} for pending donation {} (created: {})", 
                            donation.getFollowupEmailCount(), donation.getId(), donation.getCreatedAt());
                    
                    // Check if this donation has now reached the limit
                    if (donation.getFollowupEmailCount() >= 2) {
                        log.info("Donation {} has reached maximum follow-up emails (2). No more follow-ups will be sent.", 
                                donation.getId());
                    }
                } catch (Exception e) {
                    log.error("Error sending follow-up email for donation {}", donation.getId(), e);
                }
            }
            
            log.info("Follow-up email process completed. {} emails sent.", emailsSent);
            
        } catch (Exception e) {
            log.error("Error during follow-up email sending", e);
        }
    }
}
