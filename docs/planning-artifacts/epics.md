---
stepsCompleted: ["step-01", "step-02", "step-03"]
inputDocuments:
  - planning-artifacts/prds/prd-OUDE-2026-06-26/prd.md
  - planning-artifacts/architecture/architecture-OUDE-2026-06-26/ARCHITECTURE-SPINE.md
---

# OUDE — Epic Breakdown

## Overview

Ce document décompose les requirements du PRD et de l'Architecture Spine en epics et stories implémentables pour OUDE Lot 1.

---

## Requirements Inventory

### Functional Requirements

FR-1.1 : L'application utilise le SSO Renault comme unique mécanisme d'authentification. Aucun compte OUDE distinct n'est créé.
FR-1.2 : La session utilisateur est liée à l'identité SSO. Les données affichées sont filtrées selon le portefeuille du vendeur connecté.
FR-1.3 : En cas d'expiration de session, l'utilisateur est redirigé vers la page SSO sans perte de contexte.

FR-2.1.1 : La page affiche le prénom du vendeur connecté et la date du jour.
FR-2.1.2 : Un résumé textuel dynamique liste les éléments prioritaires du jour (calculé à partir des données du portefeuille).
FR-2.2.1 : Trois compteurs sont affichés : Relances urgentes, Nouveaux leads, Clients à risque.
FR-2.2.2 : Chaque compteur est cliquable et affiche la liste filtrée correspondante.
FR-2.2.3 : "Clients à risque" affiche les clients dont le score descend sous le seuil critique.
FR-2.2.4 : Un libellé contextuel sous chaque compteur donne l'information clé.
FR-2.3.1 : Liste des leads du vendeur classés par score de potentiel décroissant.
FR-2.3.2 : Colonnes : Client, Véhicule, Source, Statut, Délai de traitement recommandé.
FR-2.3.3 : Statuts possibles : Nouveau, Qualifié, À Relancer.
FR-2.3.4 : Le délai de traitement est calculé à partir du score et de l'ancienneté du lead.
FR-2.3.5 : Lien "Voir tous mes leads" vers la liste complète.
FR-2.3.6 : Chaque ligne de lead est cliquable et ouvre la Fiche Client.
FR-2.4.1 : Section affaires actives du vendeur.
FR-2.4.2 : Colonnes : Client, Véhicule, Statut, Prochaine étape, Date cible.
FR-2.4.3 : Statut reflète l'étape du pipeline commercial.
FR-2.4.4 : Lien "Toutes mes affaires" vers la liste complète.
FR-2.4.5 : Chaque ligne d'affaire est cliquable et ouvre la Fiche Client.

