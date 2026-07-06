---
status: ready-for-dev
epic: 1
story: 4
jira: SCRUM-34
---

# Story 1.4 — Couche configuration pays (Multi-Country Config Layer)

## User Story
En tant qu'architecte OUDE, je veux que chaque déploiement du backend lise sa configuration pays depuis `config/countries/{cc}/country-config.yml` au démarrage, afin que l'ajout d'un nouveau pays ne nécessite aucune modification de code.

## Contexte & motivation
Le SDR Context impose : **"build once, deploy many"**. La France est le premier pays (Lot 1), mais le socle doit permettre à d'autres pays de s'onboarder en 7 étapes sans ligne de code (AD-10, ARCHITECTURE-SPINE v2).

## Acceptance Criteria

### AC1 — Chargement config pays au démarrage
**Given** l'env `OUDE_COUNTRY_CODE=fr`  
**When** l'application Spring Boot démarre  
**Then** la config `config/countries/fr/country-config.yml` est chargée et injectée via `@ConfigurationProperties`

### AC2 — Bean CountryConfig disponible partout
**Given** `@Value("${oude.country-code}")` = "fr"  
**When** un service injecte `CountryConfig`  
**Then** il obtient les valeurs correctes : currency=EUR, locale=fr-FR, timezone=Europe/Paris

### AC3 — Fallback gracieux sur pays inconnu
**Given** `OUDE_COUNTRY_CODE=xx` (pays sans config)  
**When** l'application démarre  
**Then** elle lève une `CountryConfigNotFoundException` avec un message clair plutôt que de démarrer avec des valeurs incorrectes

### AC4 — Test H2 compatible
**Given** le profil test avec H2  
**When** les tests Spring Boot tournent  
**Then** la config fr est chargée et les beans `CountryConfig` sont testables

## Tasks

- [ ] T1 — Créer `CountryConfig.java` (@ConfigurationProperties, record ou POJO)
- [ ] T2 — Créer `CountryConfigLoader.java` : lit `config/countries/{country-code}/country-config.yml` via ResourceLoader
- [ ] T3 — Enregistrer le bean dans `@Configuration` avec validation au démarrage (AC3)
- [ ] T4 — Ajouter `src/test/resources/config/countries/fr/country-config.yml` (copie du fichier main pour les tests)
- [ ] T5 — Test unitaire `CountryConfigLoaderTest` : vérifier chargement FR et comportement sur pays inconnu

## Dev Notes

**Architecture :**
- `CountryConfig` contient : countryCode, name, locale, currency, timezone, formats, features
- Le loader résout le chemin : `classpath:config/countries/{cc}/country-config.yml`
- Utiliser `YamlPropertiesFactoryBean` ou `@PropertySource` avec YAML custom
- Attention : Spring Boot ne supporte pas `@PropertySource` natif sur YAML → utiliser `YamlPropertySourceFactory`

**Fichier de base FR déjà créé :**
`src/main/resources/config/countries/fr/country-config.yml`

**Pas d'interface REST exposée** — config interne uniquement (Lot 1).

**Contrainte H2 :** pas de type PG-spécifique dans les beans — uniquement des types Java standard (String, List, Map).

## Classification (AD-10)
- Le mécanisme de chargement est **GLOBAL** (partagé)
- Le contenu de `country-config.yml` est **CONFIGURABLE** (par pays)
- Aucune extension locale nécessaire pour le Lot 1

## Definition of Done
- [ ] AC1 à AC4 verts
- [ ] Tests passent (`./mvnw test`)
- [ ] JIRA SCRUM-34 → En cours de revue
