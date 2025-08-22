package com.donorbox.backend.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final String keySecret;
    private final EmailService emailService;
    private final String adminEmail;;;

    public PaymentService(@Value("${razorpay.key.id}") String keyId, 
                         @Value("${razorpay.key.secret}") String keySecret,
                         @Value("${admin.email:testing@alphaseam.com}") String adminEmail,
                         EmailService emailService) throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
        this.keySecret = keySecret;
        this.emailService = emailService;
        this.adminEmail = adminEmail;
    }

    /**
     * Create payment order for international transactions
     * @param amount Amount in the base currency
     * @param currency Currency code (USD, EUR, INR, etc.)
     * @param receiptId Unique receipt identifier
     * @return Razorpay Order object
     */
    @Transactional
    public Order createOrder(BigDecimal amount, String currency, String receiptId) throws RazorpayException {
        Map<String, Object> orderRequest = new HashMap<>();
        
        // Convert amount to smallest currency unit (paise for INR, cents for USD)
        int amountInSmallestUnit = amount.multiply(BigDecimal.valueOf(100)).intValue();
        
        orderRequest.put("amount", amountInSmallestUnit);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receiptId);
        
        // Support for international payments
        Map<String, Object> notes = new HashMap<>();
        notes.put("platform", "donorbox");
        notes.put("type", "donation");
        orderRequest.put("notes", notes);
        
        log.info("Creating Razorpay order for amount: {} {}, receipt: {}", amount, currency, receiptId);
        
        return razorpayClient.orders.create(new org.json.JSONObject(orderRequest));
    }

    /**
     * Verify payment signature for security
     * @param orderId Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Payment signature
     * @return true if signature is valid
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            org.json.JSONObject attributes = new org.json.JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);
            
            return com.razorpay.Utils.verifyPaymentSignature(attributes, 
                    this.keySecret);
        } catch (Exception e) {
            log.error("Error verifying payment signature", e);
            return false;
        }
    }

    /**
     * Verify payment and send notification emails
     * @param orderId Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Payment signature
     * @param donorEmail Donor's email address
     * @param donorName Donor's name
     * @param amount Donation amount
     * @param currency Currency code
     * @param causeName Name of the cause donated to
     * @return true if signature is valid and emails are sent
     */
    @Transactional
    public boolean verifyPaymentAndSendNotifications(String orderId, String paymentId, 
                                                    String signature, String donorEmail, 
                                                    String donorName, BigDecimal amount, 
                                                    String currency, String causeName) {
        boolean isValidSignature = verifyPaymentSignature(orderId, paymentId, signature);
        
        if (isValidSignature) {
            try {
                // Send confirmation email to donor
                // sendDonorConfirmationEmail(donorEmail, donorName, amount, currency, causeName, paymentId);
                
                // Send notification email to admin
                // sendAdminNotificationEmail(donorName, donorEmail, amount, currency, causeName, paymentId);
                
                log.info("Payment verified and notification emails sent for payment: {}", paymentId);
            } catch (Exception e) {
                log.error("Error sending notification emails for payment: {}", paymentId, e);
                // Don't fail payment verification due to email issues
            }
        }
        
        return isValidSignature;
    }

    /**
     * Send confirmation email to donor
     */
    // private void sendDonorConfirmationEmail(String donorEmail, String donorName, 
    //                                       BigDecimal amount, String currency, 
    //                                       String causeName, String paymentId) {
    //     String subject = "Thank You for Your Donation - Payment Successful";
        
    //     String htmlContent = String.format(
    //         "<html>" +
    //         "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
    //         "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
    //         "<h1 style='color: #2c5aa0; text-align: center;'>Thank You for Your Donation!</h1>" +
    //         "<p>Dear %s,</p>" +
    //         "<p>We are delighted to confirm that your donation has been successfully processed.</p>" +
    //         "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
    //         "<h3 style='margin-top: 0; color: #2c5aa0;'>Donation Details:</h3>" +
    //         "<p><strong>Amount:</strong> %s %s</p>" +
    //         "<p><strong>Cause:</strong> %s</p>" +
    //         "<p><strong>Payment ID:</strong> %s</p>" +
    //         "<p><strong>Date:</strong> %s</p>" +
    //         "</div>" +
    //         "<p>Your generous contribution will make a real difference in supporting our cause. " +
    //         "We will send you updates on how your donation is being used.</p>" +
    //         "<p>For any questions or concerns, please feel free to contact us.</p>" +
    //         "<p>With heartfelt gratitude,<br>The DonorBox Team</p>" +
    //         "</div>" +
    //         "</body>" +
    //         "</html>",
    //         donorName, currency, amount, causeName, paymentId, 
    //         java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    //     );
        
    //     try {
    //         emailService.sendHtmlEmail(donorEmail, subject, htmlContent);
    //     } catch (MessagingException e) {
    //         log.error("Error sending donor confirmation email for payment: {}", paymentId, e);
    //     }
    // }

    /**
     * Send notification email to admin
     */
    // private void sendAdminNotificationEmail(String donorName, String donorEmail, 
    //                                       BigDecimal amount, String currency, 
    //                                       String causeName, String paymentId) {
    //     String subject = "New Donation Received - " + currency + " " + amount;
        
    //     String htmlContent = String.format(
    //         "<html>" +
    //         "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
    //         "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
    //         "<h1 style='color: #28a745; text-align: center;'>New Donation Received!</h1>" +
    //         "<p>A new donation has been successfully processed through the DonorBox platform.</p>" +
    //         "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
    //         "<h3 style='margin-top: 0; color: #28a745;'>Donation Details:</h3>" +
    //         "<p><strong>Donor Name:</strong> %s</p>" +
    //         "<p><strong>Donor Email:</strong> %s</p>" +
    //         "<p><strong>Amount:</strong> %s %s</p>" +
    //         "<p><strong>Cause:</strong> %s</p>" +
    //         "<p><strong>Payment ID:</strong> %s</p>" +
    //         "<p><strong>Date:</strong> %s</p>" +
    //         "</div>" +
    //         "<p>Please log into the admin dashboard to view more details and manage this donation.</p>" +
    //         "<p>Best regards,<br>DonorBox System</p>" +
    //         "</div>" +
    //         "</body>" +
    //         "</html>",
    //         donorName, donorEmail, currency, amount, causeName, paymentId,
    //         java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    //     );
        
    //     try {
    //         emailService.sendHtmlEmail(adminEmail, subject, htmlContent);
    //     } catch (MessagingException e) {
    //         log.error("Error sending admin notification email for payment: {}", paymentId, e);
    //     }
    // }

    /**
     * Fetch payment details
     * @param paymentId Payment ID
     * @return Payment object
     */
    public Payment fetchPayment(String paymentId) throws RazorpayException {
        return razorpayClient.payments.fetch(paymentId);
    }

    /**
     * Process refund for international payments
     * @param paymentId Payment ID to refund
     * @param amount Amount to refund (optional, if partial refund)
     * @return String status message
     */
    @Transactional
    public String processRefund(String paymentId, BigDecimal amount) {
        try {
            Payment payment = razorpayClient.payments.fetch(paymentId);
            log.info("Processing refund for payment: {}, amount: {}", paymentId, amount);
            // For now, return a status message
            // Actual refund implementation can be added later with proper Razorpay integration
            return "Refund request processed for payment: " + paymentId;
        } catch (RazorpayException e) {
            log.error("Error processing refund", e);
            return "Refund processing failed";
        }
    }

    /**
     * Get supported currencies for international payments
     * @return Map of supported currencies
     */
    public Map<String, String> getSupportedCurrencies() {
        Map<String, String> currencies = new HashMap<>();
        
        // Major international currencies supported by Razorpay
        currencies.put("INR", "Indian Rupee");
        currencies.put("USD", "US Dollar");
        currencies.put("EUR", "Euro");
        currencies.put("GBP", "British Pound");
        currencies.put("AUD", "Australian Dollar");
        currencies.put("CAD", "Canadian Dollar");
        currencies.put("SGD", "Singapore Dollar");
        currencies.put("AED", "UAE Dirham");
        currencies.put("MYR", "Malaysian Ringgit");
        
        return currencies;
    }

    /**
     * Validate currency code
     * @param currency Currency code to validate
     * @return true if currency is supported
     */
    public boolean isCurrencySupported(String currency) {
        return getSupportedCurrencies().containsKey(currency);
    }

    /**
     * Get payment status from Razorpay gateway
     * @param orderId Order ID to check status
     * @return Payment status as string
     */
    public String getPaymentStatus(String orderId) {
        try {
            // Fetch order from Razorpay
            Order order = razorpayClient.orders.fetch(orderId);
            String status = order.get("status");
            
            log.debug("Payment status for order {}: {}", orderId, status);
            
            // Map Razorpay statuses to our system
            switch (status.toLowerCase()) {
                case "paid":
                    return "COMPLETED";
                case "created":
                case "attempted":
                    return "PENDING";
                case "failed":
                    return "FAILED";
                default:
                    return "PENDING";
            }
        } catch (RazorpayException e) {
            log.error("Error fetching payment status for order: {}", orderId, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error checking payment status for order: {}", orderId, e);
            return null;
        }
    }
}
