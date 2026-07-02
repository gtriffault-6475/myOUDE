---
baseline_commit: ec8bbc5b3fcbc14b3ea72106748711ffc629d1b1
---

# Story 1.2 — Intégration SSO Renault : authentification et extraction d'identité

**Epic :** E1 — Accès sécurisé à l'application (SSO + fondations)
**JIRA :** SCRUM-12
**Statut :** ready-for-dev
**Date :** 2026-06-30
**Dépend de :** Story 1.1 (bootstrap — pom.xml, OudeApplication.java, structure de base)

---

## User Story

En tant que vendeur Renault,
Je veux me connecter à OUDE via le SSO Renault,
Afin d'accéder à mes données de portefeuille sans créer de compte OUDE distinct.

---

## Critères d'acceptance

**Étant donné** que le vendeur n'est pas authentifié
**Quand** il accède à OUDE sur n'importe quelle URL protégée
**Alors** il est redirigé vers la page SSO Renault (ou vers le mock SSO en local)

**Étant donné** que le vendeur s'est authentifié via SSO
**Quand** le token JWT est reçu par le back-end sur chaque requête (header `Authorization: Bearer <token>`)
**Alors** le back valide la signature JWT (clé publique IdP Renault ou JWKS URI)
**Et** extrait l'identifiant vendeur (`vendeurId`) et son portefeuille depuis les claims
**Et** injecte un objet `VendeurIdentity` dans le contexte Spring Security

**Étant donné** que le vendeur est authentifié
**Quand** le front Angular appelle `/api/**`
**Alors** le token JWT est automatiquement ajouté dans le header `Authorization: Bearer`
**Et** toutes les données retournées sont filtrées selon le `vendeurId` extrait du token

**Étant donné** que le token JWT est expiré ou invalide
**Quand** le back-end tente de le valider
**Alors** il retourne HTTP 401
**Et** le front Angular redirige l'utilisateur vers la page SSO

**Étant donné** qu'on est en environnement local (profil `local`)
**Quand** le back démarre
**Alors** un mock JWT est généré et accepté sans connexion à l'IdP Renault réel
**Et** le `vendeurId` du mock est configurable via `application-local.yml`

---

## ⚠️ Contrainte critique — AQ-1 ouverte

**AQ-1 est en attente de réponse Renault.** Cette story est implémentable à ~70% sans cette réponse. Les points de branchement sont marqués `// TODO: AQ-1`.

| Information manquante | Impact | Marqueur dans le code |
|----------------------|--------|----------------------|
| JWKS URI de l'IdP Renault | URL de validation des clés publiques | `TODO: AQ-1 — JWKS_URI` |
| Claim exact du `vendeurId` | Extraction de l'identité | `TODO: AQ-1 — CLAIM_VENDEUR_ID` |
| Claim exact du portefeuille | Filtrage des données | `TODO: AQ-1 — CLAIM_PORTEFEUILLE` |
| Flow exact (OIDC Authorization Code ?) | Config Spring Security | `TODO: AQ-1 — OAUTH_FLOW` |
| URL de redirection SSO Renault | Redirection non-authentifié | `TODO: AQ-1 — SSO_REDIRECT_URI` |

**Stratégie :** implémenter avec des propriétés externalisées (`application.yml`). Quand AQ-1 est résolu, seul `application.yml` change — pas le code Java.

---

## Stack technique — dépendances à ajouter

Ajouter dans `oude-backend/pom.xml` (en plus des dépendances existantes de Story 1.1) :

```xml
<!-- Spring Security + OAuth2 Resource Server (JWT) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Pour le mock JWT en local (génération de tokens de test) -->
<dependency>
  <groupId>com.nimbusds</groupId>
  <artifactId>nimbus-jose-jwt</artifactId>
  <version>9.40</version>
  <scope>test</scope>
</dependency>
```

