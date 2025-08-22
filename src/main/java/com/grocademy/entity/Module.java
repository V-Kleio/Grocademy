package com.grocademy.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "modules", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "module_order"}))
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "pdf_content_url", length = 255)
    private String pdfContentUrl;

    @Column(name = "video_content_url", length = 255)
    private String videoContentUrl;

    @Column(name = "module_order", nullable = false)
    private Integer moduleOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Module() {}

    private Module(Builder builder) {
        this.course = builder.course;
        this.title = builder.title;
        this.description = builder.description;
        this.pdfContentUrl = builder.pdfContentUrl;
        this.videoContentUrl = builder.videoContentUrl;
        this.moduleOrder = builder.moduleOrder;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPdfContentUrl() { return pdfContentUrl; }
    public void setPdfContentUrl(String pdfContentUrl) { this.pdfContentUrl = pdfContentUrl; }

    public String getVideoContentUrl() { return videoContentUrl; }
    public void setVideoContentUrl(String videoContentUrl) { this.videoContentUrl = videoContentUrl; }

    public Integer getModuleOrder() { return moduleOrder; }
    public void setModuleOrder(Integer moduleOrder) { this.moduleOrder = moduleOrder; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private Course course;
        private String title;
        private String description;
        private String pdfContentUrl;
        private String videoContentUrl;
        private Integer moduleOrder;

        public Builder course(Course course) {
            this.course = course;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder pdfContentUrl(String pdfContentUrl) {
            this.pdfContentUrl = pdfContentUrl;
            return this;
        }

        public Builder videoContentUrl(String videoContentUrl) {
            this.videoContentUrl = videoContentUrl;
            return this;
        }

        public Builder moduleOrder(Integer moduleOrder) {
            this.moduleOrder = moduleOrder;
            return this;
        }

        public Module build() {
            return new Module(this);
        }
    }
}