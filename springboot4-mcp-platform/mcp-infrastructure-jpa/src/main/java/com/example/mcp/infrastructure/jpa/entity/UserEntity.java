package com.example.mcp.infrastructure.jpa.entity;

import com.example.mcp.domain.model.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 新系统中的用户表实体。
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(length = 64, nullable = false)
    private String id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 128)
    private String displayName;

    @Column(nullable = false, length = 128)
    private String email;

    @Column(nullable = false, length = 64)
    private String role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    public static UserEntity of(UserProfile profile) {
        UserEntity entity = new UserEntity();
        entity.setId(profile.id());
        entity.setUsername(profile.username());
        entity.setDisplayName(profile.displayName());
        entity.setEmail(profile.email());
        entity.setRole(profile.role());
        entity.setActive(profile.active());
        entity.setCreatedAt(profile.createdAt());
        return entity;
    }

    public UserProfile toDomain() {
        return new UserProfile(id, username, displayName, email, role, active, createdAt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