Ajouter dans `oude-frontend/package.json` :
```json
"@auth0/angular-jwt": "^5.2.0"
```
*(pour décoder le JWT côté front et extraire le prénom du vendeur — jamais pour valider, toujours côté back)*

---

## Structure des fichiers à créer

```
oude-backend/src/main/java/com/renault/oude/
  security/
    SecurityConfig.java                    ← NOUVEAU — config Spring Security
    JwtAuthenticationConverter.java        ← NOUVEAU — extraction VendeurIdentity depuis claims
    VendeurIdentity.java                   ← NOUVEAU — record immuable (vendeurId, prenom, email, portefeuilleId)
    MockJwtConfig.java                     ← NOUVEAU — bean conditionnel @Profile("local")

oude-backend/src/main/resources/
  application.yml                          ← UPDATE — ajout section spring.security
  application-local.yml                   ← NOUVEAU — mock SSO config (gitignore ✓)
  application-gke.yml                     ← NOUVEAU — config réelle IdP Renault (placeholders AQ-1)

oude-backend/src/test/java/com/renault/oude/
  security/
    JwtAuthenticationConverterTest.java    ← NOUVEAU — tests unitaires extraction claims
    SecurityConfigIntegrationTest.java     ← NOUVEAU — test 401 sur endpoint protégé

oude-frontend/src/app/
  core/
    auth/
      auth.interceptor.ts                  ← NOUVEAU — injecte Bearer token sur /api/**
      auth.guard.ts                        ← NOUVEAU — bloque navigation si non authentifié
      auth.service.ts                      ← NOUVEAU — gestion état auth, décodage JWT côté front
    models/
      vendeur.model.ts                     ← NOUVEAU — interface VendeurProfile (prénom, email, etc.)
  app.config.ts                            ← UPDATE — enregistrer AuthInterceptor + AuthGuard
  app.routes.ts                            ← UPDATE — appliquer AuthGuard sur toutes les routes
```

---

## Implémentation back-end

### VendeurIdentity.java

```java
package com.renault.oude.security;

public record VendeurIdentity(
    String vendeurId,        // TODO: AQ-1 — CLAIM_VENDEUR_ID (ex: "sub" ou claim propriétaire)
    String prenom,
    String email,
    String portefeuilleId    // TODO: AQ-1 — CLAIM_PORTEFEUILLE (ex: "portfolio_id")
) {
    public static VendeurIdentity fromMock(String vendeurId) {
        return new VendeurIdentity(vendeurId, "Natasha", "natasha.martin@renault.com", "PF-001");
    }
}
```

### JwtAuthenticationConverter.java

```java
package com.renault.oude.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // TODO: AQ-1 — CLAIM_VENDEUR_ID : remplacer "sub" par le claim réel Renault
    private static final String CLAIM_VENDEUR_ID   = "sub";
    // TODO: AQ-1 — CLAIM_PORTEFEUILLE : remplacer par le claim réel Renault
    private static final String CLAIM_PORTEFEUILLE = "portfolio_id";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var identity = new VendeurIdentity(
            jwt.getClaimAsString(CLAIM_VENDEUR_ID),
            jwt.getClaimAsString("given_name"),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString(CLAIM_PORTEFEUILLE)
        );
        return new JwtAuthenticationToken(jwt, List.of(), identity.vendeurId()) {
            @Override public Object getDetails() { return identity; }
        };
    }

    // Helper statique pour récupérer l'identité depuis n'importe quel service
    public static VendeurIdentity currentVendeur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jat) {
            return (VendeurIdentity) jat.getDetails();
        }
        throw new IllegalStateException("Aucun vendeur authentifié dans le contexte");
    }
}
```

### SecurityConfig.java

```java
package com.renault.oude.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                  // API REST stateless — pas de CSRF
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()   // healthcheck GKE sans auth
                .requestMatchers("/actuator/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            );
        return http.build();
    }
}
```