FR-3.1.1 : Header client : initiales (avatar), nom, téléphone, adresse, email, date de début de relation, nb véhicules achetés.
FR-3.1.2 : Résumé IA de 1-2 phrases synthétisant le profil client (généré à l'ouverture via Vertex AI).
FR-3.1.3 : Le vendeur référent est affiché (nom + photo).
FR-3.2.1 : Score exprimé en % (0–100) avec libellé qualitatif (Froid / Tiède / Chaud / Très chaud).
FR-3.2.2 : Calcul du score suit les règles SC-1 (signal financier > signal véhicule > signaux digitaux).
FR-3.2.3 : Infobulle détaillant les facteurs ayant contribué au score.
FR-3.2.4 : Score recalculé à chaque ouverture de fiche (données fraîches depuis APIs).
FR-3.3.1 : Consentements actifs/inactifs par canal (Téléphone, Email, Postal), affichés.
FR-3.3.2 : Consentements en lecture seule.
FR-3.4.1 : Bloc dédié à la recommandation d'action générée par IA à l'ouverture de la fiche.
FR-3.4.2 : Recommandation produite à la demande (Vertex AI), pas de pre-calcul batch.
FR-3.4.3 : Recommandation inclut texte libre + CTA contextuels.
FR-3.4.4 : Une seule recommandation par ouverture de fiche (pas de régénération Lot 1).
FR-3.4.5 : Les CTA de la recommandation déclenchent les actions de contact (F-3.7).
FR-3.5.1 : Véhicule actuel : modèle, année, kilométrage, type de contrat.
FR-3.5.2 : Services actifs liés au véhicule listés avec lien vers détail.
FR-3.5.3 : Financement actif : type, mensualité, durée restante/totale, date d'échéance, apport, valeur résiduelle, kilométrage contractuel.
FR-3.5.4 : Données véhicule/financement en lecture seule (APIs Renault).
FR-3.5.5 : Onglet "Historique" pour les véhicules précédents.
FR-3.6.1 : Pipeline visuel des étapes du cycle de vente : Lead → RDV → Test Drive → Opportunité → Offre → Commande → Livraison.
FR-3.6.2 : Étape courante mise en évidence.
FR-3.6.3 : Dernières opportunités listées avec date et source.
FR-3.6.4 : Affichage en lecture seule.
FR-3.6.5 : Bouton "Créer une nouvelle opportunité" (comportement TBD selon OQ-2/OQ-4).
FR-3.7.1 : Actions permanentes sur Fiche Client : Appeler et Envoyer un mail.
FR-3.7.2 : "Appeler" ouvre le composeur natif avec numéro pré-renseigné.
FR-3.7.3 : "Envoyer un mail" ouvre le client mail natif avec adresse pré-renseignée.
FR-3.7.4 : Chaque déclenchement crée automatiquement une entrée dans l'historique des interactions via API Renault.
FR-3.7.5 : Qualification post-interaction hors périmètre Lot 1.
FR-3.8.1 : Liste chronologique des interactions passées : date, type, résumé.
FR-3.8.2 : Interactions lues depuis API Renault (toutes sources, pas seulement OUDE).
FR-3.8.3 : Lien "Voir toutes les interactions" vers l'historique complet.
FR-3.9.1 : Signaux digitaux : dernière connexion Renault.fr, MyRenault, configurations enregistrées, statut fidélité.
FR-3.9.2 : Données signaux digitaux en lecture seule (APIs Renault).
FR-3.9.3 : Lien "Voir l'activité digitale" vers l'historique complet.

FR-4.1 : Widget AskRG intégré sur Homepage et Fiche Client (code fourni par Renault).
FR-4.2 : Widget positionné en barre de recherche en haut de chaque écran.
FR-4.3 : OUDE responsable uniquement de l'intégration, pas du développement du widget.
FR-4.4 : Identifiant client transmis au widget à l'ouverture de la Fiche Client (si API widget le permet).

FR-5.1 : Liste des véhicules disponibles à l'essai gérée par concession dans la base OUDE (seed direct en base pour Lot 1, pas d'admin UI).
FR-5.2 : Créneaux d'une heure disponibles affichés par véhicule sur calendrier glissant. Créneau indisponible dès qu'une réservation existe.
FR-5.3 : Prise de RDV depuis Fiche Client uniquement : sélection véhicule → sélection créneau → confirmation → enregistrement en base OUDE.
FR-5.4 : Confirmation email optionnelle au client — simulée en Lot 1 (log serveur + message de succès UI, aucun envoi réel).
FR-5.5 : Données essais (véhicules + réservations) propriétaires OUDE, pas issues d'API Renault.

### NonFunctional Requirements

NFR-1 : Intégration SSO Renault conforme aux standards OAuth2/OIDC (protocole exact à confirmer avec Renault).
NFR-2 : Consommation exclusive des APIs REST Renault via Apigee X — aucun accès direct aux bases de données.
NFR-3 : Écriture dans le système d'interactions Renault en temps réel lors des actions de contact.
NFR-4 : Homepage < 3s. Fiche Client (données + IA) < 5s.
NFR-5 : Conformité sécurité Groupe Renault. Données client visibles uniquement pour le vendeur référent ou son responsable.
NFR-6 : Application déployable dans l'environnement d'hébergement Renault (GKE).

### Additional Requirements

