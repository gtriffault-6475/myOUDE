---
status: final
project: OUDE
updated: 2026-06-30
design-ref: DESIGN.md
sources:
  - planning-artifacts/prds/prd-OUDE-2026-06-26/prd.md
  - planning-artifacts/architecture/architecture-OUDE-2026-06-26/ARCHITECTURE-SPINE.md
  - imports/screenshot-homepage.png
  - imports/screenshot-fiche-client.png
---

# Foundation

**Form-factor :** Desktop-first (vendeur au bureau) + Mobile-responsive (tablette/mobile en concession).

**UI system :** Angular Material 18 étendu avec tokens DESIGN.md. Angular Material fournit la base comportementale (ripple, focus, a11y) ; les tokens DESIGN.md overrident les couleurs, typographie et rayons.

**Référence visuelle :** `DESIGN.md` est le contrat visuel unique. Toute décision de couleur, police ou espacement non couverte ici se résout dans DESIGN.md.

**Surfaces :**
| Surface | Route | Description |
|---------|-------|-------------|
| Homepage | `/home` | Vue portefeuille du vendeur |
| Fiche Client | `/clients/:id` | Vue 360° d'un client |
| (futur) Liste complète leads | `/leads` | Hors périmètre Lot 1 — lien uniquement |
| (futur) Liste complète affaires | `/affaires` | Hors périmètre Lot 1 — lien uniquement |

---

# Information Architecture

## Navigation principale — Sidebar (desktop) / Bottom bar (mobile)

La sidebar est icon-only (`64px`) sur desktop. L'icône active porte un indicateur left-border bleu Renault. Le label apparaît en tooltip au hover. En bas : avatar vendeur + nom + rôle.

**Icônes de navigation (ordre maquette) :**
1. 🏠 Home (Homepage)
2. 📅 Agenda *(hors scope Lot 1 — icône visible, état disabled avec tooltip "Bientôt disponible")*
3. ✅ Tâches *(hors scope Lot 1 — disabled)*
4. 👤 Fiche client *(actif uniquement quand une fiche est ouverte)*
5. 💡 *(réservé)*
6. 👥 *(réservé)*
7. 🚗 *(réservé)*
8. 🗺️ *(réservé)*
9. 📊 *(réservé)*
10. 📞 *(réservé)*
11. ⚡ AskRG *(en bas, icône distincte bleue — action globale)*

**Mobile :** bottom navigation bar avec 4 items : Home, Fiche Client, AskRG, Profil.

## Hiérarchie des pages

```
App Shell (sidebar + header global)
├── Homepage
│   ├── Header de bienvenue (prénom + date + résumé IA du jour)
│   ├── Bandeau AskRG
│   ├── Priorités du jour (3 compteurs)
│   ├── Leads prioritaires (tableau)
│   └── Affaires en cours (tableau)
└── Fiche Client
    ├── Bandeau AskRG + CTAs (Appeler / Envoyer un mail)
    ├── Header client (identité + score + consentements + action suggérée)
    ├── Bloc véhicule (onglets : Véhicule actuel / Historique / RDV APV)
    ├── Financement en cours
    ├── Opportunités commerciales (pipeline + liste)
    ├── Historique des interactions
    ├── Historique campagne marketing *(hors scope Lot 1 — affiché flou)*
    ├── Signaux digitaux
    └── Voice of Customer *(hors scope Lot 1 — affiché flou)*
```

---

# Voice and Tone

**Voix :** directe, professionnelle, encourageante. S'adresser au vendeur par son prénom.

**Règles microcopy :**
- Salutations : "Bonjour [Prénom], voici votre journée !" — pas "Tableau de bord"
- Résumés IA : 1-2 phrases max, en italique, ton factuel et synthétique
- Labels de statut : majuscule initiale uniquement ("Nouveau", pas "NOUVEAU")
- Délais : format court "25 min", "1h15" — pas "1 heure et 15 minutes"
- Dates : "19.05.2026" (format court) ou "19 mai 2026" (format lisible) — cohérence par surface
- CTA : verbe d'action + contexte ("Appeler →", "Planifier l'essai", "Créer une nouvelle opportunité")
- Messages d'erreur : "Données temporairement indisponibles — affichage du dernier état connu" (jamais "Erreur 503")
- IA indisponible : "Suggestion indisponible pour le moment" — jamais de message technique

