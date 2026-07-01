package com.renault.oude.security;

public record VendeurIdentity(
    String vendeurId,        // TODO: AQ-1 — CLAIM_VENDEUR_ID (ex: "sub" ou claim propriétaire Renault)
    String prenom,
    String email,
    String portefeuilleId    // TODO: AQ-1 — CLAIM_PORTEFEUILLE (ex: "portfolio_id")
) {
    public static VendeurIdentity mock(String vendeurId, String prenom, String email, String portefeuilleId) {
        return new VendeurIdentity(vendeurId, prenom, email, portefeuilleId);
    }
}
