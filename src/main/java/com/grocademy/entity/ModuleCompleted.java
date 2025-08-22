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
@Table(name = "module_completed", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"}))
public class ModuleCompleted {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(name = "completed_at", nullable = false, updatable = false)
    private Instant completedAt;

    protected ModuleCompleted() {}

    private ModuleCompleted(Builder builder) {
        this.user = builder.user;
        this.module = builder.module;
        this.completedAt = builder.completedAt;
    }

    @PrePersist
    protected void onCreate() {
        completedAt = Instant.now();
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public Instant getCompletedAt() { return completedAt; }

    public static class Builder {
        private User user;
        private Module module;
        private Instant completedAt;

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder module(Module module) {
            this.module = module;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public ModuleCompleted build() {
            return new ModuleCompleted(this);
        }
    }
}
