package com.donorbox.backend.service;

import com.donorbox.backend.dto.BlogRequest;
import com.donorbox.backend.dto.BlogResponse;
import com.donorbox.backend.entity.Blog;
import com.donorbox.backend.repository.BlogRepository;
import com.donorbox.backend.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {

    private final BlogRepository blogRepository;
    private final ImageUploadService imageUploadService;

    @Transactional
    public Blog createBlog(BlogRequest request) {
        // Check if slug already exists
        if (blogRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Blog with slug '" + request.getSlug() + "' already exists");
        }

        Blog blog = Blog.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .slug(request.getSlug())
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .featuredImage(request.getFeaturedImage())
                .author(request.getAuthor())
                .authorEmail(request.getAuthorEmail())
                .status(request.getStatus())
                .readingTime(request.getReadingTime())
                .tags(request.getTags())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .isFeatured(request.getIsFeatured())
                .allowComments(request.getAllowComments())
                .build();

        // Set published date if status is PUBLISHED
        if (Blog.BlogStatus.PUBLISHED.equals(request.getStatus())) {
            blog.setPublishedAt(DateTimeUtil.getCurrentTimeForDatabase());
        }

        Blog savedBlog = blogRepository.save(blog);
        log.info("Blog created with ID: {} and slug: {}", savedBlog.getId(), savedBlog.getSlug());
        return savedBlog;
    }

    @Transactional
    public Blog updateBlog(Long id, BlogRequest request) {
        Blog existingBlog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found with id: " + id));

        // Check if slug is being changed and if it conflicts with another blog
        if (!existingBlog.getSlug().equals(request.getSlug())) {
            blogRepository.findBySlug(request.getSlug())
                    .ifPresent(blog -> {
                        if (!blog.getId().equals(id)) {
                            throw new IllegalArgumentException("Blog with slug '" + request.getSlug() + "' already exists");
                        }
                    });
        }

        // Update fields
        existingBlog.setTitle(request.getTitle());
        existingBlog.setSubtitle(request.getSubtitle());
        existingBlog.setSlug(request.getSlug());
        existingBlog.setContent(request.getContent());
        existingBlog.setExcerpt(request.getExcerpt());
        existingBlog.setFeaturedImage(request.getFeaturedImage());
        existingBlog.setAuthor(request.getAuthor());
        existingBlog.setAuthorEmail(request.getAuthorEmail());
        existingBlog.setReadingTime(request.getReadingTime());
        existingBlog.setTags(request.getTags());
        existingBlog.setMetaTitle(request.getMetaTitle());
        existingBlog.setMetaDescription(request.getMetaDescription());
        existingBlog.setIsFeatured(request.getIsFeatured());
        existingBlog.setAllowComments(request.getAllowComments());

        // Handle status change
        Blog.BlogStatus oldStatus = existingBlog.getStatus();
        existingBlog.setStatus(request.getStatus());

        // Set or clear published date based on status
        if (Blog.BlogStatus.PUBLISHED.equals(request.getStatus()) && !Blog.BlogStatus.PUBLISHED.equals(oldStatus)) {
            existingBlog.setPublishedAt(DateTimeUtil.getCurrentTimeForDatabase());
        } else if (!Blog.BlogStatus.PUBLISHED.equals(request.getStatus()) && Blog.BlogStatus.PUBLISHED.equals(oldStatus)) {
            existingBlog.setPublishedAt(null);
        }

        Blog updatedBlog = blogRepository.save(existingBlog);
        log.info("Blog updated with ID: {}", updatedBlog.getId());
        return updatedBlog;
    }

    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found with id: " + id));
        
        blogRepository.delete(blog);
        log.info("Blog deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found with id: " + id));
    }

    @Transactional
    public BlogResponse updateBlogWithImage(Long id, MultipartFile image) throws IOException {
        Blog blog = getBlogById(id);
        
        if (image != null && !image.isEmpty()) {
            // Delete old image if it exists
            if (blog.getFeaturedImage() != null && !blog.getFeaturedImage().trim().isEmpty()) {
                boolean deleted = imageUploadService.deleteImage(blog.getFeaturedImage());
                if (deleted) {
                    log.info("Successfully deleted old featured image: {}", blog.getFeaturedImage());
                } else {
                    log.warn("Failed to delete old featured image: {}", blog.getFeaturedImage());
                }
            }
            
            // Upload new image
            String newImagePath = imageUploadService.uploadImage(image, "blogs");
            blog.setFeaturedImage(newImagePath);
            log.info("Successfully uploaded new featured image: {}", newImagePath);
        }
        
        Blog updatedBlog = blogRepository.save(blog);
        log.info("Blog updated with new featured image for ID: {}", updatedBlog.getId());
        return BlogResponse.fromEntity(updatedBlog);
    }

    @Transactional
    public BlogResponse updateBlogWithImageAndContent(Long id, BlogRequest request, MultipartFile image) throws IOException {
        Blog existingBlog = getBlogById(id);
        
        // Handle image upload/replacement if provided
        if (image != null && !image.isEmpty()) {
            // Delete old image if it exists
            if (existingBlog.getFeaturedImage() != null && !existingBlog.getFeaturedImage().trim().isEmpty()) {
                boolean deleted = imageUploadService.deleteImage(existingBlog.getFeaturedImage());
                if (deleted) {
                    log.info("Successfully deleted old featured image: {}", existingBlog.getFeaturedImage());
                } else {
                    log.warn("Failed to delete old featured image: {}", existingBlog.getFeaturedImage());
                }
            }
            
            // Upload new image
            String newImagePath = imageUploadService.uploadImage(image, "blogs");
            existingBlog.setFeaturedImage(newImagePath);
            log.info("Successfully uploaded new featured image: {}", newImagePath);
        }
        
        // Update other fields from request
        if (request.getTitle() != null) existingBlog.setTitle(request.getTitle());
        if (request.getSubtitle() != null) existingBlog.setSubtitle(request.getSubtitle());
        if (request.getSlug() != null && !existingBlog.getSlug().equals(request.getSlug())) {
            // Check if slug conflicts with another blog
            blogRepository.findBySlug(request.getSlug())
                    .ifPresent(blog -> {
                        if (!blog.getId().equals(id)) {
                            throw new IllegalArgumentException("Blog with slug '" + request.getSlug() + "' already exists");
                        }
                    });
            existingBlog.setSlug(request.getSlug());
        }
        if (request.getContent() != null) existingBlog.setContent(request.getContent());
        if (request.getExcerpt() != null) existingBlog.setExcerpt(request.getExcerpt());
        if (request.getAuthor() != null) existingBlog.setAuthor(request.getAuthor());
        if (request.getAuthorEmail() != null) existingBlog.setAuthorEmail(request.getAuthorEmail());
        if (request.getReadingTime() != null) existingBlog.setReadingTime(request.getReadingTime());
        if (request.getTags() != null) existingBlog.setTags(request.getTags());
        if (request.getMetaTitle() != null) existingBlog.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) existingBlog.setMetaDescription(request.getMetaDescription());
        if (request.getIsFeatured() != null) existingBlog.setIsFeatured(request.getIsFeatured());
        if (request.getAllowComments() != null) existingBlog.setAllowComments(request.getAllowComments());
        
        // Handle status change
        if (request.getStatus() != null) {
            Blog.BlogStatus oldStatus = existingBlog.getStatus();
            existingBlog.setStatus(request.getStatus());
            
            // Set or clear published date based on status
            if (Blog.BlogStatus.PUBLISHED.equals(request.getStatus()) && !Blog.BlogStatus.PUBLISHED.equals(oldStatus)) {
                existingBlog.setPublishedAt(DateTimeUtil.getCurrentTimeForDatabase());
            } else if (!Blog.BlogStatus.PUBLISHED.equals(request.getStatus()) && Blog.BlogStatus.PUBLISHED.equals(oldStatus)) {
                existingBlog.setPublishedAt(null);
            }
        }
        
        Blog updatedBlog = blogRepository.save(existingBlog);
        log.info("Blog updated with content and image for ID: {}", updatedBlog.getId());
        return BlogResponse.fromEntity(updatedBlog);
    }

    @Transactional
    public Blog getBlogBySlug(String slug, boolean incrementViews) {
        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found with slug: " + slug));
        
        if (incrementViews) {
            blog.incrementViewCount();
            blogRepository.save(blog);
        }
        
        return blog;
    }

    @Transactional(readOnly = true)
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public List<Blog> getPublishedBlogs() {
        try {
            List<Blog> publishedBlogs = blogRepository.findByStatus(Blog.BlogStatus.PUBLISHED);
            log.info("Found {} published blogs", publishedBlogs.size());
            
            return publishedBlogs.stream()
                    .sorted((b1, b2) -> {
                        // Handle null publishedAt values gracefully
                        LocalDateTime date1 = b1.getPublishedAt() != null ? b1.getPublishedAt() : b1.getCreatedAt();
                        LocalDateTime date2 = b2.getPublishedAt() != null ? b2.getPublishedAt() : b2.getCreatedAt();
                        return date2.compareTo(date1); // Most recent first
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching published blogs", e);
            // Return empty list instead of throwing exception to prevent 401 errors
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public Page<Blog> getBlogsPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return blogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Blog> getFeaturedBlogs() {
        try {
            return blogRepository.findAll().stream()
                    .filter(blog -> Blog.BlogStatus.PUBLISHED.equals(blog.getStatus()) && 
                                   Boolean.TRUE.equals(blog.getIsFeatured()))
                    .sorted((b1, b2) -> {
                        // Handle null publishedAt values gracefully
                        LocalDateTime date1 = b1.getPublishedAt() != null ? b1.getPublishedAt() : b1.getCreatedAt();
                        LocalDateTime date2 = b2.getPublishedAt() != null ? b2.getPublishedAt() : b2.getCreatedAt();
                        return date2.compareTo(date1); // Most recent first
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching featured blogs", e);
            // Return empty list instead of throwing exception
            return new ArrayList<>();
        }
    }

    @Transactional
    public Blog publishBlog(Long id) {
        Blog blog = getBlogById(id);
        blog.publish();
        Blog publishedBlog = blogRepository.save(blog);
        log.info("Blog published with ID: {}", id);
        return publishedBlog;
    }

    @Transactional
    public Blog unpublishBlog(Long id) {
        Blog blog = getBlogById(id);
        blog.unpublish();
        Blog unpublishedBlog = blogRepository.save(blog);
        log.info("Blog unpublished with ID: {}", id);
        return unpublishedBlog;
    }

    @Transactional(readOnly = true)
    public List<Blog> getBlogsByStatus(Blog.BlogStatus status) {
        return blogRepository.findByStatus(status);
    }

    // Utility method to generate slug from title
    public String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // Calculate reading time based on content
    public int calculateReadingTime(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 1;
        }
        
        // Average reading speed is 200-250 words per minute, we'll use 200
        String[] words = content.split("\\s+");
        int wordCount = words.length;
        int readingTime = Math.max(1, (int) Math.ceil(wordCount / 200.0));
        
        return readingTime;
    }
}