**Champs sensibles (consentements, données financières) :** affichés en lecture seule, sans libellé alarmiste. Le rouge est réservé au consentement `Postal: ●` désactivé.

---

# Component Patterns

## AskRG Bar

- Pleine largeur, positionnée en bandeau sous le header de page
- Deux zones : label "AskRG — Que souhaitez-vous savoir ?" à gauche (texte orange, non interactif), champ de recherche à droite avec icône loupe et placeholder contextuel
- Placeholder change selon la surface : "Quelles priorités ce matin ?" (Homepage) / "Aide moi à préparer l'appel" (Fiche Client)
- Bouton ✕ pour fermer la suggestion active
- Comportement : délégué entièrement au widget AskRG fourni par Renault (Story 5.1)

## Compteurs Priorités du Jour

- 3 cartes côte à côte (`flex: 1`)
- Chaque carte : label titre (bold 15px), grand chiffre (36px bold), sous-label contextuel (12px, couleur sémantique)
- Carte "Clients à risque" : border `1px solid {colors.semantic.danger}` si valeur > 0
- Clic sur carte → scroll vers / filtre la liste correspondante (même page)

## Tableau Leads / Affaires

- En-têtes de colonnes : label uppercase 11px, couleur muted, tri désactivé Lot 1
- Lignes : hover `background: #F5FBF5`, height `52px`
- Badge statut : pill colorée (voir DESIGN.md `colors.status-badges`)
- Délai de traitement : icône horloge + texte coloré (vert < 30min, orange 30-60min, rouge > 1h)
- Lien "Voir tous mes leads" / "Toutes mes affaires" : en haut à droite de chaque section, bleu souligné

## Score de Potentiel

- Grand chiffre + "%" (`48px bold`)
- Couleur selon seuil : vert `#00C853` si ≥ 75, orange `#FF9800` si 50-74, gris `#90A4AE` si < 50
- Libellé qualitatif en dessous : "Très chaud" / "Chaud" / "Tiède" / "Froid"
- Infobulle au hover : liste les 3 principaux facteurs contributifs au score
- Chevron ">" → expandable (Lot 1 : tooltip uniquement)

## Résumé IA (client)

- Texte italique, couleur `{colors.text.ai-italic}`
- Border-left `3px solid {colors.brand.renault-green}`
- 1-2 phrases max
- État de chargement : skeleton loader 2 lignes pendant l'appel Vertex AI (max 4s)
- Fallback : texte "Résumé indisponible pour le moment" en muted italic, sans border-left

## Carte "Prochaine Action Suggérée"

- Fond rose lavande `{colors.action-card.bg}`
- Titre bold "Prochaine action suggérée"
- Corps : texte libre IA (2-4 phrases)
- CTAs : 1-2 liens bleus soulignés ("Planifier l'essai", "Préparer une simulation")
- Ces CTAs déclenchent les actions de contact (appel, email) ou la navigation vers le module essais
- Fallback : carte masquée si Vertex AI indisponible

## Pipeline Opportunités Commerciales

- Stepper horizontal : Lead identifié → RDV planifié → Test Drive → Opportunité → Offre → Commande → Livraison
- Étape active : cercle bleu Renault, bold
- Étapes passées : cercle vert, texte muted
- Étapes futures : cercle gris
- En dessous : liste "Dernières opportunités" avec date relative (-6 mois, -3 mois)
- Bouton "Créer une nouvelle opportunité" : primaire bleu

## Consentements Marketing

- 3 lignes : Téléphone / Email / Postal
- Indicateur circulaire : `● vert` = actif, `● rouge` = inactif
- Texte en lecture seule, non cliquable
- Chevron ">" → tooltip d'information (Lot 1)

## Bloc Véhicule

- Onglets : "Véhicule actuel" / "Historique" / "RDV APV"
- Image véhicule à gauche (illustration, pas photo réelle)
- Infos : Modèle + Année en titre, Kilométrage avec icône compteur, Type de contrat (badge pill "Locataire")
- Services actifs : "Contrat d'entretien ●" / "Extension de garantie ●" + "Voir détail →"

