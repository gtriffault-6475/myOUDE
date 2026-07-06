package com.renault.oude.portfolio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    private String id;

    @Column(name = "portfolio_id", nullable = false)
    private String portfolioId;

    @Column(name = "client_last_name")
    private String clientLastName;

    @Column(name = "client_first_name")
    private String clientFirstName;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_phone")
    private String clientPhone;

    @Column(name = "potential_score")
    private Integer potentialScore;

    private String status;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    @Column(name = "last_sync_at", nullable = false)
    private LocalDateTime lastSyncAt;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPortfolioId() { return portfolioId; }
    public void setPortfolioId(String portfolioId) { this.portfolioId = portfolioId; }
    public String getClientLastName() { return clientLastName; }
    public void setClientLastName(String clientLastName) { this.clientLastName = clientLastName; }
    public String getClientFirstName() { return clientFirstName; }
    public void setClientFirstName(String clientFirstName) { this.clientFirstName = clientFirstName; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public Integer getPotentialScore() { return potentialScore; }
    public void setPotentialScore(Integer potentialScore) { this.potentialScore = potentialScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastInteractionAt() { return lastInteractionAt; }
    public void setLastInteractionAt(LocalDateTime lastInteractionAt) { this.lastInteractionAt = lastInteractionAt; }
    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