### application.yml — section security à ajouter

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # TODO: AQ-1 — JWKS_URI : remplacer par l'URL réelle de l'IdP Renault
          # Ex: https://sso.renault.com/.well-known/jwks.json
          jwks-uri: ${SSO_JWKS_URI:http://localhost:9090/jwks}
          # TODO: AQ-1 : issuer à confirmer avec Renault sécurité
          issuer-uri: ${SSO_ISSUER_URI:http://localhost:9090}
```

### application-local.yml (profil local — ne pas committer si contient des secrets)

```yaml
# Profil local — mock SSO, pas de connexion à l'IdP Renault réel
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwks-uri: http://localhost:9090/jwks   # WireMock ou mock local

# Identité mock du vendeur (configurable pour tester différents portefeuilles)
oude:
  mock:
    vendeur-id: "VEND-001"
    prenom: "Natasha"
    email: "natasha.martin@renault.com"
    portefeuille-id: "PF-BOULOGNE-001"
```

### MockJwtConfig.java — mock SSO pour développement local

```java
package com.renault.oude.security;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Génère une paire RSA locale pour signer/valider des JWT de test.
 * Actif uniquement avec le profil "local" — jamais en GKE.
 */
@Configuration
@Profile("local")
public class MockJwtConfig {

    @Bean
    public RSAKey mockRsaKey() throws Exception {
        // Génère une clé RSA 2048 bits éphémère au démarrage
        return new RSAKeyGenerator(2048)
            .keyID("mock-key-local")
            .generate();
    }
}
```

**Pour générer un token de test en local :**
```bash
# Script utilitaire (à placer dans scripts/generate-mock-token.sh)
curl -s -X POST http://localhost:9090/token \
  -d "vendeurId=VEND-001&prenom=Natasha&portefeuilleId=PF-001" | jq .access_token
```

---

## Implémentation front-end Angular

### auth.service.ts

```typescript
// src/app/core/auth/auth.service.ts
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

export interface VendeurProfile {
  vendeurId: string;
  prenom: string;
  email: string;
  portefeuilleId: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _profile: VendeurProfile | null = null;

  constructor(private router: Router) {}

  // Appelle le back-end pour récupérer le profil (back extrait du token)
  // Ne jamais décoder le token côté front pour les décisions de sécurité
  async loadProfile(): Promise<void> {
    const res = await fetch('/api/auth/me');
    if (!res.ok) {
      this.redirectToSso();
      return;
    }
    this._profile = await res.json();
  }

  get profile(): VendeurProfile | null { return this._profile; }
  get isAuthenticated(): boolean { return this._profile !== null; }

  redirectToSso(): void {
    // TODO: AQ-1 — SSO_REDIRECT_URI : remplacer par l'URL réelle du SSO Renault
    // En local, rediriger vers le mock SSO
    const ssoUrl = `${window.location.origin}/api/auth/sso-redirect`;
    window.location.href = ssoUrl;
  }
}
```

### auth.interceptor.ts

```typescript
// src/app/core/auth/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

// L'intercepteur ajoute le token JWT sur tous les appels /api/**
// Le token est stocké en mémoire (sessionStorage), jamais en localStorage
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('oudejwt');
  if (token && req.url.startsWith('/api')) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
```

### auth.guard.ts

```typescript
// src/app/core/auth/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  if (!auth.isAuthenticated) {
    await auth.loadProfile();
  }
  if (!auth.isAuthenticated) {
    auth.redirectToSso();
    return false;
  }
  return true;
};
```

### app.routes.ts — protéger toutes les routes

```typescript
// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: 'home', loadComponent: () => import('./features/home/home.component') },
      { path: 'clients/:id', loadComponent: () => import('./features/fiche-client/fiche-client.component') },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  }
];
```

### Endpoint back-end `/api/auth/me`

Ajouter dans le back-end :

```java
package com.renault.oude.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public VendeurIdentity me() {
        return JwtAuthenticationConverter.currentVendeur();
    }
}
```

---

## Sécurité — règles impératives

| ✅ À faire | ❌ Ne jamais faire |
|-----------|-------------------|
| Valider le JWT côté back-end à chaque requête | Faire confiance au front Angular pour la validation |
| Stocker le token en `sessionStorage` (perdu à la fermeture onglet) | Stocker en `localStorage` (persiste, risque XSS) |
| Logger les erreurs d'auth en ERROR (sans le token) | Logger le token JWT brut (données sensibles) |
| Utiliser `SecurityContextHolder` pour accéder à `VendeurIdentity` | Passer `vendeurId` en paramètre d'URL ou body |
| `SessionCreationPolicy.STATELESS` (API REST) | Utiliser les sessions HTTP (incompatible JWT) |
| Désactiver CSRF (API REST stateless) | Activer CSRF sur une API REST (inutile et cassant) |
| `/actuator/health` accessible sans auth (sonde GKE) | Protéger le healthcheck (GKE ne peut plus sonder) |
| Externaliser JWKS_URI dans `application.yml` | Hardcoder l'URL de l'IdP Renault dans le code |

---

## Tests à implémenter

### JwtAuthenticationConverterTest.java

```java
@Test
void extractVendeurId_fromSubClaim() {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .claim("sub", "VEND-001")
        .claim("given_name", "Natasha")
        .claim("email", "natasha@renault.com")
        .claim("portfolio_id", "PF-001")   // TODO: AQ-1 — claim réel
        .build();

    var token = converter.convert(jwt);
    var identity = (VendeurIdentity) token.getDetails();

    assertThat(identity.vendeurId()).isEqualTo("VEND-001");
    assertThat(identity.prenom()).isEqualTo("Natasha");
    assertThat(identity.portefeuilleId()).isEqualTo("PF-001");
}

