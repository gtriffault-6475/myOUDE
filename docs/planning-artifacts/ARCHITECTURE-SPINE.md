---
title: "Architecture Spine — OUDE Lot 1"
status: revised
created: 2026-06-26
updated: 2026-07-06
altitude: initiative
revision: "v2 — multi-country by design (SDR context intégré)"
---

# Architecture Spine — OUDE Lot 1

## Paradigme

**BFF (Backend For Frontend) multi-pays avec stockage agrégé local, couche de configuration par pays et domaine de données natif.**

OUDE est une **plateforme retail configurable déployée une seule fois, instanciée pour chaque pays via configuration**. Elle consomme des APIs Renault via Apigee X, stocke localement les données agrégées dans PostgreSQL, et expose une interface vendeur unifiée. Le backend est le seul à parler aux systèmes Renault.

> **Principe fondateur :** *Build once, deploy many.* Les différences entre pays sont absorbées par la configuration, la localisation et des mécanismes d'extension contrôlés — jamais par des forks de code ou des développements locaux isolés.

**Lot 1 :** France uniquement en déploiement. Toutes les décisions d'architecture sont prises pour supporter plusieurs pays sans refactoring. L'i18n, la couche de configuration pays et les extension points existent dès Lot 1, même si seules les valeurs françaises sont renseignées.

