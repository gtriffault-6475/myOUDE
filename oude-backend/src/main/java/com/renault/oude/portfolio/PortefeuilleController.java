package com.renault.oude.portfolio;

import com.renault.oude.security.JwtAuthenticationConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortefeuilleController {

    private final PortefeuilleService portefeuilleService;

    public PortefeuilleController(PortefeuilleService portefeuilleService) {
        this.portefeuilleService = portefeuilleService;
    }

    @GetMapping
    public ResponseEntity<PortefeuilleResponse> getPortefeuille() {
        var vendeur = JwtAuthenticationConverter.currentVendeur();
        return ResponseEntity.ok(portefeuilleService.getPortefeuille(vendeur.portfolioId()));
    }
}