@Test
void missingVendeurId_throwsException() {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .claim("email", "natasha@renault.com")
        // pas de "sub"
        .build();

    assertThatThrownBy(() -> converter.convert(jwt))
        .isInstanceOf(IllegalArgumentException.class);
}
```

### SecurityConfigIntegrationTest.java

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Test
    void healthcheck_accessible_sans_auth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    void api_retourne_401_sans_token() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void api_retourne_200_avec_token_valide() throws Exception {
        String token = generateMockToken("VEND-001");
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vendeurId").value("VEND-001"));
    }
}
```

---

## Checklist de complétion

- [ ] `GET /api/auth/me` retourne `{ vendeurId, prenom, email, portefeuilleId }` avec un token valide
- [ ] `GET /api/auth/me` sans token retourne HTTP 401
- [ ] `GET /actuator/health` retourne 200 sans token (sonde GKE)
- [ ] Le profil `local` fonctionne sans connexion à l'IdP Renault réel
- [ ] Le front Angular redirige vers SSO si non authentifié
- [ ] Tous les appels `/api/**` Angular incluent le header `Authorization: Bearer`
- [ ] Tous les `TODO: AQ-1` sont documentés et localisables par `grep "TODO: AQ-1" -r ./src`
- [ ] Aucun token JWT dans les logs
- [ ] Tests JwtAuthenticationConverterTest passent
- [ ] Tests SecurityConfigIntegrationTest passent

---

## Intelligence Story 1.1 — ce qui existe déjà

La Story 1.1 a créé :
- `pom.xml` avec `spring-boot-starter-web`, `spring-boot-starter-actuator` → **ajouter** `spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server`
- `OudeApplication.java` → **ne pas modifier**
- `application.yml` avec datasource + flyway → **ajouter** la section `spring.security`
- `app.config.ts` Angular → **ajouter** `provideHttpClient(withInterceptors([authInterceptor]))`
- `app.routes.ts` Angular → **modifier** pour ajouter `canActivate: [authGuard]`

**⚠️ Ne pas casser :** le endpoint `/actuator/health` doit rester accessible sans auth (sonde GKE readinessProbe).
