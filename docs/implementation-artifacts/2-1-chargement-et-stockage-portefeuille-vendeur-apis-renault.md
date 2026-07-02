---
baseline_commit: e5cb1529804d7c64aaa1e9d45d74c119ba59c01f
---

# Story 2.1 — Chargement et stockage du portefeuille vendeur depuis les APIs Renault

**Epic :** E2 — Homepage : vue portefeuille du vendeur
**JIRA :** SCRUM-14
**Statut :** ready-for-dev
**Date :** 2026-07-01
**Dépend de :** Story 1.2 (SSO JWT — VendeurIdentity dans SecurityContext, /api/auth/me)

---

## User Story

En tant que vendeur Renault,
Je veux que mon portefeuille (leads et affaires) soit chargé depuis les APIs Renault et stocké localement,
Afin que la Homepage s'affiche rapidement même si les APIs Renault sont temporairement indisponibles.

---

## Critères d'acceptance

**AC-1 — Chargement initial au login**

**Étant donné** que le vendeur vient de se connecter via SSO
**Quand** la première requête authentifiée arrive sur le BFF
**Alors** le BFF appelle les APIs Renault (via Apigee X) pour récupérer leads et affaires du vendeur identifié par `portefeuilleId`
**Et** les données reçues sont stockées en base PostgreSQL avec un timestamp `derniere_synchronisation`
**Et** le BFF retourne les données au front

**AC-2 — Cache avec TTL ~1h**

**Étant donné** que des données de portefeuille existent en base pour ce vendeur
**Quand** le BFF reçoit une requête pour le portefeuille
**Et** que `derniere_synchronisation` date de moins d'1 heure
**Alors** les données en cache sont retournées directement (sans appel Renault API)
**Et** le champ `donnees_datees` de la réponse est `false`

**Étant donné** que des données de portefeuille existent en base pour ce vendeur
**Quand** le BFF reçoit une requête pour le portefeuille
**Et** que `derniere_synchronisation` date de plus d'1 heure
**Alors** le BFF appelle les APIs Renault pour rafraîchir les données
**Et** les données rafraîchies remplacent le cache (upsert PostgreSQL)

**AC-3 — Fallback cache en cas d'indisponibilité API**

**Étant donné** que l'API Renault est indisponible (timeout ou erreur 5xx)
**Quand** le BFF tente un appel Renault
**Alors** Resilience4j intercepte l'erreur après `maxAttempts=2` tentatives
**Et** si un cache existe en base → les données en cache sont retournées avec `donnees_datees: true`
**Et** si aucun cache n'existe → HTTP 503 est retourné avec message `{"error": "SERVICE_UNAVAILABLE", "message": "Données Renault temporairement indisponibles"}`

---

## ⚠️ Contraintes ouvertes — APIs Renault

| Question | Impact | Marqueur |
|----------|--------|---------|
| AQ-2 : URL exacte de l'endpoint leads Apigee X | URL d'appel BFF→Apigee | `TODO: AQ-2` |
| AQ-3 : URL exacte de l'endpoint affaires Apigee X | URL d'appel BFF→Apigee | `TODO: AQ-3` |
| AQ-4 : Format de la réponse API (champs, nommage) | Mapping vers entités JPA | `TODO: AQ-4` |
| AQ-5 : Mécanisme d'auth Apigee X (OAuth2 client_credentials ?) | Config RestClient | `TODO: AQ-5` |

**Stratégie :** les stubs retournent des données seedées cohérentes avec les maquettes (Natasha Martin, portefeuille PF-001, 5 leads, 3 affaires). Quand les APIs réelles sont disponibles, seules les URLs et le mapping de réponse changent.

---

## Stack technique

### Backend — nouvelles dépendances `pom.xml`

```xml
<!-- Resilience4j pour circuit-breaker + retry sur appels Renault API -->
<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-spring-boot3</artifactId>
  <version>2.2.0</version>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<!-- Spring WebFlux pour WebClient (appels Renault API) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

> Utiliser **WebClient** (Spring WebFlux) en mode bloquant `.block()` dans les services, ou `RestClient` (Spring 6.1+). Préférer `RestClient` si déjà présent, sinon `WebClient`. Ne pas ajouter les deux.

### Frontend — aucune nouvelle dépendance

Le front appelle uniquement `/api/portfolio` — toute la logique d'appel Renault est côté BFF.

---

## Architecture de la solution

### Schéma de flux

```
Angular Front
     │  GET /api/portfolio
     ▼