**Exception — domaine natif OUDE :** le module essais véhicules introduit un domaine de données entièrement géré par OUDE (véhicules disponibles par concession, réservations d'essai). Ces données ne proviennent pas d'APIs Renault — elles sont créées et persistées directement dans PostgreSQL OUDE.

---

## Contraintes héritées (imposées par Renault)

| ID | Contrainte | Source |
|----|-----------|--------|
| C-1 | Cloud provider : GCP | Architecture Renault |
| C-2 | Frontend : Angular | Pattern S1 établi |
| C-3 | Backend : Java / Spring Boot | Pattern S2/S7 établi |
| C-4 | Base de données : PostgreSQL sur GCP Cloud SQL | Pattern S3 établi |
| C-5 | API Gateway : Apigee X pour toutes les APIs Renault | Pattern APIGEEX établi |
| C-6 | Déploiement : GKE (Kubernetes) | Pattern VPC DCE / Podgroup établi |
| C-7 | SSO : géré centralement par Renault — OUDE consomme le token | Pattern Renault central |

---

## Modèle multi-pays — classification des responsabilités

Chaque décision d'architecture, chaque entité de données et chaque règle métier est classifiée selon quatre niveaux :

| Niveau | Définition | Exemples OUDE | Gouvernance |
|--------|-----------|---------------|-------------|
| **GLOBAL** | Invariant pour tous les pays — jamais modifiable localement | BFF pattern, entités canoniques Lead/Customer, circuit breaker, JWT validation | Core OUDE — changement via PR centrale |
| **CONFIGURABLE** | Paramétrable par pays via fichier de config — aucun code à écrire | TTL cache, scoring weights, nb retries, timezone, currency format | Équipe OUDE — config PR |
| **LOCAL** | Valeur locale gérée par le pays — dans les fichiers i18n ou config pays | Labels UI, formats date/heure, templates emails, wording métier | Pays — dans `config/countries/{cc}/` |
| **EXTENSION** | Fonctionnalité spécifique à un pays, non présente dans le core | Règle fiscale locale, workflow réglementaire, champ obligatoire local | Pays — module isolé, interface contractualisée |

### Règle de décision

> Avant tout développement spécifique à un pays : peut-on le résoudre par CONFIGURABLE ou LOCAL ? Si oui, c'est la voie obligatoire. Un développement EXTENSION ne se justifie que si la configuration est insuffisante et doit être approuvé par la gouvernance centrale OUDE.

---

## Décisions d'architecture

### AD-1 — Un seul microservice backend (Lot 1)
**Niveau :** GLOBAL
**Binds :** déploiement, découpage en epics et stories
**Prevents :** fragmentation prématurée, complexité opérationnelle inutile
**Rule :** Le backend OUDE Lot 1 est un unique service Spring Boot déployé dans un pod GKE. Le découpage en microservices est reporté post-Lot 1. La frontière naturelle post-Lot 1 : service d'agrégation / service de notation / service IA.

### AD-2 — Stockage agrégé local avec actualisation à deux régimes
**Niveau :** GLOBAL (mécanisme) + CONFIGURABLE (fréquences)
**Binds :** modèle de données OUDE, stratégie d'appel aux APIs Renault
**Rule :**
- **Données portefeuille** (leads, affaires) : chargées au login, actualisées périodiquement. Fréquence : paramètre `oude.cache.portfolio-ttl-hours` (défaut : 1h).
- **Données client** (fiche 360°) : TTL à l'ouverture de la fiche. Durée : paramètre `oude.cache.client-ttl-minutes` (défaut : 15 min).
- En cas d'échec API Renault : servir le cache local avec indicateur visuel "données potentiellement datées" (libellé externalisé i18n).
- Seules les données nécessaires à l'affichage sont stockées.

### AD-3 — Communication front↔back : HTTP direct dans GKE
**Niveau :** GLOBAL
**Rule :** Front Angular → Backend OUDE via HTTP direct dans le cluster GKE. Apigee n'est pas sur le chemin front→back.

### AD-4 — Backend OUDE → APIs Renault : via Apigee X uniquement
**Niveau :** GLOBAL
**Rule :** Zéro appel direct aux systèmes Renault. Tous les appels passent par Apigee X avec timeout explicite et retry/backoff (voir AD-8).

### AD-5 — Notation de potentiel : logique back-end pure, sans modèle IA
**Niveau :** GLOBAL (mécanisme) + CONFIGURABLE (pondérations par pays)
**Rule :** Score calculé par règles métier pondérées dans le backend. Les pondérations sont des paramètres de configuration définis par pays dans `config/countries/{cc}/scoring.yml`. Aucun modèle ML en Lot 1.

### AD-6 — Génération IA : Vertex AI (Gemini), appel synchrone à la demande
**Niveau :** GLOBAL
**Rule :** Résumé client et recommandation d'action générés via Vertex AI à l'ouverture de la fiche. Cache session. Timeout 4s. Fallback "suggestion indisponible" (libellé i18n). Modèle Gemini (Flash vs Pro) : décidé aux tests de qualité.

### AD-7 — Identité vendeur : extraite du token SSO Renault
**Niveau :** GLOBAL (mécanisme) + CONFIGURABLE (claim names par environnement — AQ-1)
**Rule :** OUDE extrait l'identité du vendeur du token SSO Renault. Le backend valide la signature JWT à chaque requête. Le contexte vendeur inclut `locale` et `countryCode` (extraits du token ou déduits du déploiement). Credentials de service via Workload Identity GKE.

### AD-8 — Résilience : circuit breaker et dégradation gracieuse
**Niveau :** GLOBAL (mécanisme) + CONFIGURABLE (seuils par pays)
**Rule :** Tous les appels Apigee X et Vertex AI encadrés par Resilience4j. Paramètres configurables : `oude.resilience.retry.max-attempts` (défaut : 2), `oude.resilience.retry.wait-duration` (défaut : 500ms). Dégradation gracieuse systématique — jamais de page blanche sur erreur externe.

### AD-9 — Internationalisation : i18n natif, aucune string en dur ✨ NOUVEAU
**Niveau :** GLOBAL
**Binds :** toute interface utilisateur, tout message backend, tout libellé métier
**Prevents :** strings françaises hardcodées, forks de code pour chaque pays
**Rule :**
- **Frontend :** Angular i18n via `@ngx-translate/core`. Toutes les strings UI dans `assets/i18n/{locale}.json`. Locale résolue depuis `VendeurContext.locale` (extrait du token SSO ou paramètre de déploiement).
- **Backend :** `MessageSource` Spring avec fichiers `messages_{locale}.properties`. Aucun libellé en dur dans les exceptions, logs destinés aux utilisateurs ou réponses API.
- **Données :** les valeurs de statut en base sont des codes techniques anglais (`NEW`, `IN_PROGRESS`, `CLOSED`). La traduction vers les libellés affichés est faite côté frontend via i18n.
- **Lot 1 :** seul `fr` est traduit. La structure i18n est en place pour onboarder d'autres langues sans code.

### AD-10 — Couche de configuration pays ✨ NOUVEAU
**Niveau :** GLOBAL (mécanisme) + LOCAL (valeurs)
**Binds :** toutes les règles métier variables, formats locaux, paramètres opérationnels
**Prevents :** hardcoding de règles FR dans le code, impossibilité d'adapter sans redéploiement
**Rule :**
- Chaque pays dispose d'un profil de configuration dans `config/countries/{country-code}/`.
- Structure minimale par pays :
  ```
  config/countries/fr/
    application.yml      ← overrides Spring Boot (TTL, retry, scoring weights…)
    scoring.yml          ← pondérations score potentiel
    i18n/fr.json         ← traductions UI (Angular)
    messages_fr.properties ← messages backend
  ```
- Le profil pays est activé via la variable d'environnement `OUDE_COUNTRY_CODE` injectée au déploiement GKE.
- Les valeurs par défaut sont celles de France en Lot 1. L'onboarding d'un nouveau pays = créer son dossier `config/countries/{cc}/` sans toucher au code.

### AD-11 — Modèle de données en anglais, libellés externalisés ✨ NOUVEAU
**Niveau :** GLOBAL
**Binds :** schéma PostgreSQL, noms de colonnes, valeurs de statuts
**Prevents :** couplage entre la langue technique et la langue du pays déployé
**Rule :**
- Noms de tables et colonnes en **anglais** (`leads`, `deals`, `portfolio_sync`, `status`, `potential_score`…).
- Les valeurs de statut sont des **codes techniques anglais** (`NEW`, `IN_PROGRESS`, `CLOSED`, `RENEWAL`…) jamais des libellés traduits.
- La traduction des statuts en labels affichés est faite par le frontend via le dictionnaire i18n.
- Les colonnes `country_code` (VARCHAR 2) et `locale` (VARCHAR 10) sont présentes sur les entités partagées.

---

## Flux de données

```
Vendeur
  │  HTTPS
  ▼
[Front Angular — pod GKE]
  │  HTTP interne GKE
  ▼
[Backend BFF — pod GKE]  ←─── OUDE_COUNTRY_CODE (env var déploiement)
  │                │                    │
  │          Country Config        VendeurContext
  │          (TTL, scoring,        (locale, countryCode)
  │           retry params)
  │
  ├──── lecture/écriture ────► PostgreSQL Cloud SQL
  │                            (colonnes EN, country_code)
  │
  ├──── HTTPS via Apigee X ──► APIs Renault
  │     (retry + circuit breaker)
  │
  └──── appel synchrone 4s ──► Vertex AI Gemini
```

---

## Modèle de déploiement multi-pays

```
Déploiement France                  Déploiement (futur pays)
──────────────────                  ──────────────────────────
OUDE_COUNTRY_CODE=fr                OUDE_COUNTRY_CODE=xx
  └─ config/countries/fr/             └─ config/countries/xx/
       application.yml                     application.yml
       scoring.yml                         scoring.yml
       i18n/fr.json                        i18n/xx.json
       messages_fr.properties             messages_xx.properties

Même image Docker ──────────────────────────────────────────►
Même schéma PostgreSQL (multi-tenant par country_code)
Même pipeline CI/CD
```

Un nouveau pays = un nouveau dossier de config + une entrée DNS/GKE. Zéro fork de code.

---

## Domaines de données — classification multi-pays

| Domaine | Niveau | TTL / fréquence | Clé pays | Notes |
|---------|--------|-----------------|----------|-------|
| Leads du vendeur | GLOBAL + CONFIGURABLE (TTL) | `portfolio-ttl-hours` | `country_code` | Statuts en anglais |
| Affaires en cours | GLOBAL + CONFIGURABLE (TTL) | `portfolio-ttl-hours` | `country_code` | |
| Fiche client | GLOBAL + CONFIGURABLE (TTL) | `client-ttl-minutes` | `country_code` | |
| Véhicule + historique | GLOBAL + CONFIGURABLE (TTL) | `client-ttl-minutes` | `country_code` | |
| Financement | GLOBAL + CONFIGURABLE (TTL) | `client-ttl-minutes` | `country_code` | Données sensibles |
| Historique interactions | GLOBAL + CONFIGURABLE | TTL 5 min | `country_code` | Lu + écrit |
| Signaux digitaux | GLOBAL + CONFIGURABLE | `client-ttl-minutes` | `country_code` | |
| Score de potentiel | GLOBAL (mécanisme) + CONFIGURABLE (weights) | Dérivé TTL client | — | Recalculé, non stocké |
| Suggestion IA | GLOBAL | Cache session | — | Non persistée |
| Véhicules essais | NATIF OUDE — GLOBAL | Statique Lot 1 | `dealership_id` | Seeded par concession |
| Réservations essai | NATIF OUDE — GLOBAL | Persisté | `country_code` | |

---

## Sécurité

| Périmètre | Décision | Niveau |
|-----------|----------|--------|
| Transport externe | HTTPS obligatoire | GLOBAL |
| Transport interne | HTTP dans le cluster GKE | GLOBAL |
| Validation token | Backend valide JWT SSO Renault (clé publique IdP) | GLOBAL |
| Claim names JWT | Configurables via `application.yml` (AQ-1) | CONFIGURABLE |
| Credentials de service | GCP Secret Manager + Workload Identity GKE — aucun secret en variable ou dans le code | GLOBAL |
| Isolation des données | Filtrées par `vendeur_id` extrait du token + `country_code` du déploiement | GLOBAL |

---

## Internationalisation — règles d'implémentation

### Frontend (Angular)
```
assets/i18n/
  fr.json          ← Lot 1 : France
  (en.json)        ← à venir
  (es.json)        ← à venir

Chargement : TranslateModule.forRoot() avec TranslateHttpLoader
Usage : {{ 'PORTFOLIO.STALE_DATA_BANNER' | translate }}
Jamais : une string FR en dur dans un template ou un composant
```

### Backend (Spring Boot)
```
src/main/resources/
  messages_fr.properties    ← Lot 1 : France
  messages.properties       ← fallback anglais

Usage : messageSource.getMessage("error.portfolio.unavailable", null, locale)
Jamais : new RuntimeException("Données indisponibles")
```

### Valeurs de statut (DB → UI)
```
DB (code technique EN) → Backend (passe tel quel) → Frontend (i18n lookup)
"NEW"        →  fr.json: "LEAD.STATUS.NEW": "Nouveau"
"IN_PROGRESS" →  fr.json: "LEAD.STATUS.IN_PROGRESS": "En cours"
"COLD"       →  fr.json: "LEAD.STATUS.COLD": "Froid"
```

---

## Observabilité

- **Logs :** JSON structurés vers GCP Cloud Logging. Champ `country_code` présent dans chaque log structuré.
- **Métriques :** latences et taux d'erreur Apigee + Vertex AI → GCP Cloud Monitoring, taguées `country_code`.
- **Suivi tokens IA :** consommation Vertex AI trackée par appel et par pays.
- **Alertes :** à définir avec l'équipe ops Renault, par pays.

---

## Objectifs de performance

| Surface | Cible | Contrainte IA |
|---------|-------|--------------|
| Homepage (données portefeuille) | < 3 secondes | Sans appel IA |
| Fiche client (données + notation + suggestion IA) | < 5 secondes | Timeout Vertex AI : 4s max |

Ces objectifs s'appliquent à tous les pays déployés.

---

## Décisions reportées

- **Envoi email réel :** simulé en Lot 1. Intégration SMTP/SendGrid post-Lot 1.
- **Synchronisation essais avec DMS/CRM :** données OUDE uniquement en Lot 1.
- **UI d'administration flotte essais :** seedée en base en Lot 1.
- **Pub/Sub événementiel :** non retenu Lot 1.
- **Découpage microservices :** 1 service Lot 1. Frontière naturelle : agrégation / notation / IA.
- **Exposition APIs OUDE via Apigee :** non retenu Lot 1.
- **Choix modèle Gemini :** Flash vs Pro décidé aux tests de qualité.
- **Invalidation explicite du cache :** TTL pur en Lot 1. Invalidation sur événement Pub/Sub post-Lot 1.
- **Multi-tenant DB vs déploiement séparé par pays :** les deux patterns sont supportés par l'architecture. Le choix (colonne `country_code` dans un seul cluster vs instances séparées par pays) est reporté à la définition du modèle opérationnel post-Lot 1.

---

## Questions ouvertes

| # | Question | Impact | Propriétaire | Échéance |
|----|---------|--------|--------------|----------|
| AQ-1 | Format et claims du token SSO Renault (JWT, identifiant vendeur, portefeuille, endpoint de validation, claim `locale`/`country`) | AD-7, AD-9, filtrage données | Renault sécurité | Semaine 1 |
| AQ-2 | URLs endpoints Apigee X — leads | AD-4, Story 2.1 | Renault DSI | Semaine 1 |
| AQ-3 | URLs endpoints Apigee X — affaires | AD-4, Story 2.1 | Renault DSI | Semaine 1 |
| AQ-4 | Format réponses APIs Renault (champs, nommage) | Mapping entités JPA | Renault DSI | Semaine 1 |
| AQ-5 | Mécanisme auth BFF → Apigee X (OAuth2 client_credentials, API key, mTLS ?) | RenaultApiClient | Renault DSI | Semaine 1 |
| AQ-6 | GCP_PROJECT_ID, GKE_CLUSTER, GCP_REGION du projet OUDE | CI/CD déploiement | Renault infra | Semaine 1 |
| AQ-7 | Le token SSO Renault inclut-il `locale` et/ou `country_code` ? Sinon, quel mécanisme pour résoudre le pays du vendeur ? | AD-9, AD-10 | Renault sécurité | Semaine 1 |
| AQ-8 | Pondérations initiales du score de potentiel (signal financier, véhicule, digital) — valeurs France | AD-5, Story 2.2 | Renault produit | Semaine 2 |

---

## Annexe — Matrice d'onboarding d'un nouveau pays

Pour déployer OUDE dans un nouveau pays après la France :

| Étape | Action | Qui | Touche au code ? |
|-------|--------|-----|-----------------|
| 1 | Créer `config/countries/{cc}/application.yml` avec overrides locaux | Équipe OUDE + pays | Non |
| 2 | Créer `config/countries/{cc}/scoring.yml` avec pondérations locales | Équipe produit pays | Non |
| 3 | Traduire `assets/i18n/{locale}.json` (Angular) | Équipe UX pays | Non |
| 4 | Traduire `messages_{locale}.properties` (backend) | Équipe pays | Non |
| 5 | Configurer `OUDE_COUNTRY_CODE` dans le déploiement GKE | Infra | Non |
| 6 | Configurer les endpoints Apigee X locaux dans le profil pays | Équipe intégration | Non |
| 7 | Valider les règles métier locales — si non couvertes par config → Extension formelle | Gouvernance OUDE | Possible |
