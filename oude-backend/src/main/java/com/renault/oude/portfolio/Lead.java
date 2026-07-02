package com.renault.oude.portfolio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    private String id;

    @Column(name = "portefeuille_id", nullable = false)
    private String portefeuilleId;

    @Column(name = "nom_client")
    private String nomClient;

    @Column(name = "prenom_client")
    private String prenomClient;

    @Column(name = "email_client")
    private String emailClient;

    @Column(name = "telephone_client")
    private String telephoneClient;

    @Column(name = "score_potentiel")
    private Integer scorePotentiel;

    private String statut;

    @Column(name = "modele_interesse")
    private String modeleInteresse;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_interaction")
    private LocalDateTime derniereInteraction;

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
    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }
    public String getTelephoneClient() { return telephoneClient; }
    public void setTelephoneClient(String telephoneClient) { this.telephoneClient = telephoneClient; }
    public Integer getScorePotentiel() { return scorePotentiel; }
    public void setScorePotentiel(Integer scorePotentiel) { this.scorePotentiel = scorePotentiel; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getModeleInteresse() { return modeleInteresse; }
    public void setModeleInteresse(String modeleInteresse) { this.modeleInteresse = modeleInteresse; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDerniereInteraction() { return derniereInteraction; }
    public void setDerniereInteraction(LocalDateTime derniereInteraction) { this.derniereInteraction = derniereInteraction; }
    public LocalDateTime getDerniereSynchronisation() { return derniereSynchronisation; }
    public void setDerniereSynchronisation(LocalDateTime derniereSynchronisation) { this.derniereSynchronisation = derniereSynchronisation; }
}