[BFF Spring Boot]
     │
     ├── PortefeuilleService.getPortefeuille(portefeuilleId)
     │        │
     │        ├── Vérifie cache PostgreSQL (age < 1h ?)
     │        │       └── OUI → retourne cache (donnees_datees=false)
     │        │
     │        └── NON → appelle RenaultApiClient (via Apigee X)
     │                  │
     │                  ├── Succès → upsert PostgreSQL + retourne données
     │                  │
     │                  └── Erreur (Resilience4j fallback)
     │                            ├── Cache existe → retourne cache (donnees_datees=true)
     │                            └── Pas de cache → 503
     ▼
PostgreSQL (Cloud SQL)
  ├── portefeuille_cache (portefeuille_id, données JSON, derniere_synchronisation)
  ├── leads (id, portefeuille_id, données, score, statut, derniere_synchronisation)
  └── affaires (id, portefeuille_id, données, statut, derniere_synchronisation)
```

### Décisions d'architecture à respecter

- **ARCH-2** : deux régimes de cache — portefeuille TTL ~1h (cette story), client TTL ~15min (Story 3.1)
- **ARCH-4** : tout appel BFF→Renault passe par Apigee X — JAMAIS d'appel direct aux APIs internes
- **ARCH-8** : Resilience4j sur tous les appels externes — `maxAttempts=2`, `waitDuration=500ms`
- **ARCH-9** : logs structurés (JSON via Logback) — logguer `portefeuilleId`, `source` (cache/api), `latency_ms`

---

## Modèle de données — Migrations Flyway

### V3 — Tables portefeuille cache

Créer : `oude-backend/src/main/resources/db/migration/V3__create_portefeuille_tables.sql`

> Story 1.1 a créé V1. Story 1.2 n'a pas ajouté de migration. La prochaine migration disponible est V3 (préférer V3 par sécurité, en vérifiant que V2 n'existe pas).

```sql
-- Leads du portefeuille vendeur
CREATE TABLE leads (
    id                     VARCHAR(50)  PRIMARY KEY,
    portefeuille_id        VARCHAR(50)  NOT NULL,
    nom_client             VARCHAR(255),
    prenom_client          VARCHAR(255),
    email_client           VARCHAR(255),
    telephone_client       VARCHAR(50),
    score_potentiel        INTEGER,
    statut                 VARCHAR(50),
    modele_interesse       VARCHAR(255),
    date_creation          TIMESTAMP,
    derniere_interaction   TIMESTAMP,
    derniere_synchronisation TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Affaires en cours du portefeuille vendeur
CREATE TABLE affaires (
    id                     VARCHAR(50)  PRIMARY KEY,
    portefeuille_id        VARCHAR(50)  NOT NULL,
    nom_client             VARCHAR(255),
    prenom_client          VARCHAR(255),
    modele             VARCHAR(255),
    type_financement       VARCHAR(50),
    statut                 VARCHAR(50),
    date_creation          TIMESTAMP,
    echeance               TIMESTAMP,
    derniere_synchronisation TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index pour les requêtes par portefeuille
CREATE INDEX idx_leads_portefeuille ON leads(portefeuille_id);
CREATE INDEX idx_affaires_portefeuille ON affaires(portefeuille_id);

-- Métadonnées de synchronisation par portefeuille
CREATE TABLE portefeuille_sync (
    portefeuille_id          VARCHAR(50)  PRIMARY KEY,
    derniere_synchronisation TIMESTAMP    NOT NULL DEFAULT NOW(),
    source                   VARCHAR(20)  NOT NULL DEFAULT 'api' -- 'api' ou 'cache'
);
```

---

## Fichiers backend à créer / modifier

### Nouveaux fichiers

| Fichier | Rôle |
|---------|------|
| `src/main/java/com/renault/oude/portfolio/Lead.java` | Entité JPA Lead |
| `src/main/java/com/renault/oude/portfolio/Affaire.java` | Entité JPA Affaire |
| `src/main/java/com/renault/oude/portfolio/PortefeuilleSync.java` | Entité JPA sync metadata |
| `src/main/java/com/renault/oude/portfolio/LeadRepository.java` | Spring Data JPA |
| `src/main/java/com/renault/oude/portfolio/AffaireRepository.java` | Spring Data JPA |
| `src/main/java/com/renault/oude/portfolio/PortefeuilleSyncRepository.java` | Spring Data JPA |
| `src/main/java/com/renault/oude/portfolio/PortefeuilleResponse.java` | Record DTO réponse API |
| `src/main/java/com/renault/oude/portfolio/PortefeuilleService.java` | Logique cache + appel API |
| `src/main/java/com/renault/oude/portfolio/PortefeuilleController.java` | GET /api/portfolio |
| `src/main/java/com/renault/oude/portfolio/RenaultApiClient.java` | Client stub Apigee X |
| `src/main/resources/db/migration/V3__create_portefeuille_tables.sql` | Migration Flyway |
| `src/test/java/com/renault/oude/portfolio/PortefeuilleServiceTest.java` | Tests unitaires service |
| `src/test/java/com/renault/oude/portfolio/PortefeuilleControllerIT.java` | Tests intégration endpoint |

### Fichiers à modifier

| Fichier | Modification |
|---------|-------------|
| `pom.xml` | Ajouter Resilience4j + AOP + WebClient |
| `src/main/resources/application.yml` | Section `renault.api` + Resilience4j config |
| `src/main/resources/application-local.yml.example` | Stub URL locales |
| `src/test/resources/application-test.yml` | Mock URL APIs Renault test |

---

## Templates de code

### `Lead.java` — Entité JPA

```java
package com.renault.oude.portfolio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    private String id;

    @Column(name = "portefeuille_id", nullable = false)
    private String portefeuilleId;

    @Column(name = "nom_client")
    private String nomClient;

    @Column(name = "prenom_client")
    private String prenomClient;

    @Column(name = "email_client")
    private String emailClient;

    @Column(name = "telephone_client")
    private String telephoneClient;

    @Column(name = "score_potentiel")
    private Integer scorePotentiel;

    private String statut;

    @Column(name = "modele_interesse")
    private String modeleInteresse;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_interaction")
    private LocalDateTime derniereInteraction;

    @Column(name = "derniere_synchronisation", nullable = false)
    private LocalDateTime derniereSynchronisation;

    // Getters/setters ou convertir en record si Hibernate 6.2+
    // Utiliser @Builder + @NoArgsConstructor/@AllArgsConstructor (Lombok si disponible)
    // Si pas de Lombok dans le projet : générer getters/setters manuellement
}
```

### `PortefeuilleResponse.java` — DTO record

```java
package com.renault.oude.portfolio;

import java.time.LocalDateTime;
import java.util.List;

public record PortefeuilleResponse(
    List<Lead> leads,
    List<Affaire> affaires,
    boolean donneesDatees,
    LocalDateTime derniereSynchronisation
) {}
```

### `PortefeuilleController.java`

```java
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
        return ResponseEntity.ok(portefeuilleService.getPortefeuille(vendeur.portefeuilleId()));
    }
}
```

### `PortefeuilleService.java`

```java
package com.renault.oude.portfolio;

