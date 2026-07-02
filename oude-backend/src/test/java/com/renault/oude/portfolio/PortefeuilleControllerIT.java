package com.renault.oude.portfolio;

import com.renault.oude.security.VendeurIdentity;
import com.renault.oude.security.JwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortefeuilleControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getPortefeuille_sansToken_retourne401() throws Exception {
        mockMvc.perform(get("/api/portfolio"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getPortefeuille_avecTokenValide_retourne200AvecLeadsEtAffaires() throws Exception {
        Jwt mockJwt = buildJwt("VEND-001", "PF-001");
        when(jwtDecoder.decode(anyString())).thenReturn(mockJwt);

        mockMvc.perform(get("/api/portfolio")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.leads").isArray())
            .andExpect(jsonPath("$.affaires").isArray())
            .andExpect(jsonPath("$.donneesDatees").value(false))
            .andExpect(jsonPath("$.leads[0].nomClient").value("Dupont"));
    }

    @Test
    void getPortefeuille_retourneLeadsTriesParScore() throws Exception {
        Jwt mockJwt = buildJwt("VEND-001", "PF-001");
        when(jwtDecoder.decode(anyString())).thenReturn(mockJwt);

        mockMvc.perform(get("/api/portfolio")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.leads").isArray())
            .andExpect(jsonPath("$.leads.length()").value(5))
            .andExpect(jsonPath("$.affaires.length()").value(3));
    }

    private Jwt buildJwt(String vendeurId, String portefeuilleId) {
        return Jwt.withTokenValue("valid-token")
            .header("alg", "RS256")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .claims(c -> c.putAll(Map.of(
                "sub", vendeurId,
                "given_name", "Natasha",
                "email", "natasha@renault.com",
                "portfolio_id", portefeuilleId
            )))
            .build();
    }
}
