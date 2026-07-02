package com.renault.oude.portfolio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portefeuille_sync")
public class PortefeuilleSync {

    @Id
    @Column(name = "portefeuille_id")
    private String portefeuilleId;

    @Column(name = "derniere_synchronisation", nullable = false)
    private LocalDateTime derniereSynchronisation;

    private String source;

    public PortefeuilleSync() {}

    public PortefeuilleSync(String portefeuilleId) {
        this.portefeuilleId = portefeuilleId;
        this.derniereSynchronisation = LocalDateTime.now();
        this.source = "api";
    }

    public String getPortefeuilleId() { return portefeuilleId; }
    public void setPortefeuilleId(String portefeuilleId) { this.portefeuilleId = portefeuilleId; }
    public LocalDateTime getDerniereSynchronisation() { return derniereSynchronisation; }
    public void setDerniereSynchronisation(LocalDateTime derniereSynchronisation) { this.derniereSynchronisation = derniereSynchronisation; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