import io.github.resilience4j.retry.annotation.Retry;
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

        return refreshFromApi(portefeuilleId);
    }

    @Retry(name = "renaultApi", fallbackMethod = "fallbackPortefeuille")
    private PortefeuilleResponse refreshFromApi(String portefeuilleId) {
        long start = System.currentTimeMillis();
        var leads = renaultApiClient.fetchLeads(portefeuilleId);
        var affaires = renaultApiClient.fetchAffaires(portefeuilleId);

        upsertLeads(portefeuilleId, leads);
        upsertAffaires(portefeuilleId, affaires);
        upsertSync(portefeuilleId, "api");

        log.info("portefeuille_source=api portefeuille_id={} latency_ms={}", portefeuilleId, System.currentTimeMillis() - start);
        return new PortefeuilleResponse(leads, affaires, false, LocalDateTime.now());
    }

    private PortefeuilleResponse fallbackPortefeuille(String portefeuilleId, Exception ex) {
        log.warn("renault_api_fallback portefeuille_id={} reason={}", portefeuilleId, ex.getMessage());
        if (syncRepository.existsById(portefeuilleId)) {
            return fromCache(portefeuilleId, true);
        }
        throw new PortefeuilleIndisponibleException("Données Renault temporairement indisponibles");
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
```

### `RenaultApiClient.java` — Stub (remplacé quand AQ-2/3/4/5 résolues)

```java
package com.renault.oude.portfolio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RenaultApiClient {

    @Value("${renault.api.leads-url:stub}")  // TODO: AQ-2 — URL Apigee X leads
    private String leadsUrl;

    @Value("${renault.api.affaires-url:stub}")  // TODO: AQ-3 — URL Apigee X affaires
    private String affairesUrl;

    public List<Lead> fetchLeads(String portefeuilleId) {
        // TODO: AQ-2, AQ-4, AQ-5 — remplacer par appel RestClient/WebClient vers Apigee X
        return stubLeads(portefeuilleId);
    }

    public List<Affaire> fetchAffaires(String portefeuilleId) {
        // TODO: AQ-3, AQ-4, AQ-5 — remplacer par appel RestClient/WebClient vers Apigee X
        return stubAffaires(portefeuilleId);
    }

    // ── Données seedées cohérentes avec maquettes (Natasha Martin, PF-001) ──

    private List<Lead> stubLeads(String portefeuilleId) {
        return List.of(
            lead("LEAD-001", portefeuilleId, "Dupont", "Jean", "jean.dupont@gmail.com", "0612345678", 89, "NOUVEAU", "Clio VI E-Tech", -2),
            lead("LEAD-002", portefeuilleId, "Durand", "Paul", "paul.durand@gmail.com", "0698765432", 75, "EN_COURS", "Nouvelle Mégane E-Tech", -5),
            lead("LEAD-003", portefeuilleId, "Martin", "Sophie", "sophie.martin@hotmail.fr", "0655544332", 60, "NOUVEAU", "Arkana", -1),
            lead("LEAD-004", portefeuilleId, "Bernard", "Luc", "luc.bernard@orange.fr", "0677889900", 45, "EN_ATTENTE", "Captur", -10),
            lead("LEAD-005", portefeuilleId, "Petit", "Claire", "claire.petit@free.fr", "0633221100", 30, "FROID", "Zoe", -15)
        );
    }

    private List<Affaire> stubAffaires(String portefeuilleId) {
        return List.of(
            affaire("AFF-001", portefeuilleId, "Durand", "Paul", "Clio VI E-Tech", "LLD", "RENOUVELLEMENT", 3),
            affaire("AFF-002", portefeuilleId, "Lambert", "Marie", "Mégane E-Tech", "LOA", "NEGOCIATION", 2),
            affaire("AFF-003", portefeuilleId, "Rousseau", "Pierre", "Austral E-Tech", "CREDIT", "OFFRE", 1)
        );
    }

    private Lead lead(String id, String pfId, String nom, String prenom, String email, String tel,
                      int score, String statut, String modele, int joursDepuis) {
        var l = new Lead();
        l.setId(id);
        l.setPortefeuilleId(pfId);
        l.setNomClient(nom);
        l.setPrenomClient(prenom);
        l.setEmailClient(email);
        l.setTelephoneClient(tel);
        l.setScorePotentiel(score);
        l.setStatut(statut);
        l.setModeleInteresse(modele);
        l.setDateCreation(LocalDateTime.now().plusDays(joursDepuis));
        l.setDerniereInteraction(LocalDateTime.now().plusDays(joursDepuis / 2));
        l.setDerniereSynchronisation(LocalDateTime.now());
        return l;
    }

    private Affaire affaire(String id, String pfId, String nom, String prenom, String modele,
                             String financement, String statut, int moisEcheance) {
        var a = new Affaire();
        a.setId(id);
        a.setPortefeuilleId(pfId);
        a.setNomClient(nom);
        a.setPrenomClient(prenom);
        a.setModele(modele);
        a.setTypeFinancement(financement);
        a.setStatut(statut);
        a.setDateCreation(LocalDateTime.now().minusMonths(6));
        a.setEcheance(LocalDateTime.now().plusMonths(moisEcheance));
        a.setDerniereSynchronisation(LocalDateTime.now());
        return a;
    }
}
```

### `PortefeuilleIndisponibleException.java`

```java
package com.renault.oude.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PortefeuilleIndisponibleException extends RuntimeException {
    public PortefeuilleIndisponibleException(String message) {
        super(message);
    }
}
```

### Configuration `application.yml` — sections à ajouter

```yaml
renault:
  api:
    leads-url: ${RENAULT_API_LEADS_URL:stub}      # TODO: AQ-2
    affaires-url: ${RENAULT_API_AFFAIRES_URL:stub} # TODO: AQ-3

resilience4j:
  retry:
    instances:
      renaultApi:
        max-attempts: 2
        wait-duration: 500ms
        retry-exceptions:
          - java.io.IOException
          - org.springframework.web.client.ResourceAccessException
```

---

## Fichiers frontend à créer / modifier

### Nouveaux fichiers

| Fichier | Rôle |
|---------|------|
| `src/app/core/models/portefeuille.model.ts` | Interfaces Lead, Affaire, PortefeuilleResponse |
| `src/app/core/services/portefeuille.service.ts` | Signal-based service, appelle GET /api/portfolio |

### `portefeuille.model.ts`

```typescript
export interface Lead {
  id: string;
  portefeuilleId: string;
  nomClient: string;
  prenomClient: string;
  emailClient?: string;
  telephoneClient?: string;
  scorePotentiel?: number;
  statut: string;
  modeleInteresse?: string;
  dateCreation?: string;
  derniereInteraction?: string;
  derniereSynchronisation: string;
}

export interface Affaire {
  id: string;
  portefeuilleId: string;
  nomClient: string;
  prenomClient: string;
  modele?: string;
  typeFinancement?: string;
  statut: string;
  dateCreation?: string;
  echeance?: string;
  derniereSynchronisation: string;
}

export interface PortefeuilleResponse {
  leads: Lead[];
  affaires: Affaire[];
  donneesDatees: boolean;
  derniereSynchronisation: string;
}
```

### `portefeuille.service.ts`

```typescript
import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PortefeuilleResponse } from '../models/portefeuille.model';

