package com.renault.oude.portfolio;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RenaultApiClient {

    @Value("${renault.api.leads-url:stub}")     // TODO: AQ-2 — URL Apigee X leads
    private String leadsUrl;

    @Value("${renault.api.affaires-url:stub}")  // TODO: AQ-3 — URL Apigee X affaires
    private String affairesUrl;

    @Retry(name = "renaultApi")
    public List<Lead> fetchLeads(String portefeuilleId) {
        // TODO: AQ-2, AQ-4, AQ-5 — remplacer par appel RestClient vers Apigee X
        return stubLeads(portefeuilleId);
    }

    @Retry(name = "renaultApi")
    public List<Affaire> fetchAffaires(String portefeuilleId) {
        // TODO: AQ-3, AQ-4, AQ-5 — remplacer par appel RestClient vers Apigee X
        return stubAffaires(portefeuilleId);
    }

    private List<Lead> stubLeads(String portefeuilleId) {
        return List.of(
            lead("LEAD-001", portefeuilleId, "Dupont",  "Jean",   "jean.dupont@gmail.com",    "0612345678", 89, "NOUVEAU",    "Clio VI E-Tech",       -2),
            lead("LEAD-002", portefeuilleId, "Durand",  "Paul",   "paul.durand@gmail.com",    "0698765432", 75, "EN_COURS",   "Nouvelle Mégane E-Tech", -5),
            lead("LEAD-003", portefeuilleId, "Martin",  "Sophie", "sophie.martin@hotmail.fr", "0655544332", 60, "NOUVEAU",    "Arkana",               -1),
            lead("LEAD-004", portefeuilleId, "Bernard", "Luc",    "luc.bernard@orange.fr",    "0677889900", 45, "EN_ATTENTE", "Captur",               -10),
            lead("LEAD-005", portefeuilleId, "Petit",   "Claire", "claire.petit@free.fr",     "0633221100", 30, "FROID",      "Zoe",                  -15)
        );
    }

    private List<Affaire> stubAffaires(String portefeuilleId) {
        return List.of(
            affaire("AFF-001", portefeuilleId, "Durand",  "Paul",   "Clio VI E-Tech",    "LLD",    "RENOUVELLEMENT", 3),
            affaire("AFF-002", portefeuilleId, "Lambert", "Marie",  "Mégane E-Tech",     "LOA",    "NEGOCIATION",    2),
            affaire("AFF-003", portefeuilleId, "Rousseau","Pierre", "Austral E-Tech",    "CREDIT", "OFFRE",          1)
        );
    }

    private Lead lead(String id, String pfId, String nom, String prenom, String email, String tel,
                      int score, String statut, String modele, int joursDepuis) {
        var l = new Lead();
        l.setId(id);
        l.setPortefeuilleId(pfId);
        l.setNomClient(nom);
        l.setPrenomClient(prenom);
        l.setEmailClient(email);
        l.setTelephoneClient(tel);
        l.setScorePotentiel(score);
        l.setStatut(statut);
        l.setModeleInteresse(modele);
        l.setDateCreation(LocalDateTime.now().plusDays(joursDepuis));
        l.setDerniereInteraction(LocalDateTime.now().plusDays(joursDepuis / 2));
        l.setDerniereSynchronisation(LocalDateTime.now());
        return l;
    }

    private Affaire affaire(String id, String pfId, String nom, String prenom, String modele,
                             String financement, String statut, int moisEcheance) {
        var a = new Affaire();
        a.setId(id);
        a.setPortefeuilleId(pfId);
        a.setNomClient(nom);
        a.setPrenomClient(prenom);
        a.setModele(modele);
        a.setTypeFinancement(financement);
        a.setStatut(statut);
        a.setDateCreation(LocalDateTime.now().minusMonths(6));
        a.setEcheance(LocalDateTime.now().plusMonths(moisEcheance));
        a.setDerniereSynchronisation(LocalDateTime.now());
        return a;
    }
}