---

# State Patterns

## États de chargement

| Surface | Comportement |
|---------|-------------|
| Homepage (données portefeuille) | Skeleton loaders pour chaque section — jamais de spinner plein écran |
| Fiche Client (données) | Skeleton loaders par bloc — le header charge en premier, les blocs secondaires suivent |
| Score (recalcul) | Shimmer animation sur le chiffre pendant calcul |
| Résumé IA | Skeleton 2 lignes, max 4s puis fallback texte |
| Recommandation IA | Skeleton 3 lignes, max 4s puis carte masquée |

## État "données datées"

Quand le cache est servi après échec API Renault :
- Banner discret en haut de la section concernée : `ℹ️ Données du [date/heure] — actualisation indisponible`
- Couleur fond banner : `#FFF8E1` (jaune très clair), texte `#E65100`
- La section reste entièrement lisible et interactive

## États vides

- Leads prioritaires vide : "Aucun lead actif — votre portefeuille est à jour ✓" (vert, icône check)
- Affaires en cours vide : "Aucune affaire en cours"
- Historique interactions vide : "Aucune interaction enregistrée"

## Navigation vers Fiche Client

- Depuis tableau leads : clic sur la ligne → navigation Angular Router vers `/clients/:id`
- Depuis tableau affaires : idem
- Depuis sidebar : icône "Fiche client" active uniquement quand une fiche a été ouverte dans la session ; sinon désactivée (tooltip "Ouvrez une fiche depuis la Homepage")

---

# Interaction Primitives

## Clics et navigation

- Lignes de tableau : curseur `pointer`, hover `background: #F5FBF5`, clic → navigation
- Compteurs : clic → scroll/filtre (même page, Lot 1)
- Chevrons ">" dans les blocs : expansion locale ou tooltip (Lot 1)
- Liens "Voir tous" : navigation vers page liste (hors scope Lot 1 → état coming soon)

## Actions de contact (Fiche Client)

- Bouton "📞 Appeler →" : `window.location.href = 'tel:NUMERO'` → ouvre le composeur natif
- Bouton "✉️ Envoyer un mail →" : `window.location.href = 'mailto:EMAIL'` → ouvre le client mail
- Log automatique déclenché par le back-end lors de ces actions (FR-3.7.4)

## Formulaire Réservation Essai (F-5)

Flow en 3 étapes dans une modale ou une page dédiée :
1. **Sélection véhicule** : liste cards (photo + modèle + disponibilité)
2. **Sélection créneau** : calendrier glissant 7 jours, créneaux 1h, grisés si réservés
3. **Confirmation** : récap + bouton "Confirmer la réservation"

Après confirmation : toast success "Réservation enregistrée — confirmation envoyée au client ✓" (3s auto-dismiss).

## Responsive — Mobile

| Composant | Desktop | Mobile |
|-----------|---------|--------|
| Sidebar | Fixed left 64px | Bottom navigation bar |
| Compteurs | 3 colonnes | 1 colonne stack |
| Tableau leads | 5 colonnes | Cards verticales (Client + Statut + Délai) |
| Tableau affaires | 5 colonnes | Cards verticales |
| Header client | Grid 4 colonnes | Stack vertical |
| Pipeline stepper | Horizontal | Vertical ou scroll horizontal |
| Bloc véhicule | Image + infos côte à côte | Image au-dessus, infos en dessous |

---

# Accessibility Floor

- **Contraste minimum :** 4.5:1 pour texte normal, 3:1 pour texte large et icônes — vérifier les badges de statut
- **Focus visible :** ring `2px solid {colors.border.focus}` sur tous les éléments interactifs (ne jamais `outline: none` sans alternative)
- **Navigation clavier :** toutes les actions disponibles au clavier (tab order logique : sidebar → header → contenu principal)
- **ARIA labels :** icônes de navigation avec `aria-label`, compteurs avec `aria-describedby` pointant vers le sous-label
- **Score :** `<span aria-label="Score de potentiel : 89%, Très chaud">89%</span>`
- **Badges statut :** couleur + texte (jamais couleur seule)
- **Données en lecture seule :** `aria-readonly="true"` sur les champs de consentement
- **Skeleton loaders :** `aria-busy="true"` sur le conteneur pendant chargement
- **Langue :** `lang="fr"` sur `<html>`

