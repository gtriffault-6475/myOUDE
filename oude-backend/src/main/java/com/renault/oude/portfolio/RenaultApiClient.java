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

    @Value("${oude.country-code:fr}")
    private String countryCode;

    @Retry(name = "renaultApi")
    public List<Lead> fetchLeads(String portfolioId) {
        // TODO: AQ-2, AQ-4, AQ-5 — remplacer par appel RestClient vers Apigee X
        return stubLeads(portfolioId);
    }

    @Retry(name = "renaultApi")
    public List<Affaire> fetchAffaires(String portfolioId) {
        // TODO: AQ-3, AQ-4, AQ-5 — remplacer par appel RestClient vers Apigee X
        return stubAffaires(portfolioId);
    }

    private List<Lead> stubLeads(String portfolioId) {
        return List.of(
            lead("LEAD-001", portfolioId, "Dupont",  "Jean",   "jean.dupont@gmail.com",    "0612345678", 89, "NEW",         "Clio VI E-Tech",         -2),
            lead("LEAD-002", portfolioId, "Durand",  "Paul",   "paul.durand@gmail.com",    "0698765432", 75, "IN_PROGRESS", "Nouvelle Mégane E-Tech", -5),
            lead("LEAD-003", portfolioId, "Martin",  "Sophie", "sophie.martin@hotmail.fr", "0655544332", 60, "NEW",         "Arkana",                 -1),
            lead("LEAD-004", portfolioId, "Bernard", "Luc",    "luc.bernard@orange.fr",    "0677889900", 45, "PENDING",     "Captur",                 -10),
            lead("LEAD-005", portfolioId, "Petit",   "Claire", "claire.petit@free.fr",     "0633221100", 30, "COLD",        "Zoe",                    -15)
        );
    }

    private List<Affaire> stubAffaires(String portfolioId) {
        return List.of(
            affaire("AFF-001", portfolioId, "Durand",   "Paul",   "Clio VI E-Tech",  "LLD",    "RENEWAL",     3),
            affaire("AFF-002", portfolioId, "Lambert",  "Marie",  "Mégane E-Tech",   "LOA",    "NEGOTIATION", 2),
            affaire("AFF-003", portfolioId, "Rousseau", "Pierre", "Austral E-Tech",  "CREDIT", "OFFER",       1)
        );
    }

    private Lead lead(String id, String pfId, String lastName, String firstName, String email, String phone,
                      int score, String status, String vehicleModel, int daysAgo) {
        var l = new Lead();
        l.setId(id);
        l.setPortfolioId(pfId);
        l.setClientLastName(lastName);
        l.setClientFirstName(firstName);
        l.setClientEmail(email);
        l.setClientPhone(phone);
        l.setPotentialScore(score);
        l.setStatus(status);
        l.setVehicleModel(vehicleModel);
        l.setCreatedAt(LocalDateTime.now().plusDays(daysAgo));
        l.setLastInteractionAt(LocalDateTime.now().plusDays(daysAgo / 2));
        l.setLastSyncAt(LocalDateTime.now());
        l.setCountryCode(countryCode);
        return l;
    }

    private Affaire affaire(String id, String pfId, String lastName, String firstName, String vehicleModel,
                             String financingType, String status, int monthsUntilDue) {
        var a = new Affaire();
        a.setId(id);
        a.setPortfolioId(pfId);
        a.setClientLastName(lastName);
        a.setClientFirstName(firstName);
        a.setVehicleModel(vehicleModel);
        a.setFinancingType(financingType);
        a.setStatus(status);
        a.setCreatedAt(LocalDateTime.now().minusMonths(6));
        a.setDueAt(LocalDateTime.now().plusMonths(monthsUntilDue));
        a.setLastSyncAt(LocalDateTime.now());
        a.setCountryCode(countryCode);
        return a;
    }
}
