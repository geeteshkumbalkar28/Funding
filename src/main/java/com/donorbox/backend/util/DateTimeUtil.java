package com.donorbox.backend.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling date and time operations with timezone support
 */
public class DateTimeUtil {
    
    private static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter EMAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Gets current date and time in Asia/Kolkata timezone
     * 
     * @return LocalDateTime in Asia/Kolkata timezone
     */
    public static LocalDateTime getCurrentKolkataTime() {
        return LocalDateTime.now(KOLKATA_ZONE);
    }
    
    /**
     * Converts a LocalDateTime to Asia/Kolkata timezone and formats it for email display
     * Since LocalDateTime doesn't carry timezone info, we treat it as if it's already in Asia/Kolkata
     * for consistent display across the application
     * 
     * @param dateTime the LocalDateTime to convert
     * @return formatted date string in Asia/Kolkata timezone
     */
    public static String formatForEmail(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        // Treat the LocalDateTime as if it's already in Asia/Kolkata timezone
        // This ensures consistent display regardless of when the record was created
        return dateTime.format(EMAIL_DATE_FORMATTER);
    }
    
    /**
     * Gets current date and time in Asia/Kolkata timezone formatted for email display
     * 
     * @return formatted current date string in Asia/Kolkata timezone
     */
    public static String getCurrentTimeForEmail() {
        return LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(KOLKATA_ZONE)
            .format(EMAIL_DATE_FORMATTER);
    }
    
    /**
     * Converts a LocalDateTime to Asia/Kolkata timezone and formats it for display
     * Since LocalDateTime doesn't carry timezone info, we treat it as if it's already in Asia/Kolkata
     * for consistent display across the application
     * 
     * @param dateTime the LocalDateTime to convert
     * @return formatted date string in Asia/Kolkata timezone for display
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        // Treat the LocalDateTime as if it's already in Asia/Kolkata timezone
        // This ensures consistent display regardless of when the record was created
        return dateTime.format(DISPLAY_DATE_FORMATTER);
    }
    
    /**
     * Gets current date and time in Asia/Kolkata timezone formatted for file naming
     * 
     * @return formatted current date string in Asia/Kolkata timezone for file naming
     */
    public static String getCurrentTimeForFileNaming() {
        return LocalDateTime.now(KOLKATA_ZONE).format(FILE_TIMESTAMP_FORMATTER);
    }
    
    /**
     * Converts a LocalDateTime to Asia/Kolkata timezone
     * Since LocalDateTime doesn't carry timezone info, we treat it as if it's already in Asia/Kolkata
     * for consistent handling across the application
     * 
     * @param dateTime the LocalDateTime to convert
     * @return LocalDateTime in Asia/Kolkata timezone
     */
    public static LocalDateTime toKolkataTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        // Treat the LocalDateTime as if it's already in Asia/Kolkata timezone
        // This ensures consistent handling regardless of when the record was created
        return dateTime;
    }
    
    /**
     * Gets current time in Asia/Kolkata timezone for database storage
     * This should be used in @PrePersist and @PreUpdate methods
     * 
     * @return LocalDateTime in Asia/Kolkata timezone
     */
    public static LocalDateTime getCurrentTimeForDatabase() {
        return LocalDateTime.now(KOLKATA_ZONE);
    }
}