@Injectable({ providedIn: 'root' })
export class PortefeuilleService {
  private readonly _portefeuille = signal<PortefeuilleResponse | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly portefeuille = this._portefeuille.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly donneesDatees = computed(() => this._portefeuille()?.donneesDatees ?? false);
  readonly leads = computed(() => this._portefeuille()?.leads ?? []);
  readonly affaires = computed(() => this._portefeuille()?.affaires ?? []);

  constructor(private http: HttpClient) {}

  load(): void {
    this._loading.set(true);
    this._error.set(null);

    this.http.get<PortefeuilleResponse>('/api/portfolio').subscribe({
      next: (data) => {
        this._portefeuille.set(data);
        this._loading.set(false);
      },
      error: (err) => {
        this._loading.set(false);
        this._error.set(err.status === 503
          ? 'Données temporairement indisponibles'
          : 'Erreur lors du chargement du portefeuille');
      }
    });
  }
}
```

---

## Tests à écrire

### `PortefeuilleServiceTest.java` — Tests unitaires

```java
@ExtendWith(MockitoExtension.class)
class PortefeuilleServiceTest {

    @Mock LeadRepository leadRepository;
    @Mock AffaireRepository affaireRepository;
    @Mock PortefeuilleSyncRepository syncRepository;
    @Mock RenaultApiClient renaultApiClient;

