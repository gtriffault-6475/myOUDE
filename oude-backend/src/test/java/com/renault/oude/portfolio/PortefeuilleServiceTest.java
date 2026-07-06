package com.renault.oude.portfolio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortefeuilleServiceTest {

    @Mock LeadRepository leadRepository;
    @Mock AffaireRepository affaireRepository;
    @Mock PortefeuilleSyncRepository syncRepository;
    @Mock RenaultApiClient renaultApiClient;

    @InjectMocks PortefeuilleService service;

    private static final String PF_ID = "PF-001";

    @Test
    void getPortefeuille_cacheValide_retourneDepuisCache_sansAppelApi() {
        var sync = syncAvecAge(0); // sync récente (< 1h)
        when(syncRepository.findById(PF_ID)).thenReturn(Optional.of(sync));
        when(leadRepository.findByPortfolioId(PF_ID)).thenReturn(List.of(unLead()));
        when(affaireRepository.findByPortfolioId(PF_ID)).thenReturn(List.of(uneAffaire()));

        var result = service.getPortefeuille(PF_ID);

        assertThat(result.staleData()).isFalse();
        assertThat(result.leads()).hasSize(1);
        assertThat(result.affaires()).hasSize(1);
        verifyNoInteractions(renaultApiClient);
    }

    @Test
    void getPortefeuille_cacheExpire_appelleLesApis() {
        var sync = syncAvecAge(2); // sync ancienne (> 1h)
        when(syncRepository.findById(PF_ID)).thenReturn(Optional.of(sync));
        when(renaultApiClient.fetchLeads(PF_ID)).thenReturn(List.of(unLead()));
        when(renaultApiClient.fetchAffaires(PF_ID)).thenReturn(List.of(uneAffaire()));
        when(syncRepository.findById(PF_ID)).thenReturn(Optional.of(sync));

        var result = service.getPortefeuille(PF_ID);

        assertThat(result.staleData()).isFalse();
        verify(renaultApiClient).fetchLeads(PF_ID);
        verify(renaultApiClient).fetchAffaires(PF_ID);
    }

    @Test
    void getPortefeuille_aucunCache_appelleLesApis() {
        when(syncRepository.findById(PF_ID)).thenReturn(Optional.empty());
        when(renaultApiClient.fetchLeads(PF_ID)).thenReturn(List.of(unLead()));
        when(renaultApiClient.fetchAffaires(PF_ID)).thenReturn(List.of(uneAffaire()));

        var result = service.getPortefeuille(PF_ID);

        assertThat(result.staleData()).isFalse();
        verify(renaultApiClient).fetchLeads(PF_ID);
    }

    @Test
    void getPortefeuille_apiIndisponible_avecCache_retourneDonneesDatees() {
        when(syncRepository.findById(PF_ID)).thenReturn(Optional.empty());
        when(renaultApiClient.fetchLeads(PF_ID)).thenThrow(new RuntimeException("API timeout"));
        when(syncRepository.existsById(PF_ID)).thenReturn(true);

        var sync = syncAvecAge(2);
        when(syncRepository.findById(PF_ID)).thenReturn(Optional.empty())
            .thenReturn(Optional.of(sync));
        when(leadRepository.findByPortfolioId(PF_ID)).thenReturn(List.of(unLead()));
        when(affaireRepository.findByPortfolioId(PF_ID)).thenReturn(List.of());

        var result = service.getPortefeuille(PF_ID);

        assertThat(result.staleData()).isTrue();
        assertThat(result.leads()).hasSize(1);
    }

    @Test
    void getPortefeuille_apiIndisponible_sansCache_leveException() {
        when(syncRepository.findById(PF_ID)).thenReturn(Optional.empty());
        when(renaultApiClient.fetchLeads(PF_ID)).thenThrow(new RuntimeException("API timeout"));
        when(syncRepository.existsById(PF_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.getPortefeuille(PF_ID))
            .isInstanceOf(PortefeuilleIndisponibleException.class);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PortefeuilleSync syncAvecAge(int heures) {
        var sync = new PortefeuilleSync(PF_ID);
        sync.setLastSyncAt(LocalDateTime.now().minusHours(heures));
        return sync;
    }

    private Lead unLead() {
        var l = new Lead();
        l.setId("LEAD-001");
        l.setPortfolioId(PF_ID);
        l.setClientLastName("Dupont");
        l.setStatus("NEW");
        l.setLastSyncAt(LocalDateTime.now());
        l.setCountryCode("fr");
        return l;
    }

    private Affaire uneAffaire() {
        var a = new Affaire();
        a.setId("AFF-001");
        a.setPortfolioId(PF_ID);
        a.setClientLastName("Durand");
        a.setStatus("RENEWAL");
        a.setLastSyncAt(LocalDateTime.now());
        a.setCountryCode("fr");
        return a;
    }
}
