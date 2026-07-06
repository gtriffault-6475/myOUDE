---
status: ready-for-dev
epic: 1
story: 5
jira: SCRUM-35
---

# Story 1.5 — Internationalisation (i18n) — ngx-translate + MessageSource

## User Story
En tant que vendeur Renault, je veux que l'application s'affiche dans ma langue, et qu'un futur déploiement dans un autre pays puisse utiliser une autre langue sans modification de code.

## Contexte & motivation
SDR Principle 6 : _"Language must not be hardcoded in UI, backend logic, prompts, tests or generated assets."_  
- Lot 1 : langue FR uniquement, mais infrastructure i18n 100% opérationnelle
- Fondation posée en Story 1.5 ; toutes les stories suivantes utilisent les clés i18n

## Acceptance Criteria

### AC1 — Angular : ngx-translate chargé au démarrage
**Given** l'app Angular est ouverte  
**When** elle démarre  
**Then** `TranslateService` charge `assets/i18n/fr.json` et la langue par défaut est `fr`

### AC2 — Toutes les strings visibles utilisent des clés i18n
**Given** la page portefeuille est affichée  
**When** l'inspecteur DOM est ouvert  
**Then** aucun texte français n'est hardcodé dans les templates — tout passe par `translate` pipe ou `translate.instant()`

### AC3 — Messages d'erreur backend traduits côté frontend
**Given** le backend retourne une erreur `portfolio.unavailable`  
**When** le frontend reçoit l'erreur  
**Then** le message affiché est `PORTFOLIO.ERROR_STALE` traduit depuis `fr.json`

### AC4 — Spring MessageSource disponible pour les messages backend
**Given** `PortefeuilleService` lance `PortefeuilleIndisponibleException("portfolio.unavailable")`  
**When** Spring retourne la réponse d'erreur  
**Then** le message résolu est `"Données Renault temporairement indisponibles"` (via `messages_fr.properties`)

### AC5 — Changement de langue sans redéploiement (prep)
**Given** un nouveau fichier `assets/i18n/de.json` est déposé  
**When** `translate.use('de')` est appelé  
**Then** l'interface bascule en allemand (test manuel — pas d'UI de sélection de langue en Lot 1)

## Tasks

- [ ] T1 — Vérifier installation `@ngx-translate/core` + `@ngx-translate/http-loader` (déjà dans package.json — lancer `npm install`)
- [ ] T2 — Valider `app.config.ts` : `TranslateModule.forRoot` avec `TranslateHttpLoader` (déjà fait en Story 2.1 refacto)
- [ ] T3 — Auditer tous les templates Angular et remplacer les strings FR par `{{ 'KEY' | translate }}`
- [ ] T4 — Compléter `fr.json` avec toutes les clés nécessaires (leads list, deals list, header, navigation)
- [ ] T5 — Configurer `MessageSource` Spring Boot avec `i18n/messages_fr.properties` (déjà créé)
- [ ] T6 — Injecter `MessageSource` dans `PortefeuilleService` pour résoudre `portfolio.unavailable`
- [ ] T7 — Test backend : `messages_fr.properties` résout correctement les codes d'erreur
- [ ] T8 — Test Angular : `TranslateService` charge `fr.json` et traduit les clés (TestBed)

## Dev Notes

**Fichiers déjà créés par le refacto multi-pays :**
- `src/assets/i18n/fr.json` — clés PORTFOLIO, LEAD, DEAL, AUTH, COMMON
- `src/main/resources/i18n/messages.properties` — EN fallback
- `src/main/resources/i18n/messages_fr.properties` — FR
- `app.config.ts` — `TranslateModule.forRoot` configuré

**Templates à auditer :**  
Chercher tous les textes FR hardcodés dans `src/app/**/*.html` (leads list, header, error banner).

**Spring MessageSource config à ajouter dans `application.yml` :**
```yaml
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8
    use-code-as-default-message: false
```

**Pattern recommandé pour PortefeuilleService :**
```java
@Autowired MessageSource messageSource;
// dans catch :
throw new PortefeuilleIndisponibleException(
    messageSource.getMessage("portfolio.unavailable", null, LocaleContextHolder.getLocale()));
```

**Règle i18n (AD-9 ARCHITECTURE-SPINE) :**
- Codes techniques EN dans le DB et les API internes
- Traduction côté frontend via ngx-translate
- Seuls les messages d'erreur backend ont besoin de MessageSource (affichés dans les logs / réponses HTTP)

## Classification
- Infrastructure i18n : **GLOBAL**
- Contenu des fichiers de traduction : **LOCAL** (géré par chaque pays)

## Definition of Done
- [ ] AC1 à AC5 verts
- [ ] `npm test` passe
- [ ] `./mvnw test` passe
- [ ] Aucun string FR hardcodé dans les templates (audit grep)
- [ ] JIRA SCRUM-35 → En cours de revue
