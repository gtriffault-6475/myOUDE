package com.renault.oude.portfolio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "affaires")
public class Affaire {

    @Id
    private String id;

    @Column(name = "portefeuille_id", nullable = false)
    private String portefeuilleId;

    @Column(name = "nom_client")
    private String nomClient;

    @Column(name = "prenom_client")
    private String prenomClient;

    private String modele;

    @Column(name = "type_financement")
    private String typeFinancement;

    private String statut;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    private LocalDateTime echeance;

    @Column(name = "derniere_synchronisation", nullable = false)
    private LocalDateTime derniereSynchronisation;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPortefeuilleId() { return portefeuilleId; }
    public void setPortefeuilleId(String portefeuilleId) { this.portefeuilleId = portefeuilleId; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public String getPrenomClient() { return prenomClient; }
    public void setPrenomClient(String prenomClient) { this.prenomClient = prenomClient; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public String getTypeFinancement() { return typeFinancement; }
    public void setTypeFinancement(String typeFinancement) { this.typeFinancement = typeFinancement; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getEcheance() { return echeance; }
    public void setEcheance(LocalDateTime echeance) { this.echeance = echeance; }
    public LocalDateTime getDerniereSynchronisation() { return derniereSynchronisation; }
    public void setDerniereSynchronisation(LocalDateTime derniereSynchronisation) { this.derniereSynchronisation = derniereSynchronisation; }
}
