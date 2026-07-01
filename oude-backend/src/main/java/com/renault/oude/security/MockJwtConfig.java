package com.renault.oude.security;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Génère une paire RSA éphémère pour signer/valider les JWT mock en local.
 * Actif uniquement avec le profil "local" — jamais en GKE.
 */
@Configuration
@Profile("local")
public class MockJwtConfig {

    @Value("${oude.mock.vendeur-id:VEND-001}")
    private String mockVendeurId;

    @Value("${oude.mock.prenom:Natasha}")
    private String mockPrenom;

    @Value("${oude.mock.email:natasha.martin@renault.com}")
    private String mockEmail;

    @Value("${oude.mock.portefeuille-id:PF-BOULOGNE-001}")
    private String mockPortefeuilleId;

    @Bean
    public RSAKey mockRsaKey() throws Exception {
        return new RSAKeyGenerator(2048)
            .keyID("mock-key-local")
            .generate();
    }

    public VendeurIdentity mockIdentity() {
        return VendeurIdentity.mock(mockVendeurId, mockPrenom, mockEmail, mockPortefeuilleId);
    }
}
