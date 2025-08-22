package com.donorbox.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.donorbox.backend.entity.Donation;
import com.donorbox.backend.repository.DonationRepository;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailSchedulerService {

    private final TaskScheduler taskScheduler;
    private final DonationRepository donationRepository;
    private final EmailService emailService;

    public void scheduleDonationEmail(Long donationId, String orgEmail) {
        donationRepository.findById(donationId).ifPresent(donation -> {
            Donation.DonationStatus status = donation.getStatus();

            switch (status) {
                case COMPLETED:
                    // Send immediately for completed payments
                    emailService.sendDonationEmails(donation, orgEmail);
                    break;
                    
                case FAILED:
                    // Send immediately for failed payments
                    emailService.sendDonationEmails(donation, orgEmail);
                    break;
                    
                case REFUNDED:
                    // Send immediately for refunded payments
                    emailService.sendDonationEmails(donation, orgEmail);
                    break;
                    
                case PENDING:
                    // Send immediately for pending status as well
                    // Users should know their donation is being processed
                    emailService.sendDonationEmails(donation, orgEmail);
                    
                    // Also schedule a follow-up email after 10 minutes to check final status
                    Instant followUpTime = Instant.now().plusMillis(TimeUnit.MINUTES.toMillis(10));
                    taskScheduler.schedule(() -> {
                        donationRepository.findById(donationId).ifPresent(latestDonation -> {
                            // Only send follow-up if status has changed from PENDING
                            if (latestDonation.getStatus() != Donation.DonationStatus.PENDING) {
                                emailService.sendDonationEmails(latestDonation, orgEmail);
                            }
                        });
                    }, followUpTime);
                    break;
                    
                default:
                    // Send immediately for any unknown status
                    emailService.sendDonationEmails(donation, orgEmail);
                    break;
            }
        });
    }
}
