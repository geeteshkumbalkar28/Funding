package com.donorbox.backend.dto;

import com.donorbox.backend.entity.Blog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {

    private Long id;
    private String title;
    private String subtitle;
    private String slug;
    private String content;
    private String excerpt;
    private String featuredImage;
    private String author;
    private String authorEmail;
    private Blog.BlogStatus status;
    private LocalDateTime publishedAt;
    private Long viewCount;
    private Integer readingTime;
    private String tags;
    private String metaTitle;
    private String metaDescription;
    private Boolean isFeatured;
    private Boolean allowComments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory method to create from Blog entity
    public static BlogResponse fromEntity(Blog blog) {
        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .subtitle(blog.getSubtitle())
                .slug(blog.getSlug())
                .content(blog.getContent())
                .excerpt(blog.getExcerpt())
                .featuredImage(blog.getFeaturedImage())
                .author(blog.getAuthor())
                .authorEmail(blog.getAuthorEmail())
                .status(blog.getStatus())
                .publishedAt(blog.getPublishedAt())
                .viewCount(blog.getViewCount())
                .readingTime(blog.getReadingTime())
                .tags(blog.getTags())
                .metaTitle(blog.getMetaTitle())
                .metaDescription(blog.getMetaDescription())
                .isFeatured(blog.getIsFeatured())
                .allowComments(blog.getAllowComments())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }

    // Factory method for summary response (without full content)
    public static BlogResponse summaryFromEntity(Blog blog) {
        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .subtitle(blog.getSubtitle())
                .slug(blog.getSlug())
                .excerpt(blog.getExcerpt())
                .featuredImage(blog.getFeaturedImage())
                .author(blog.getAuthor())
                .status(blog.getStatus())
                .publishedAt(blog.getPublishedAt())
                .viewCount(blog.getViewCount())
                .readingTime(blog.getReadingTime())
                .tags(blog.getTags())
                .isFeatured(blog.getIsFeatured())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }
}
