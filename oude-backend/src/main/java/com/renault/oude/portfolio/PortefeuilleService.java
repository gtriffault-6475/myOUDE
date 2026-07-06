package com.renault.oude.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PortefeuilleService {

    private static final Logger log = LoggerFactory.getLogger(PortefeuilleService.class);

    @Value("${oude.cache.portfolio-ttl-hours:1}")
    private long cacheTtlHours;

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
    public PortefeuilleResponse getPortefeuille(String portfolioId) {
        var sync = syncRepository.findById(portfolioId);
        boolean cacheValid = sync.isPresent()
            && ChronoUnit.HOURS.between(sync.get().getLastSyncAt(), LocalDateTime.now()) < cacheTtlHours;

        if (cacheValid) {
            log.info("portefeuille_source=cache portfolio_id={}", portfolioId);
            return fromCache(portfolioId, false);
        }

        try {
            return refreshFromApi(portfolioId);
        } catch (Exception ex) {
            log.warn("renault_api_fallback portfolio_id={} reason={}", portfolioId, ex.getMessage());
            if (syncRepository.existsById(portfolioId)) {
                return fromCache(portfolioId, true);
            }
            throw new PortefeuilleIndisponibleException("portfolio.unavailable");
        }
    }

    private PortefeuilleResponse refreshFromApi(String portfolioId) {
        long start = System.currentTimeMillis();
        List<Lead> leads = renaultApiClient.fetchLeads(portfolioId);
        List<Affaire> affaires = renaultApiClient.fetchAffaires(portfolioId);

        upsertLeads(portfolioId, leads);
        upsertAffaires(portfolioId, affaires);
        upsertSync(portfolioId, "api");

        log.info("portefeuille_source=api portfolio_id={} latency_ms={}", portfolioId, System.currentTimeMillis() - start);
        return new PortefeuilleResponse(leads, affaires, false, LocalDateTime.now());
    }

    private PortefeuilleResponse fromCache(String portfolioId, boolean staleData) {
        var sync = syncRepository.findById(portfolioId).orElseThrow();
        return new PortefeuilleResponse(
            leadRepository.findByPortfolioId(portfolioId),
            affaireRepository.findByPortfolioId(portfolioId),
            staleData,
            sync.getLastSyncAt()
        );
    }

    private void upsertLeads(String portfolioId, List<Lead> leads) {
        leadRepository.deleteByPortfolioId(portfolioId);
        var now = LocalDateTime.now();
        leads.forEach(l -> {
            l.setPortfolioId(portfolioId);
            l.setLastSyncAt(now);
        });
        leadRepository.saveAll(leads);
    }

    private void upsertAffaires(String portfolioId, List<Affaire> affaires) {
        affaireRepository.deleteByPortfolioId(portfolioId);
        var now = LocalDateTime.now();
        affaires.forEach(a -> {
            a.setPortfolioId(portfolioId);
            a.setLastSyncAt(now);
        });
        affaireRepository.saveAll(affaires);
    }

    private void upsertSync(String portfolioId, String source) {
        var sync = syncRepository.findById(portfolioId)
            .orElse(new PortefeuilleSync(portfolioId));
        sync.setLastSyncAt(LocalDateTime.now());
        sync.setSource(source);
        syncRepository.save(sync);
    }
}
