package com.renault.oude.portfolio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_sync")
public class PortefeuilleSync {

    @Id
    @Column(name = "portfolio_id")
    private String portfolioId;

    @Column(name = "last_sync_at", nullable = false)
    private LocalDateTime lastSyncAt;

    private String source;

    public PortefeuilleSync() {}

    public PortefeuilleSync(String portfolioId) {
        this.portfolioId = portfolioId;
        this.lastSyncAt = LocalDateTime.now();
        this.source = "api";
    }

    public String getPortfolioId() { return portfolioId; }
    public void setPortfolioId(String portfolioId) { this.portfolioId = portfolioId; }
    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
