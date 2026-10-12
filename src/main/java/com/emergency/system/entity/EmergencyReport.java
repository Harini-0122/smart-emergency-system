
package com.emergency.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_reports")
public class EmergencyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "emergency_type", nullable = false, length = 100)
    private String emergencyType;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "reporter_email", nullable = false, length = 255)
    private String reporterEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Selected map coordinates
    @Column
    private Double latitude;

    @Column
    private Double longitude;
    // Emergency response tracking
@Column(name = "response_started_at")
private LocalDateTime responseStartedAt;

@Column(name = "resolved_at")
private LocalDateTime resolvedAt;

    // JPA requires a no-argument constructor
    public EmergencyReport() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "PENDING";
        }
    }

    // ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Location / address
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Emergency type
    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    // Status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Reporter email
    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    // Created date and time
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Latitude
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    // Longitude
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    // Response start time
public LocalDateTime getResponseStartedAt() {
    return responseStartedAt;
}

public void setResponseStartedAt(LocalDateTime responseStartedAt) {
    this.responseStartedAt = responseStartedAt;
}

// Resolution time
public LocalDateTime getResolvedAt() {
    return resolvedAt;
}

public void setResolvedAt(LocalDateTime resolvedAt) {
    this.resolvedAt = resolvedAt;
}
}