    @InjectMocks PortefeuilleService service;

    @Test
    void getPortefeuille_cacheValide_retourneDepuisCache() { ... }

    @Test
    void getPortefeuille_cachExpire_appelleLesApis() { ... }

    @Test
    void getPortefeuille_aucunCache_appelleLesApis() { ... }

    @Test
    void fallback_avecCache_retourneDonneesDatees() { ... }

    @Test
    void fallback_sansCache_levePortefeuilleIndisponibleException() { ... }
}
```

### `PortefeuilleControllerIT.java` — Tests intégration

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortefeuilleControllerIT {

    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void getPortefeuille_sansToken_retourne401() { ... }

    @Test
    void getPortefeuille_avecTokenValide_retourne200AvecLeadsEtAffaires() { ... }

    @Test
    void getPortefeuille_retourneDonneesDatees_quandCacheFallback() { ... }
}
```

---

## Comportement UX associé

D'après `EXPERIENCE.md` :

- **État loading :** skeleton loaders pour chaque section (leads, affaires) — jamais de spinner plein écran
- **État `donneesDatees: true`** → banner discret au-dessus de chaque section :
  ```
  ℹ️ Données du [date/heure] — actualisation indisponible
  ```
  Fond `#FFF8E1` (jaune très clair), texte `#E65100`