(Extraits de l'Architecture Spine)

- ARCH-1 : Un seul microservice Spring Boot backend en Lot 1 (AD-1). Pas de fragmentation.
- ARCH-2 : Stockage agrégé local dans PostgreSQL Cloud SQL — deux régimes de refresh : portefeuille (~1h) et client (TTL 15min) (AD-2).
- ARCH-3 : Communication front↔back via HTTP direct dans le cluster GKE — pas via Apigee (AD-3).
- ARCH-4 : Tous les appels backend→Renault passent obligatoirement par Apigee X (AD-4).
- ARCH-5 : Score de potentiel = règles métier pondérées dans le backend, pas de ML (AD-5). Paramètres de pondération configurables.
- ARCH-6 : Vertex AI Gemini pour résumé client + recommandation d'action. Timeout 4s. Cache session. Fallback gracieux (AD-6).
- ARCH-7 : Identité vendeur extraite du token SSO. Credentials service dans GCP Secret Manager + Workload Identity GKE (AD-7).
- ARCH-8 : Resilience4j sur tous les appels externes — retry (max 2), circuit breaker, timeout (AD-8).
- ARCH-9 : Logs structurés JSON → GCP Cloud Logging. Métriques Apigee/Vertex AI → GCP Cloud Monitoring. Tokens IA trackés par appel.
- ARCH-10 : Données essais (véhicules + réservations) gérées nativement dans PostgreSQL OUDE — pas via Apigee.
- ARCH-11 : Email confirmation essai = stub en Lot 1 : logger côté serveur, afficher succès UI, aucun envoi réel.

### UX Design Requirements

Pas de document UX formel pour ce projet. Les maquettes (screenshots partagés en session PRD) définissent le périmètre visuel mais ne constituent pas un contrat UX formel. Les requirements visuels sont couverts par le PRD.

### FR Coverage Map

FR-1.1 : Epic 1 — SSO comme unique mécanisme d'authentification
FR-1.2 : Epic 1 — Session liée à l'identité SSO, filtrage par portefeuille
FR-1.3 : Epic 1 — Redirection SSO en cas d'expiration de session

FR-2.1.1 : Epic 2 — Header Homepage (prénom vendeur, date du jour)
FR-2.1.2 : Epic 2 — Résumé dynamique des priorités du jour
FR-2.2.1 : Epic 2 — Trois compteurs priorités du jour
FR-2.2.2 : Epic 2 — Compteurs cliquables → liste filtrée
FR-2.2.3 : Epic 2 — "Clients à risque" sous seuil critique
FR-2.2.4 : Epic 2 — Libellé contextuel sous chaque compteur
FR-2.3.1 : Epic 2 — Liste leads classés par score décroissant
FR-2.3.2 : Epic 2 — Colonnes leads (Client, Véhicule, Source, Statut, Délai)
FR-2.3.3 : Epic 2 — Statuts leads (Nouveau, Qualifié, À Relancer)
FR-2.3.4 : Epic 2 — Délai de traitement calculé depuis score + ancienneté
FR-2.3.5 : Epic 2 — Lien "Voir tous mes leads"
FR-2.3.6 : Epic 2 — Clic lead → Fiche Client
FR-2.4.1 : Epic 2 — Section affaires actives
FR-2.4.2 : Epic 2 — Colonnes affaires (Client, Véhicule, Statut, Prochaine étape, Date cible)
FR-2.4.3 : Epic 2 — Statut = étape pipeline commercial
FR-2.4.4 : Epic 2 — Lien "Toutes mes affaires"
FR-2.4.5 : Epic 2 — Clic affaire → Fiche Client

FR-3.1.1 : Epic 3 — Header client (initiales, nom, téléphone, adresse, email, relation, véhicules)
FR-3.1.2 : Epic 4 — Résumé IA 1-2 phrases (Vertex AI à l'ouverture)
FR-3.1.3 : Epic 3 — Vendeur référent affiché (nom + photo)
FR-3.2.1 : Epic 3 — Score 0–100% avec libellé qualitatif
FR-3.2.2 : Epic 2 — Calcul score règles SC-1 (signal financier > signal véhicule > signaux digitaux)
FR-3.2.3 : Epic 3 — Infobulle facteurs de score
FR-3.2.4 : Epic 3 — Score recalculé à chaque ouverture de fiche
FR-3.3.1 : Epic 3 — Consentements par canal (Téléphone, Email, Postal)
FR-3.3.2 : Epic 3 — Consentements en lecture seule
FR-3.4.1 : Epic 4 — Bloc recommandation d'action IA
FR-3.4.2 : Epic 4 — Recommandation à la demande (Vertex AI), pas de batch
FR-3.4.3 : Epic 4 — Recommandation = texte libre + CTAs contextuels
FR-3.4.4 : Epic 4 — Une seule recommandation par ouverture (pas de régénération Lot 1)
FR-3.4.5 : Epic 4 — CTAs déclenchent les actions de contact (FR-3.7)
FR-3.5.1 : Epic 3 — Véhicule actuel (modèle, année, km, contrat)
FR-3.5.2 : Epic 3 — Services actifs liés au véhicule
FR-3.5.3 : Epic 3 — Financement actif (mensualité, durée, échéance, VR, km contractuel)
FR-3.5.4 : Epic 3 — Données véhicule/financement en lecture seule
FR-3.5.5 : Epic 3 — Onglet "Historique" véhicules précédents
FR-3.6.1 : Epic 3 — Pipeline visuel cycle de vente (Lead → … → Livraison)
FR-3.6.2 : Epic 3 — Étape courante mise en évidence
FR-3.6.3 : Epic 3 — Dernières opportunités listées
FR-3.6.4 : Epic 3 — Pipeline en lecture seule
FR-3.6.5 : Epic 3 — Bouton "Créer une nouvelle opportunité" (comportement TBD)
FR-3.7.1 : Epic 3 — Actions permanentes Fiche Client : Appeler + Envoyer un mail
FR-3.7.2 : Epic 3 — "Appeler" → composeur natif avec numéro pré-renseigné
FR-3.7.3 : Epic 3 — "Envoyer un mail" → client mail natif avec adresse pré-renseignée
FR-3.7.4 : Epic 3 — Log automatique dans l'historique interactions Renault
FR-3.7.5 : Epic 3 — Qualification post-interaction hors périmètre Lot 1
FR-3.8.1 : Epic 3 — Historique interactions chronologique (date, type, résumé)
FR-3.8.2 : Epic 3 — Interactions lues depuis API Renault (toutes sources)
FR-3.8.3 : Epic 3 — Lien "Voir toutes les interactions"
FR-3.9.1 : Epic 3 — Signaux digitaux (connexion Renault.fr, MyRenault, configurations, fidélité)
FR-3.9.2 : Epic 3 — Signaux digitaux en lecture seule
FR-3.9.3 : Epic 3 — Lien "Voir l'activité digitale"

FR-4.1 : Epic 5 — Widget AskRG intégré Homepage + Fiche Client
FR-4.2 : Epic 5 — Widget positionné en barre de recherche
FR-4.3 : Epic 5 — OUDE responsable uniquement de l'intégration
FR-4.4 : Epic 5 — Identifiant client transmis au widget à l'ouverture Fiche Client

FR-5.1 : Epic 6 — Liste véhicules disponibles gérée en base OUDE (seed Lot 1)
FR-5.2 : Epic 6 — Créneaux 1h par véhicule sur calendrier glissant
FR-5.3 : Epic 6 — Flux réservation depuis Fiche Client uniquement
FR-5.4 : Epic 6 — Confirmation email simulée (log + UI success, pas d'envoi réel)
FR-5.5 : Epic 6 — Données essais propriétaires OUDE

## Epic List

### Epic 1 — Accès sécurisé à l'application (SSO + fondations)
Les vendeurs peuvent accéder à l'application via le SSO Renault. L'infrastructure de base (Angular, Spring Boot, PostgreSQL, GKE) est opérationnelle et sécurisée.
**FRs couverts :** FR-1.1, FR-1.2, FR-1.3 | **ARCH :** ARCH-1, ARCH-3, ARCH-7 | **NFR :** NFR-1, NFR-5, NFR-6
**JIRA :** SCRUM-5

### Epic 2 — Homepage : vue portefeuille du vendeur
Le vendeur voit en un coup d'œil ses priorités du jour : compteurs d'alertes, leads prioritaires classés par score et affaires en cours. Le moteur de scoring est opérationnel.
**FRs couverts :** FR-2.1.1, FR-2.1.2, FR-2.2.1–4, FR-2.3.1–6, FR-2.4.1–5, FR-3.2.2 | **ARCH :** ARCH-2, ARCH-4, ARCH-5, ARCH-8, ARCH-9 | **NFR :** NFR-2, NFR-4
**JIRA :** SCRUM-6

### Epic 3 — Fiche Client 360°
Le vendeur accède à la vue complète d'un client : identité, véhicule, financement, consentements, pipeline, signaux digitaux, historique interactions, et peut déclencher un appel ou un email avec log automatique.
**FRs couverts :** FR-3.1.1, FR-3.1.3, FR-3.2.1, FR-3.2.3–4, FR-3.3.1–2, FR-3.5.1–5, FR-3.6.1–5, FR-3.7.1–5, FR-3.8.1–3, FR-3.9.1–3 | **ARCH :** ARCH-2, ARCH-4, ARCH-8, ARCH-9 | **NFR :** NFR-2, NFR-3, NFR-4
**JIRA :** SCRUM-7

### Epic 4 — Intelligence artificielle : résumé client & recommandation d'action
Les vendeurs bénéficient de deux enrichissements IA à l'ouverture de la Fiche Client : un résumé du profil et une recommandation d'action avec CTAs contextuels, via Vertex AI Gemini.
**FRs couverts :** FR-3.1.2, FR-3.4.1–5 | **ARCH :** ARCH-6, ARCH-9
**JIRA :** SCRUM-8

### Epic 5 — Intégration widget AskRG
Le widget AskRG (fourni par Renault) est intégré en barre de recherche sur la Homepage et la Fiche Client, avec transmission de l'identifiant client.
**FRs couverts :** FR-4.1–4.4
**JIRA :** SCRUM-9

### Epic 6 — Module essais véhicules
Le vendeur peut réserver un essai véhicule pour un client depuis la Fiche Client : sélection du véhicule disponible, choix du créneau, confirmation avec simulation d'email.
**FRs couverts :** FR-5.1–5.5 | **ARCH :** ARCH-10, ARCH-11
**JIRA :** SCRUM-10

---

## Epic 1 — Accès sécurisé à l'application (SSO + fondations)

Les vendeurs peuvent se connecter à OUDE via le SSO Renault et accéder à leurs données de portefeuille. L'infrastructure technique (Angular, Spring Boot, PostgreSQL, GKE) est opérationnelle et sécurisée.

### Story 1.1 — Bootstrap projet : Angular + Spring Boot + PostgreSQL + GKE

En tant que membre de l'équipe technique,
Je veux que l'infrastructure de base soit bootstrappée et déployable sur GKE,
Afin que l'équipe puisse développer et déployer les fonctionnalités OUDE en itérations rapides.

**Critères d'acceptance :**

**Étant donné** que le pipeline CI/CD est configuré
**Quand** un commit est poussé sur la branche principale
**Alors** l'application Angular est buildée et déployée dans le pod Front du cluster GKE
**Et** le service Spring Boot est buildé et déployé dans le pod Back du cluster GKE
**Et** la base PostgreSQL Cloud SQL est accessible depuis le pod Back via Workload Identity

**Étant donné** que les deux pods sont déployés
**Quand** le front Angular appelle le back Spring Boot
**Alors** la communication passe par HTTP interne Kubernetes (pas via Apigee)
**Et** le back répond avec un healthcheck 200

**JIRA :** SCRUM-11

---

### Story 1.2 — Intégration SSO Renault : authentification et extraction d'identité

En tant que vendeur Renault,
Je veux me connecter à OUDE via le SSO Renault,
Afin d'accéder à mes données de portefeuille sans créer de compte OUDE distinct.

**Critères d'acceptance :**

**Étant donné** que le vendeur n'est pas authentifié
**Quand** il accède à OUDE
**Alors** il est redirigé vers la page SSO Renault

**Étant donné** que le vendeur s'est authentifié via SSO
**Quand** le token SSO est reçu par le back-end
**Alors** le back valide la signature JWT (clé publique IdP Renault)
**Et** extrait l'identifiant vendeur et son portefeuille depuis les claims du token
**Et** filtre toutes les données retournées selon ce portefeuille

**Étant donné** que la session SSO expire
**Quand** le vendeur effectue une action
**Alors** il est redirigé vers la page SSO sans perte de contexte de navigation

**JIRA :** SCRUM-12

---

### Story 1.3 — Navigation shell : layout principal et routing Angular

En tant que vendeur,
Je veux un layout applicatif cohérent avec navigation entre les sections,
Afin de me repérer facilement dans OUDE et passer d'un écran à l'autre.

**Critères d'acceptance :**

**Étant donné** que le vendeur est authentifié
**Quand** il accède à l'application
**Alors** le layout principal s'affiche (header, navigation latérale ou top-bar, zone de contenu)
**Et** la Homepage est la vue par défaut

**Étant donné** que le vendeur est sur n'importe quel écran
**Quand** il clique sur une entrée de navigation
**Alors** le routing Angular charge la vue correspondante sans rechargement complet de la page

**JIRA :** SCRUM-13

---

## Epic 2 — Homepage : vue portefeuille du vendeur

Le vendeur dispose d'une vue centralisée de ses priorités du jour : compteurs d'alertes, leads classés par score et affaires en cours.

### Story 2.1 — Chargement et stockage du portefeuille vendeur depuis les APIs Renault

En tant que vendeur,
Je veux que mon portefeuille (leads et affaires) soit chargé depuis les APIs Renault et stocké localement,
Afin que la Homepage s'affiche rapidement même si les APIs Renault sont temporairement indisponibles.

**Critères d'acceptance :**

**Étant donné** que le vendeur vient de se connecter
**Quand** le scheduler de portefeuille s'exécute (ou au login)
**Alors** les données de leads et d'affaires en cours sont récupérées depuis les APIs Renault via Apigee X
**Et** stockées dans PostgreSQL OUDE avec horodatage

**Étant donné** que les données de portefeuille ont plus d'1h
**Quand** le scheduler s'exécute
**Alors** les données sont rafraîchies depuis les APIs Renault

**Étant donné** que l'API Renault est indisponible
**Quand** le front demande les données de portefeuille
**Alors** les données en cache sont servies avec un indicateur visuel "données potentiellement datées"

**JIRA :** SCRUM-14

---

### Story 2.2 — Moteur de scoring de potentiel client (règles métier back-end)

En tant que vendeur,
Je veux que chaque client de mon portefeuille ait un score de potentiel calculé,
Afin de prioriser mes actions vers les clients les plus susceptibles d'acheter.

**Critères d'acceptance :**

**Étant donné** que les données d'un client sont disponibles (données financières, véhicule, signaux digitaux)
**Quand** le score est calculé
**Alors** il suit les règles SC-1 : signal financier > signal véhicule > signaux digitaux
**Et** le résultat est un entier entre 0 et 100
**Et** les pondérations sont configurables (paramètres définis en semaine 1 avec Renault)

**Étant donné** qu'un signal est manquant pour un client
**Quand** le score est calculé
**Alors** la pondération manquante est ignorée et le score est normalisé sur les signaux disponibles

**JIRA :** SCRUM-15

---

### Story 2.3 — Header Homepage : prénom vendeur, date et résumé dynamique des priorités

En tant que vendeur,
Je veux voir mon prénom, la date du jour et un résumé textuel de mes priorités en haut de la Homepage,
Afin de démarrer ma journée avec une vision claire de ce qui m'attend.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur la Homepage
**Quand** la page est chargée
**Alors** son prénom (extrait du token SSO) et la date du jour sont affichés dans le header
**Et** un résumé textuel dynamique liste les éléments prioritaires calculés depuis les données du portefeuille

**JIRA :** SCRUM-16

---

### Story 2.4 — Compteurs priorités du jour (relances urgentes, nouveaux leads, clients à risque)

En tant que vendeur,
Je veux voir trois compteurs de priorités du jour en un coup d'œil,
Afin de savoir immédiatement combien d'actions urgentes m'attendent.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur la Homepage
**Quand** les données de portefeuille sont chargées
**Alors** trois compteurs s'affichent : "Relances urgentes", "Nouveaux leads", "Clients à risque"
**Et** chaque compteur affiche le nombre correspondant avec un libellé contextuel

**Étant donné** que le vendeur clique sur un compteur
**Quand** le clic est effectué
**Alors** la liste filtrée correspondante s'affiche
**Et** "Clients à risque" contient les clients dont le score est sous le seuil critique configuré

**JIRA :** SCRUM-17

---

### Story 2.5 — Liste des leads prioritaires classés par score

En tant que vendeur,
Je veux voir mes leads classés par score de potentiel décroissant,
Afin de traiter en priorité les prospects les plus chauds.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur la Homepage
**Quand** la liste des leads est affichée
**Alors** les leads sont triés par score décroissant
**Et** chaque ligne affiche : Client, Véhicule, Source, Statut (Nouveau/Qualifié/À Relancer), Délai de traitement recommandé
**Et** un lien "Voir tous mes leads" est présent

**Étant donné** que le vendeur clique sur une ligne de lead
**Quand** le clic est effectué
**Alors** la Fiche Client correspondante s'ouvre

**JIRA :** SCRUM-18

---

### Story 2.6 — Liste des affaires en cours

En tant que vendeur,
Je veux voir mes affaires en cours avec leur statut et prochaine étape,
Afin de suivre l'avancement de mon pipeline commercial.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur la Homepage
**Quand** la liste des affaires est affichée
**Alors** chaque ligne affiche : Client, Véhicule, Statut (étape pipeline), Prochaine étape, Date cible
**Et** un lien "Toutes mes affaires" est présent

**Étant donné** que le vendeur clique sur une ligne d'affaire
**Quand** le clic est effectué
**Alors** la Fiche Client correspondante s'ouvre

**JIRA :** SCRUM-19

---

## Epic 3 — Fiche Client 360°

Le vendeur accède à la vue complète d'un client avec toutes les informations nécessaires pour préparer et déclencher une action commerciale.

### Story 3.1 — Chargement des données client depuis les APIs Renault (TTL 15min)

En tant que vendeur,
Je veux que les données d'un client soient fraîches à chaque ouverture de fiche,
Afin de baser mes actions sur des informations à jour.

**Critères d'acceptance :**

**Étant donné** que le vendeur ouvre une Fiche Client
**Quand** les données ont plus de 15 minutes (TTL expiré)
**Alors** les données sont récupérées depuis les APIs Renault via Apigee X avec Resilience4j (retry x2, circuit breaker, timeout)
**Et** stockées/mises à jour dans PostgreSQL OUDE

**Étant donné** que l'API Renault échoue ou dépasse le timeout
**Quand** les données en cache existent
**Alors** les données en cache sont servies avec un indicateur "données potentiellement datées"
**Et** la fiche n'est jamais bloquée par une erreur API externe

**JIRA :** SCRUM-20

---

### Story 3.2 — Header client : identité, score de potentiel et infobulle

En tant que vendeur,
Je veux voir l'identité complète du client et son score de potentiel en haut de la fiche,
Afin d'avoir le contexte essentiel avant de parcourir les détails.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur une Fiche Client
**Quand** la fiche est chargée
**Alors** le header affiche : initiales (avatar), nom, téléphone, adresse, email, date début de relation, nb véhicules achetés, vendeur référent (nom + photo)
**Et** le score de potentiel est affiché en % (0–100) avec libellé qualitatif (Froid/Tiède/Chaud/Très chaud)
**Et** une infobulle détaille les facteurs ayant contribué au score

**JIRA :** SCRUM-21

---

### Story 3.3 — Véhicule actuel, financement en cours et historique véhicules

En tant que vendeur,
Je veux voir le véhicule actuel du client avec son financement et son historique de véhicules,
Afin d'identifier les opportunités de renouvellement ou d'upgrade.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur la Fiche Client
**Quand** la section véhicule est affichée
**Alors** le véhicule actuel est affiché : modèle, année, kilométrage, type de contrat
**Et** les services actifs liés au véhicule sont listés
**Et** le financement actif est affiché : type, mensualité, durée restante/totale, date d'échéance, apport, valeur résiduelle, km contractuel
**Et** l'onglet "Historique" affiche les véhicules précédents
**Et** toutes les données sont en lecture seule

**JIRA :** SCRUM-22

---

### Story 3.4 — Consentements marketing, pipeline opportunités et signaux digitaux

En tant que vendeur,
Je veux voir les consentements du client, son pipeline d'opportunités et ses signaux digitaux,
Afin de qualifier le bon canal de contact et comprendre son engagement digital.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur la Fiche Client
**Quand** ces sections sont affichées
**Alors** les consentements actifs/inactifs sont affichés par canal (Téléphone, Email, Postal) en lecture seule
**Et** le pipeline visuel affiche les étapes (Lead→RDV→Test Drive→Opportunité→Offre→Commande→Livraison) avec l'étape courante mise en évidence
**Et** les dernières opportunités sont listées avec date et source
**Et** les signaux digitaux sont affichés : dernière connexion Renault.fr, MyRenault, configurations enregistrées, statut fidélité

**JIRA :** SCRUM-23

---

### Story 3.5 — Actions de contact : appel et email avec log automatique

En tant que vendeur,
Je veux pouvoir appeler ou envoyer un email à un client en un clic depuis la Fiche Client,
Afin que chaque interaction soit déclenchée rapidement et tracée automatiquement.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur une Fiche Client
**Quand** il clique "Appeler"
**Alors** le composeur natif s'ouvre avec le numéro du client pré-renseigné
**Et** une entrée est automatiquement créée dans l'historique des interactions via l'API Renault

**Étant donné** que le vendeur clique "Envoyer un mail"
**Quand** le clic est effectué
**Alors** le client mail natif s'ouvre avec l'adresse du client pré-renseignée
**Et** une entrée est créée dans l'historique des interactions via l'API Renault

**JIRA :** SCRUM-24

---

### Story 3.6 — Historique des interactions client

En tant que vendeur,
Je veux voir l'historique complet des interactions passées avec le client,
Afin de préparer ma prochaine action avec tout le contexte.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur une Fiche Client
**Quand** la section historique est affichée
**Alors** les interactions sont listées chronologiquement : date, type, résumé
**Et** les interactions proviennent de toutes les sources Renault (pas seulement OUDE)
**Et** un lien "Voir toutes les interactions" est présent vers l'historique complet

**JIRA :** SCRUM-25

---

## Epic 4 — Intelligence artificielle : résumé client & recommandation d'action

Les vendeurs bénéficient de deux enrichissements IA à l'ouverture de la Fiche Client via Vertex AI Gemini.

### Story 4.1 — Intégration Vertex AI Gemini : client SDK, timeout et fallback

En tant que développeur,
Je veux que l'appel à Vertex AI Gemini soit encapsulé avec timeout et fallback gracieux,
Afin que les fonctionnalités IA ne bloquent jamais l'affichage de la Fiche Client.

**Critères d'acceptance :**

**Étant donné** qu'un appel Vertex AI est déclenché
**Quand** le timeout de 4 secondes est dépassé ou que Gemini renvoie une erreur
**Alors** le système retourne un fallback "suggestion indisponible" sans bloquer le reste de la fiche
**Et** l'appel est logué en ERROR dans GCP Cloud Logging avec modèle utilisé, tokens input/output et durée

**Étant donné** qu'un appel Vertex AI réussit
**Quand** le résultat est généré
**Alors** il est mis en cache pour la durée de la session (pas de re-génération à chaque navigation)

**JIRA :** SCRUM-26

---

### Story 4.2 — Résumé IA du profil client (Vertex AI Gemini)

En tant que vendeur,
Je veux voir un résumé IA de 1-2 phrases du profil client à l'ouverture de la fiche,
Afin de saisir instantanément le contexte client sans lire tous les détails.

**Critères d'acceptance :**

**Étant donné** que la Fiche Client est ouverte
**Quand** les données client sont disponibles
**Alors** un résumé de 1-2 phrases est généré via Vertex AI Gemini et affiché dans le header client
**Et** le résumé est mis en cache pour la session (pas de re-génération si le vendeur revient sur la fiche)
**Et** si Vertex AI échoue ou timeout, le bloc affiche "Résumé indisponible" sans bloquer la fiche

**JIRA :** SCRUM-27

---

### Story 4.3 — Recommandation d'action IA avec CTAs contextuels

En tant que vendeur,
Je veux recevoir une recommandation d'action IA avec des CTAs contextuels sur la Fiche Client,
Afin de savoir exactement quoi faire pour maximiser mes chances de conclure avec ce client.

**Critères d'acceptance :**

**Étant donné** que la Fiche Client est ouverte
**Quand** les données client et le score sont disponibles
**Alors** une recommandation est générée via Vertex AI Gemini : texte libre + CTAs contextuels (appel, email, essai, etc.)
**Et** les CTAs de la recommandation déclenchent les actions de contact correspondantes (Story 3.5)
**Et** une seule recommandation par ouverture de fiche (pas de bouton "Régénérer" en Lot 1)
**Et** si Vertex AI échoue, le bloc affiche "Recommandation indisponible"

**JIRA :** SCRUM-28

---

## Epic 5 — Intégration widget AskRG

Le widget AskRG (fourni par Renault) est intégré en barre de recherche sur la Homepage et la Fiche Client.

### Story 5.1 — Intégration widget AskRG sur Homepage et Fiche Client

En tant que vendeur,
Je veux accéder au widget AskRG depuis la barre de recherche de chaque écran,
Afin de bénéficier des fonctionnalités AskRG sans quitter OUDE.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur la Homepage ou la Fiche Client
**Quand** la page est chargée
**Alors** le widget AskRG est intégré en barre de recherche en haut de l'écran
**Et** OUDE ne développe pas le widget — uniquement l'intégration du code fourni par Renault

**Étant donné** que le vendeur est sur une Fiche Client
**Quand** le widget AskRG est initialisé
**Alors** l'identifiant client est transmis au widget (si l'API widget le supporte)

**JIRA :** SCRUM-29

---

## Epic 6 — Module essais véhicules

Le vendeur peut réserver un essai véhicule pour un client depuis la Fiche Client.

### Story 6.1 — Modèle de données et seed de la flotte essais par concession

En tant que développeur,
Je veux que les véhicules disponibles à l'essai soient seedés en base OUDE pour Lot 1,
Afin que le module essais soit fonctionnel sans admin UI.

**Critères d'acceptance :**

**Étant donné** que l'application démarre en Lot 1
**Quand** la base de données est initialisée
**Alors** les véhicules disponibles à l'essai sont seedés directement dans PostgreSQL OUDE
**Et** chaque véhicule est associé à une concession
**Et** les données essais (véhicules + réservations) sont dans des tables OUDE distinctes, sans lien avec les APIs Renault

**JIRA :** SCRUM-30

---

### Story 6.2 — Affichage des créneaux disponibles par véhicule

En tant que vendeur,
Je veux voir les créneaux disponibles pour chaque véhicule d'essai,
Afin de choisir le bon véhicule et le bon moment pour l'essai d'un client.

**Critères d'acceptance :**

**Étant donné** que le vendeur accède au module essais depuis une Fiche Client
**Quand** il sélectionne un véhicule
**Alors** les créneaux d'1h disponibles sont affichés sur un calendrier glissant
**Et** un créneau est marqué indisponible dès qu'une réservation existe pour ce créneau

**JIRA :** SCRUM-31

---

### Story 6.3 — Flux de réservation d'essai depuis la Fiche Client

En tant que vendeur,
Je veux réserver un essai véhicule pour un client en quelques clics depuis la Fiche Client,
Afin de planifier l'essai sans sortir du contexte client.

**Critères d'acceptance :**

**Étant donné** que le vendeur est sur une Fiche Client
**Quand** il initie une réservation d'essai
**Alors** le flux est : sélection véhicule → sélection créneau → confirmation → enregistrement en base OUDE
**Et** le créneau réservé devient immédiatement indisponible pour les autres réservations
**Et** le flux est accessible uniquement depuis la Fiche Client (pas depuis la Homepage)

**JIRA :** SCRUM-32

---

### Story 6.4 — Confirmation email simulée (stub Lot 1)

En tant que vendeur,
Je veux que le client reçoive une confirmation de son essai,
Afin qu'il soit informé de la réservation. (Lot 1 : simulation uniquement)

**Critères d'acceptance :**

**Étant donné** qu'une réservation d'essai est confirmée
**Quand** la réservation est enregistrée en base
**Alors** l'envoi d'email est simulé : un log serveur est écrit (niveau INFO, avec détails de la réservation)
**Et** un message de succès est affiché à l'UI ("Confirmation envoyée au client")
**Et** aucun email réel n'est envoyé (pas d'intégration SMTP en Lot 1)

**JIRA :** SCRUM-33
