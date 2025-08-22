package com.donorbox.backend.entity;

import com.donorbox.backend.util.DateTimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 300)
    private String excerpt;

    @Column(name = "featured_image")
    private String featuredImage;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(name = "author_email", length = 150)
    private String authorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlogStatus status = BlogStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "reading_time")
    private Integer readingTime; // in minutes

    @Column(length = 1000)
    private String tags;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 300)
    private String metaDescription;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "allow_comments")
    @Builder.Default
    private Boolean allowComments = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BlogStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED,
        SCHEDULED
    }

    // Helper methods
    public void incrementViewCount() {
        this.viewCount = (this.viewCount != null ? this.viewCount : 0) + 1;
    }

    public void publish() {
        this.status = BlogStatus.PUBLISHED;
        this.publishedAt = DateTimeUtil.getCurrentTimeForDatabase();
    }

    public void unpublish() {
        this.status = BlogStatus.DRAFT;
        this.publishedAt = null;
    }

    public boolean isPublished() {
        return BlogStatus.PUBLISHED.equals(this.status);
    }

    public boolean isDraft() {
        return BlogStatus.DRAFT.equals(this.status);
    }
}
