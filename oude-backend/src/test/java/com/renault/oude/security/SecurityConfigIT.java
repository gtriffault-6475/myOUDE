package com.renault.oude.security;

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
class SecurityConfigIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void actuatorHealth_accessible_sansAuthentification() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    void apiAuthMe_retourne_401_sansToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void apiAuthMe_retourne_200_avecTokenValide() throws Exception {
        Jwt mockJwt = Jwt.withTokenValue("valid-token")
            .header("alg", "RS256")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .claims(c -> c.putAll(Map.of(
                "sub", "VEND-001",
                "given_name", "Natasha",
                "email", "natasha@renault.com",
                "portfolio_id", "PF-001"
            )))
            .build();

        when(jwtDecoder.decode(anyString())).thenReturn(mockJwt);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vendeurId").value("VEND-001"))
            .andExpect(jsonPath("$.prenom").value("Natasha"))
            .andExpect(jsonPath("$.email").value("natasha@renault.com"))
            .andExpect(jsonPath("$.portfolioId").value("PF-001"));
    }
}
