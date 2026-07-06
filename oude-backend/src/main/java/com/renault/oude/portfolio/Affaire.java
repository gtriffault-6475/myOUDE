package com.renault.oude.portfolio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "affaires")
public class Affaire {

    @Id
    private String id;

    @Column(name = "portfolio_id", nullable = false)
    private String portfolioId;

    @Column(name = "client_last_name")
    private String clientLastName;

    @Column(name = "client_first_name")
    private String clientFirstName;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "financing_type")
    private String financingType;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

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
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getFinancingType() { return financingType; }
    public void setFinancingType(String financingType) { this.financingType = financingType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
