package com.renault.oude.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PortefeuilleService {

    private static final Logger log = LoggerFactory.getLogger(PortefeuilleService.class);
    private static final long CACHE_TTL_HOURS = 1;

    private final LeadRepository leadRepository;
    private final AffaireRepository affaireRepository;
    private final PortefeuilleSyncRepository syncRepository;
    private final RenaultApiClient renaultApiClient;

    public PortefeuilleService(LeadRepository leadRepository,
                                AffaireRepository affaireRepository,
                                PortefeuilleSyncRepository syncRepository,
                                RenaultApiClient renaultApiClient) {
        this.leadRepository = leadRepository;
        this.affaireRepository = affaireRepository;
        this.syncRepository = syncRepository;
        this.renaultApiClient = renaultApiClient;
    }

    @Transactional
    public PortefeuilleResponse getPortefeuille(String portefeuilleId) {
        var sync = syncRepository.findById(portefeuilleId);
        boolean cacheValide = sync.isPresent()
            && ChronoUnit.HOURS.between(sync.get().getDerniereSynchronisation(), LocalDateTime.now()) < CACHE_TTL_HOURS;

        if (cacheValide) {
            log.info("portefeuille_source=cache portefeuille_id={}", portefeuilleId);
            return fromCache(portefeuilleId, false);
        }

        try {
            return refreshFromApi(portefeuilleId);
        } catch (Exception ex) {
            log.warn("renault_api_fallback portefeuille_id={} reason={}", portefeuilleId, ex.getMessage());
            if (syncRepository.existsById(portefeuilleId)) {
                return fromCache(portefeuilleId, true);
            }
            throw new PortefeuilleIndisponibleException("Données Renault temporairement indisponibles");
        }
    }

    private PortefeuilleResponse refreshFromApi(String portefeuilleId) {
        long start = System.currentTimeMillis();
        List<Lead> leads = renaultApiClient.fetchLeads(portefeuilleId);
        List<Affaire> affaires = renaultApiClient.fetchAffaires(portefeuilleId);

        upsertLeads(portefeuilleId, leads);
        upsertAffaires(portefeuilleId, affaires);
        upsertSync(portefeuilleId, "api");

        log.info("portefeuille_source=api portefeuille_id={} latency_ms={}", portefeuilleId, System.currentTimeMillis() - start);
        return new PortefeuilleResponse(leads, affaires, false, LocalDateTime.now());
    }

    private PortefeuilleResponse fromCache(String portefeuilleId, boolean donneesDatees) {
        var sync = syncRepository.findById(portefeuilleId).orElseThrow();
        return new PortefeuilleResponse(
            leadRepository.findByPortefeuilleId(portefeuilleId),
            affaireRepository.findByPortefeuilleId(portefeuilleId),
            donneesDatees,
            sync.getDerniereSynchronisation()
        );
    }

    private void upsertLeads(String portefeuilleId, List<Lead> leads) {
        leadRepository.deleteByPortefeuilleId(portefeuilleId);
        var now = LocalDateTime.now();
        leads.forEach(l -> {
            l.setPortefeuilleId(portefeuilleId);
            l.setDerniereSynchronisation(now);
        });
        leadRepository.saveAll(leads);
    }

    private void upsertAffaires(String portefeuilleId, List<Affaire> affaires) {
        affaireRepository.deleteByPortefeuilleId(portefeuilleId);
        var now = LocalDateTime.now();
        affaires.forEach(a -> {
            a.setPortefeuilleId(portefeuilleId);
            a.setDerniereSynchronisation(now);
        });
        affaireRepository.saveAll(affaires);
    }

    private void upsertSync(String portefeuilleId, String source) {
        var sync = syncRepository.findById(portefeuilleId)
            .orElse(new PortefeuilleSync(portefeuilleId));
        sync.setDerniereSynchronisation(LocalDateTime.now());
        sync.setSource(source);
        syncRepository.save(sync);
    }
}