- **État vide :** "Aucun lead actif — votre portefeuille est à jour ✓" (vert, icône check)

> Ces composants UI seront implémentés dans les Stories 2.5 et 2.6 (listes leads/affaires). Cette story livre uniquement le BFF + le service Angular — **pas de composant Homepage**.

---

## Tâches et sous-tâches

- [x] **T1 — Migration Flyway V2** (backend) — V1 existait seule, la migration est V2
  - [x] T1.1 Vérifier que V1 existe et que V2 n'existe pas (dossier `db/migration`)
  - [x] T1.2 Créer `V2__create_portefeuille_tables.sql` avec les 3 tables
  - [x] T1.3 Vérifier le démarrage de l'app (Flyway doit migrer sans erreur)

- [x] **T2 — Entités JPA et repositories** (backend)
  - [x] T2.1 Créer `Lead.java` avec tous les champs + getters/setters
  - [x] T2.2 Créer `Affaire.java` avec tous les champs + getters/setters
  - [x] T2.3 Créer `PortefeuilleSync.java`
  - [x] T2.4 Créer `LeadRepository`, `AffaireRepository`, `PortefeuilleSyncRepository` (Spring Data JPA)
  - [x] T2.5 Ajouter `findByPortefeuilleId()` et `deleteByPortefeuilleId()` dans les repositories

- [x] **T3 — RenaultApiClient stub** (backend)
  - [x] T3.1 Créer `RenaultApiClient.java` avec stubs 5 leads + 3 affaires (données Natasha/PF-001)
  - [x] T3.2 Marquer les TODOs AQ-2, AQ-3, AQ-4, AQ-5
  - [x] T3.3 Ajouter `@Retry(name="renaultApi")` sur les méthodes fetch (bean séparé → AOP fonctionne)

- [x] **T4 — PortefeuilleService avec logique cache** (backend)
  - [x] T4.1 Créer `PortefeuilleService.java` avec logique TTL 1h
  - [x] T4.2 Fallback géré via try/catch (appel vers RenaultApiClient via AOP proxy → @Retry fonctionne)
  - [x] T4.3 Créer `PortefeuilleIndisponibleException` avec `@ResponseStatus(503)`
  - [x] T4.4 Ajouter logs structurés (source, portefeuilleId, latency_ms)

- [x] **T5 — PortefeuilleController** (backend)
  - [x] T5.1 Créer `PortefeuilleController.java` → `GET /api/portfolio`
  - [x] T5.2 Utiliser `JwtAuthenticationConverter.currentVendeur()` pour extraire `portefeuilleId`

- [x] **T6 — Configuration** (backend)
  - [x] T6.1 Ajouter dépendance AOP dans `pom.xml` (Resilience4j était déjà présent)
  - [x] T6.2 Ajouter section `renault.api` + `resilience4j.retry` dans `application.yml`
  - [x] T6.3 Mettre à jour `application-local.yml.example`
  - [x] T6.4 Mettre à jour `application-test.yml` (max-attempts=1, wait-duration=0ms pour les tests)

- [x] **T7 — Tests unitaires et intégration** (backend)
  - [x] T7.1 Écrire `PortefeuilleServiceTest` (5 cas : cache valide, cache expiré, pas de cache, fallback avec cache, fallback sans cache)
  - [x] T7.2 Écrire `PortefeuilleControllerIT` (401 sans token, 200 avec leads+affaires, vérif nombre)
  - [x] T7.3 `HealthEndpointIT` et `SecurityConfigIT` inchangés — @MockitoBean JwtDecoder toujours présent

- [x] **T8 — Service Angular** (frontend)
  - [x] T8.1 Créer `portefeuille.model.ts` (interfaces Lead, Affaire, PortefeuilleResponse)
  - [x] T8.2 Créer `portefeuille.service.ts` avec signals Angular
  - [x] T8.3 Écrire `portefeuille.service.spec.ts` (4 cas : succès, 503, 500, reset)

---

## Dev Agent Record

### Notes d'implémentation

