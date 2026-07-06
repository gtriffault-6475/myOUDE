package com.renault.oude.security;

public record VendeurIdentity(
    String vendeurId,      // TODO: AQ-1 — CLAIM_VENDEUR_ID (ex: "sub" ou claim propriétaire Renault)
    String prenom,
    String email,
    String portfolioId     // TODO: AQ-1 — CLAIM_PORTFOLIO (ex: "portfolio_id")
) {
    public static VendeurIdentity mock(String vendeurId, String prenom, String email, String portfolioId) {
        return new VendeurIdentity(vendeurId, prenom, email, portfolioId);
    }
}
