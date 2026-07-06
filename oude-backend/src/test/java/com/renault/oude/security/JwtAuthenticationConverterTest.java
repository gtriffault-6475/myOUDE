package com.renault.oude.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    @Test
    void convert_extraitVendeurIdentityDepuisClaims() {
        Jwt jwt = buildJwt(Map.of(
            "sub", "VEND-001",
            "given_name", "Natasha",
            "email", "natasha@renault.com",
            "portfolio_id", "PF-001"
        ));

        var token = converter.convert(jwt);
        var identity = (VendeurIdentity) token.getDetails();

        assertThat(identity.vendeurId()).isEqualTo("VEND-001");
        assertThat(identity.prenom()).isEqualTo("Natasha");
        assertThat(identity.email()).isEqualTo("natasha@renault.com");
        assertThat(identity.portfolioId()).isEqualTo("PF-001");
    }

    @Test
    void convert_vendeurIdManquant_leveException() {
        Jwt jwt = buildJwt(Map.of(
            "email", "natasha@renault.com"
            // pas de "sub"
        ));

        assertThatThrownBy(() -> converter.convert(jwt))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("vendeurId");
    }

    @Test
    void convert_vendeurIdVide_leveException() {
        Jwt jwt = buildJwt(Map.of("sub", ""));

        assertThatThrownBy(() -> converter.convert(jwt))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convert_claimsOptionnelsAbsents_nesontPasRequis() {
        Jwt jwt = buildJwt(Map.of("sub", "VEND-002"));

        var token = converter.convert(jwt);
        var identity = (VendeurIdentity) token.getDetails();

        assertThat(identity.vendeurId()).isEqualTo("VEND-002");
        assertThat(identity.prenom()).isNull();
        assertThat(identity.portfolioId()).isNull();
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .claims(c -> c.putAll(claims))
            .build();
    }
}
