package com.donorbox.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import com.donorbox.backend.util.DateTimeUtil;

@Service
@Slf4j
public class MediaUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.upload.image.max-size:25MB}")
    private String maxImageSizeStr;

    @Value("${app.upload.video.max-size:100MB}")
    private String maxVideoSizeStr;

    @Value("${app.upload.image.allowed-types:jpg,jpeg,png,gif,webp,bmp,tiff,svg}")
    private String allowedImageTypesStr;

    @Value("${app.upload.video.allowed-types:mp4,avi,mov,wmv,flv,webm,mkv,m4v,3gp,ogv}")
    private String allowedVideoTypesStr;

    // Default file size limits (fallback)
    private static final long DEFAULT_MAX_IMAGE_SIZE = 25 * 1024 * 1024; // 25MB for images
    private static final long DEFAULT_MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB for videos

    /**
     * Upload a media file (image or video) to local storage
     * @param file The multipart file to upload
     * @param category The category/folder for the media (e.g., "causes", "events")
     * @return The relative path to access the uploaded media
     * @throws IOException if file upload fails
     */
    public String uploadMedia(MultipartFile file, String category) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = createUploadDirectory(category);
        
        // Generate unique filename
        String filename = generateUniqueFilename(file.getOriginalFilename());
        
        // Copy file to upload directory
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Media uploaded successfully: {}", filePath.toString());
        
        // Return relative path for accessing the media
        return category + "/" + filename;
    }

    /**
     * Upload an image file specifically
     * @param file The image file to upload
     * @param category The category for organizing images
     * @return The relative path to access the uploaded image
     * @throws IOException if file upload fails
     */
    public String uploadImage(MultipartFile file, String category) throws IOException {
        validateImageFile(file);
        return uploadMedia(file, category);
    }

    /**
     * Upload a video file specifically
     * @param file The video file to upload
     * @param category The category for organizing videos
     * @return The relative path to access the uploaded video
     * @throws IOException if file upload fails
     */
    public String uploadVideo(MultipartFile file, String category) throws IOException {
        validateVideoFile(file);
        return uploadMedia(file, category);
    }

    /**
     * Delete a media file from local storage
     * @param mediaPath The relative path of the media to delete
     * @return true if file was deleted successfully
     */
    public boolean deleteMedia(String mediaPath) {
        if (mediaPath == null || mediaPath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path fullPath = Paths.get(uploadDir).resolve(mediaPath);
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                log.info("Media deleted successfully: {}", fullPath.toString());
            } else {
                log.warn("Media file not found for deletion: {}", fullPath.toString());
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("Error deleting media: {}", mediaPath, e);
            return false;
        }
    }

    /**
     * Get the full URL for accessing uploaded media
     * @param relativePath The relative path returned from uploadMedia()
     * @return Full URL to access the media
     */
    public String getMediaUrl(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        // Build URL dynamically based on configuration
        String port = serverPort.equals("8080") ? "" : ":" + serverPort;
        return baseUrl + port + "/api/media/" + relativePath;
    }

    /**
     * Check if a media file exists in storage
     * @param relativePath The relative path of the media
     * @return true if file exists
     */
    public boolean mediaExists(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return false;
        }
        
        Path fullPath = Paths.get(uploadDir).resolve(relativePath);
        return Files.exists(fullPath);
    }

    /**
     * Determine if a file is an image based on its extension
     * @param filename The filename to check
     * @return true if the file is an image
     */
    public boolean isImageFile(String filename) {
        if (filename == null) return false;
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedTypes = Arrays.asList(allowedImageTypesStr.split(","));
        return allowedTypes.contains(extension);
    }

    /**
     * Determine if a file is a video based on its extension
     * @param filename The filename to check
     * @return true if the file is a video
     */
    public boolean isVideoFile(String filename) {
        if (filename == null) return false;
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedTypes = Arrays.asList(allowedVideoTypesStr.split(","));
        return allowedTypes.contains(extension);
    }

    /**
     * Parse file size string (e.g., "25MB", "100MB") to bytes
     * @param sizeStr The size string to parse
     * @return Size in bytes
     */
    private long parseFileSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return DEFAULT_MAX_IMAGE_SIZE; // fallback to default
        }
        
        sizeStr = sizeStr.trim().toUpperCase();
        try {
            if (sizeStr.endsWith("MB")) {
                long mb = Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2));
                return mb * 1024 * 1024;
            } else if (sizeStr.endsWith("KB")) {
                long kb = Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2));
                return kb * 1024;
            } else if (sizeStr.endsWith("B")) {
                return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 1));
            } else {
                return Long.parseLong(sizeStr);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid file size format: {}, using default", sizeStr);
            return DEFAULT_MAX_IMAGE_SIZE;
        }
    }

    /**
     * Get the media type based on file extension
     * @param filename The filename to check
     * @return FileMediaType enum (IMAGE, VIDEO, or null if unknown)
     */
    public FileMediaType getMediaType(String filename) {
        if (isImageFile(filename)) {
            return FileMediaType.IMAGE;
        } else if (isVideoFile(filename)) {
            return FileMediaType.VIDEO;
        }
        return null;
    }

    /**
     * Get content type for serving media files
     * @param filename The filename to determine content type for
     * @return Content type string
     */
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        // Image content types
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "tiff":
                return "image/tiff";
            case "svg":
                return "image/svg+xml";
                
            // Video content types
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "webm":
                return "video/webm";
            case "mkv":
                return "video/x-matroska";
            case "m4v":
                return "video/x-m4v";
            case "3gp":
                return "video/3gpp";
            case "ogv":
                return "video/ogg";
                
            default:
                return "application/octet-stream";
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File name is null");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedImageTypes = Arrays.asList(allowedImageTypesStr.split(","));
        List<String> allowedVideoTypes = Arrays.asList(allowedVideoTypesStr.split(","));
        
        boolean isImage = allowedImageTypes.contains(extension);
        boolean isVideo = allowedVideoTypes.contains(extension);

        if (!isImage && !isVideo) {
            throw new IOException("File type not allowed. Allowed types: " + 
                String.join(", ", allowedImageTypes) + ", " + 
                String.join(", ", allowedVideoTypes));
        }

        // Check file size based on type
        if (isImage) {
            long maxImageSize = parseFileSize(maxImageSizeStr);
            if (file.getSize() > maxImageSize) {
            throw new IOException("Image size exceeds maximum allowed size of " + 
                    (maxImageSize / 1024 / 1024) + "MB");
            }
        }

        if (isVideo) {
            long maxVideoSize = parseFileSize(maxVideoSizeStr);
            if (file.getSize() > maxVideoSize) {
            throw new IOException("Video size exceeds maximum allowed size of " + 
                    (maxVideoSize / 1024 / 1024) + "MB");
            }
        }
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Image file is empty or null");
        }

        long maxImageSize = parseFileSize(maxImageSizeStr);
        if (file.getSize() > maxImageSize) {
            throw new IOException("Image size exceeds maximum allowed size of " + 
                (maxImageSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Image file name is null");
        }

        List<String> allowedImageTypes = Arrays.asList(allowedImageTypesStr.split(","));
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedImageTypes.contains(extension)) {
            throw new IOException("Image type not allowed. Allowed types: " + 
                String.join(", ", allowedImageTypes));
        }
    }

    private void validateVideoFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Video file is empty or null");
        }

        long maxVideoSize = parseFileSize(maxVideoSizeStr);
        if (file.getSize() > maxVideoSize) {
            throw new IOException("Video size exceeds maximum allowed size of " + 
                (maxVideoSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Video file name is null");
        }

        List<String> allowedVideoTypes = Arrays.asList(allowedVideoTypesStr.split(","));
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedVideoTypes.contains(extension)) {
            throw new IOException("Video type not allowed. Allowed types: " + 
                String.join(", ", allowedVideoTypes));
        }
    }

    private Path createUploadDirectory(String category) throws IOException {
        Path uploadPath = Paths.get(uploadDir).resolve(category);
        
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toString());
            }
            
            // Test write permissions
            if (!Files.isWritable(uploadPath)) {
                throw new IOException("Upload directory is not writable: " + uploadPath.toString());
            }
            
        } catch (Exception e) {
            log.error("Failed to create or access upload directory: {}", uploadPath.toString(), e);
            throw new IOException("Cannot create upload directory: " + e.getMessage(), e);
        }
        
        return uploadPath;
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = DateTimeUtil.getCurrentTimeForFileNaming();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return timestamp + "_" + uuid + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Upload multiple media files (images and/or videos)
     * @param files Array of multipart files to upload
     * @param category The category for organizing media
     * @return List of relative paths to access the uploaded media files
     * @throws IOException if any file upload fails
     */
    public List<String> uploadMultipleMedia(MultipartFile[] files, String category) throws IOException {
        if (files == null || files.length == 0) {
            throw new IOException("No files provided for upload");
        }

        List<String> uploadedPaths = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String relativePath = uploadMedia(file, category);
                uploadedPaths.add(relativePath);
            }
        }
        
        if (uploadedPaths.isEmpty()) {
            throw new IOException("No valid files were uploaded");
        }
        
        return uploadedPaths;
    }

    /**
     * Upload multiple image files specifically
     * @param files Array of image files to upload
     * @param category The category for organizing images
     * @return List of relative paths to access the uploaded images
     * @throws IOException if any file upload fails
     */
    public List<String> uploadMultipleImages(MultipartFile[] files, String category) throws IOException {
        if (files == null || files.length == 0) {
            throw new IOException("No image files provided for upload");
        }

        List<String> uploadedPaths = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                validateImageFile(file);
                String relativePath = uploadMedia(file, category);
                uploadedPaths.add(relativePath);
            }
        }
        
        if (uploadedPaths.isEmpty()) {
            throw new IOException("No valid image files were uploaded");
        }
        
        return uploadedPaths;
    }

    /**
     * Upload multiple video files specifically
     * @param files Array of video files to upload
     * @param category The category for organizing videos
     * @return List of relative paths to access the uploaded videos
     * @throws IOException if any file upload fails
     */
    public List<String> uploadMultipleVideos(MultipartFile[] files, String category) throws IOException {
        if (files == null || files.length == 0) {
            throw new IOException("No video files provided for upload");
        }

        List<String> uploadedPaths = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                validateVideoFile(file);
                String relativePath = uploadMedia(file, category);
                uploadedPaths.add(relativePath);
            }
        }
        
        if (uploadedPaths.isEmpty()) {
            throw new IOException("No valid video files were uploaded");
        }
        
        return uploadedPaths;
    }

    /**
     * Delete multiple media files from local storage
     * @param mediaPaths List of relative paths of media files to delete
     * @return Number of files successfully deleted
     */
    public int deleteMultipleMedia(List<String> mediaPaths) {
        if (mediaPaths == null || mediaPaths.isEmpty()) {
            return 0;
        }
        
        int deletedCount = 0;
        for (String mediaPath : mediaPaths) {
            if (deleteMedia(mediaPath)) {
                deletedCount++;
            }
        }
        
        return deletedCount;
    }

    public enum FileMediaType {
        IMAGE, VIDEO
    }
}