---

# Key Flows

## Flow 1 — Natasha commence sa journée

*Natasha Martin, vendeuse Renault Boulogne, arrive au bureau à 8h30. Elle ouvre OUDE sur son desktop.*

1. Le SSO Renault authentifie Natasha automatiquement (token déjà actif).
2. La Homepage charge. Le header affiche "Bonjour Natasha, voici votre journée !" avec la date du jour.
3. Le résumé dynamique indique "Vous avez 2 leads chauds à traiter, 2 essais programmés et 1 renouvellement à sécuriser avant la fin de journée."
4. Natasha voit les compteurs : **3** relances urgentes, **5** nouveaux leads, **1** client à risque (en rouge).
5. Elle scanne la liste des leads prioritaires. M. Dupont est en tête avec le score le plus élevé.
6. **Climax :** Elle clique sur la ligne de M. Dupont → la Fiche Client s'ouvre.

## Flow 2 — Natasha prépare un appel avec Paul Durand

*Paul Durand a un renouvellement imminent. Natasha a ouvert sa Fiche Client.*

1. La Fiche Client charge. Le header affiche l'identité de Paul, son score **89% Très chaud** en vert.
2. Le résumé IA s'affiche en italique : *"Client fidèle avec un renouvellement imminent prévu dans 3 mois. Paul Durand a configuré deux versions de Clio VI sur Renault.fr hier soir."*
3. La carte "Prochaine action suggérée" recommande : "Proposer un essai de la Nouvelle Clio vendredi à 14h30 et valider l'intérêt pour une offre de renouvellement."
4. Natasha voit que le contrat LLD se termine dans **3 mois / 48**, mensualité **289 €/mois**.
5. Elle vérifie les consentements : Téléphone ✓, Email ✓, Postal ✗.
6. **Climax :** Elle clique "📞 Appeler →". Le composeur s'ouvre avec le numéro pré-renseigné. Une interaction est automatiquement loguée.

## Flow 3 — Natasha réserve un essai (F-5)

*Suite de l'appel : Paul est intéressé par un essai. Natasha reste sur la Fiche Client.*

1. Natasha clique sur le CTA "Planifier l'essai" dans la carte "Prochaine action suggérée".
2. Une modale s'ouvre : sélection du véhicule disponible à l'essai (liste de 2-3 modèles seedés).
3. Elle sélectionne "Nouvelle Clio E-Tech" et choisit un créneau vendredi 14h00-15h00.
4. Écran de confirmation : "Renault Clio E-Tech — Vendredi 04 juillet 2026, 14h00"
5. **Climax :** Elle clique "Confirmer la réservation". Toast success : *"Réservation enregistrée — confirmation envoyée à paul.durand@gmail.com ✓"* (email simulé Lot 1).

## Flow 4 — Natasha sur mobile en concession

*Natasha est en salle d'exposition avec un client. Elle consulte OUDE sur son téléphone.*

1. La bottom navigation est visible. Elle tape sur l'icône Fiche Client.
2. La fiche du client actuel s'affiche — le header est compacté en stack vertical.
3. Elle vérifie rapidement le score (89%, Très chaud) et le contrat de financement.
4. Elle tape "📞 Appeler" pour simuler un transfert vers son collègue.
5. **Climax :** L'affichage mobile reste lisible et les actions principales sont accessibles en un tap — pas de défilement horizontal requis.

---

# Hors scope Lot 1 — traitement visuel

Les sections présentes dans les maquettes mais hors périmètre Lot 1 sont **affichées avec un masque flou** (comme dans les maquettes) + tooltip au hover "Disponible prochainement" :

- Agenda (Homepage)
- Objectifs commerciaux (Homepage)
- Tunnel des ventes complet (Homepage)
- Historique campagne marketing (Fiche Client)
- Voice of Customer (Fiche Client)

Cela permet de présenter une vision complète du produit à Renault sans promettre de fonctionnalités non livrées.