- **Vérifier les migrations existantes** avant de créer V3 : `ls myOUDE/oude-backend/src/main/resources/db/migration/` — si V2 existe déjà, utiliser V4.
- **Lombok** : le projet n'utilise pas encore Lombok (Story 1.1). Pour les entités JPA, écrire getters/setters manuellement ou ajouter Lombok en dépendance (`lombok` scope `provided` dans pom.xml). Préférer l'ajout de Lombok pour réduire le boilerplate.
- **@Retry sur méthode privée** : Resilience4j `@Retry` ne fonctionne pas sur les méthodes privées via AOP. Rendre `refreshFromApi` package-private ou extraire dans un bean séparé.
- **Transactionnalité** : `deleteByPortefeuilleId` + `saveAll` dans la même transaction — l'annotation `@Transactional` sur `getPortefeuille` couvre tout le flux.
- **H2 + MODE=PostgreSQL** : la migration V3 doit utiliser uniquement des types SQL compatibles H2 (`VARCHAR`, `TIMESTAMP`, `INTEGER`). Ne pas utiliser `JSONB`, `UUID`, ou extensions PostgreSQL-only.
- **Tests intégration** : utiliser `@MockitoBean RenaultApiClient` pour contrôler le comportement des stubs dans les tests d'intégration.
- **Fallback Resilience4j** : la signature de la méthode fallback doit être identique à la méthode principale + un paramètre `Exception` en dernier.

### Log de debug

- Migration nommée V2 (pas V3) car seule V1 existait dans `db/migration/`
- `@Retry` placé sur `RenaultApiClient` (bean Spring séparé) — évite le piège de l'auto-invocation AOP
- Fallback géré par `try/catch` dans `PortefeuilleService.getPortefeuille()` au lieu de `fallbackMethod` sur l'annotation — plus lisible et testable
- `resilience4j.retry.max-attempts=1` en profil test pour éviter les délais d'attente dans les tests
- `deleteByPortefeuilleId` + `saveAll` dans la même transaction `@Transactional` → cohérence garantie

### Notes de complétion

Tous les ACs sont couverts :
- AC-1 : appel API au premier login (pas de cache) → RenaultApiClient.fetchLeads/Affaires → upsert PostgreSQL
- AC-2 : cache TTL 1h via `ChronoUnit.HOURS.between(...)` + `PortefeuilleSync` avec timestamp
- AC-3 : fallback try/catch avec `donneesDatees=true` si cache dispo, 503 sinon

Stubs cohérents avec les maquettes : Dupont Jean (89% NOUVEAU), Durand Paul (75% EN_COURS — même personnage que dans Flow 2 EXPERIENCE.md), 3 affaires avec échéances réalistes.

---

## Liste des fichiers modifiés

**Backend — nouveaux fichiers :**
- `myOUDE/oude-backend/src/main/resources/db/migration/V2__create_portefeuille_tables.sql`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/Lead.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/Affaire.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/PortefeuilleSync.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/LeadRepository.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/AffaireRepository.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/PortefeuilleSyncRepository.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/PortefeuilleResponse.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/PortefeuilleService.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/PortefeuilleController.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/RenaultApiClient.java`
- `myOUDE/oude-backend/src/main/java/com/renault/oude/portfolio/PortefeuilleIndisponibleException.java`
- `myOUDE/oude-backend/src/test/java/com/renault/oude/portfolio/PortefeuilleServiceTest.java`
- `myOUDE/oude-backend/src/test/java/com/renault/oude/portfolio/PortefeuilleControllerIT.java`

**Backend — fichiers modifiés :**
- `myOUDE/oude-backend/pom.xml` (ajout spring-boot-starter-aop)
- `myOUDE/oude-backend/src/main/resources/application.yml` (sections renault.api + resilience4j)
- `myOUDE/oude-backend/src/main/resources/application-local.yml.example` (section renault.api)
- `myOUDE/oude-backend/src/test/resources/application-test.yml` (section renault.api + resilience4j test)

**Frontend — nouveaux fichiers :**
- `myOUDE/oude-frontend/src/app/core/models/portefeuille.model.ts`
- `myOUDE/oude-frontend/src/app/core/services/portefeuille.service.ts`
- `myOUDE/oude-frontend/src/app/core/services/portefeuille.service.spec.ts`

---

## Changelog

| Date | Auteur | Changement |
|------|--------|-----------|
| 2026-07-01 | bmad-create-story | Création initiale |
| 2026-07-01 | bmad-dev-story | Implémentation complète — migration V2, entités JPA, service cache TTL 1h, stub API, tests backend+frontend |
