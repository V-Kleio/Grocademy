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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "purchased_course", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class PurchasedCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    private Instant purchasedAt;

    protected PurchasedCourse() {}

    private PurchasedCourse(Builder builder) {
        this.user = builder.user;
        this.course = builder.course;
    }

    @PrePersist
    protected void onCreate() {
        purchasedAt = Instant.now();
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Instant getPurchasedAt() { return purchasedAt; }

    public static class Builder {
        private User user;
        private Course course;

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder course(Course course) {
            this.course = course;
            return this;
        }

        public PurchasedCourse build() {
            return new PurchasedCourse(this);
        }
    }
}
