package com.renault.oude.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // TODO: AQ-1 — CLAIM_VENDEUR_ID : remplacer "sub" par le claim réel Renault
    static final String CLAIM_VENDEUR_ID = "sub";
    // TODO: AQ-1 — CLAIM_PORTEFEUILLE : remplacer par le claim réel Renault
    static final String CLAIM_PORTEFEUILLE = "portfolio_id";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String vendeurId = jwt.getClaimAsString(CLAIM_VENDEUR_ID);
        if (vendeurId == null || vendeurId.isBlank()) {
            throw new IllegalArgumentException("JWT manquant : claim vendeurId (" + CLAIM_VENDEUR_ID + ")");
        }

        var identity = new VendeurIdentity(
            vendeurId,
            jwt.getClaimAsString("given_name"),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString(CLAIM_PORTEFEUILLE)
        );

        var token = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_VENDEUR")), vendeurId);
        token.setDetails(identity);
        return token;
    }

    public static VendeurIdentity currentVendeur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jat && jat.getDetails() instanceof VendeurIdentity identity) {
            return identity;
        }
        throw new IllegalStateException("Aucun vendeur authentifié dans le contexte de sécurité");
    }
}
