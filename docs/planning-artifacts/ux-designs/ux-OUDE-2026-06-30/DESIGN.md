---
status: final
project: OUDE
updated: 2026-06-30
sources:
  - planning-artifacts/prds/prd-OUDE-2026-06-26/prd.md
  - planning-artifacts/architecture/architecture-OUDE-2026-06-26/ARCHITECTURE-SPINE.md
  - imports/screenshot-homepage.png
  - imports/screenshot-fiche-client.png

colors:
  background:
    page: "#EFF7EE"          # vert sauge très clair — fond général de l'app
    card: "#FFFFFF"          # blanc — cartes et panneaux
    sidebar: "#FFFFFF"       # blanc — barre de navigation latérale
    askrg: "#FFF5EC"         # crème pêche — bandeau AskRG

  brand:
    renault-blue: "#1A3EAD"  # bleu Renault — boutons CTA primaires, liens actifs
    renault-dark: "#1A1F2E"  # quasi-noir — titres, textes forts
    renault-green: "#00BF6F" # vert Renault — logo, score élevé, indicateurs positifs

  semantic:
    score-hot: "#00C853"     # vert vif — score Très chaud (75-100)
    score-warm: "#FF9800"    # orange — score Chaud (50-74)
    score-cold: "#90A4AE"    # gris bleu — score Tiède/Froid (0-49)
    success: "#00C853"
    warning: "#FF9800"
    danger: "#F44336"
    info: "#1A3EAD"

  status-badges:
    nouveau: { bg: "#E3F2FD", text: "#1565C0" }
    qualifie: { bg: "#E8F5E9", text: "#2E7D32" }
    a-relancer: { bg: "#FFF3E0", text: "#E65100" }
    test-drive: { bg: "#EDE7F6", text: "#4527A0" }
    offre: { bg: "#FCE4EC", text: "#880E4F" }
    relance: { bg: "#F3E5F5", text: "#6A1B9A" }

  action-card:
    bg: "#F3E5F5"            # rose lavande — carte "Prochaine action suggérée"
    text: "#4A148C"

  text:
    primary: "#1A1F2E"       # titres et libellés forts
    secondary: "#5F6B7A"     # textes secondaires, méta-données
    muted: "#9EAAB4"         # placeholders, textes désactivés
    link: "#1A3EAD"          # liens cliquables
    ai-italic: "#5F6B7A"     # résumé IA en italique

  border:
    default: "#E0EAE0"       # bord de carte sur fond vert sauge
    focus: "#1A3EAD"         # focus ring

typography:
  font-family: "'Renault', 'Inter', system-ui, sans-serif"
  # Note: utiliser la police Renault si disponible via CDN interne, sinon Inter comme fallback

  scale:
    display:    { size: "32px", weight: "700", line-height: "1.2" }  # "Bonjour Natasha"
    h1:         { size: "24px", weight: "700", line-height: "1.3" }  # titres de page
    h2:         { size: "18px", weight: "600", line-height: "1.4" }  # titres de section
    h3:         { size: "15px", weight: "600", line-height: "1.4" }  # sous-sections
    body:       { size: "14px", weight: "400", line-height: "1.5" }  # texte courant
    small:      { size: "12px", weight: "400", line-height: "1.4" }  # méta, dates
    label:      { size: "11px", weight: "500", line-height: "1.3", transform: "uppercase", spacing: "0.06em" }
    score-num:  { size: "48px", weight: "700", line-height: "1.0" }  # "89%"
    counter:    { size: "36px", weight: "700", line-height: "1.0" }  # "03", "05"

rounded:
  none: "0"
  sm: "6px"
  md: "12px"       # cartes standard
  lg: "16px"       # cartes larges, conteneurs
  xl: "24px"       # conteneur header de page
  full: "9999px"   # badges, pills, avatars circulaires

spacing:
  base: "8px"
  scale: [0, 4, 8, 12, 16, 24, 32, 48, 64]
  card-padding: "20px 24px"
  section-gap: "16px"
  sidebar-width: "64px"         # barre icon-only (collapsed)
  sidebar-width-expanded: "180px"

components:
  sidebar:
    width: "64px"
    bg: "{colors.background.sidebar}"
    border-right: "1px solid {colors.border.default}"
    icon-size: "22px"
    icon-color-default: "{colors.text.secondary}"
    icon-color-active: "{colors.brand.renault-blue}"
    active-indicator: "left border 3px {colors.brand.renault-blue}"
    avatar-size: "36px"
    avatar-position: "bottom"

  card:
    bg: "{colors.background.card}"
    border: "1px solid {colors.border.default}"
    border-radius: "{rounded.md}"
    padding: "{spacing.card-padding}"
    shadow: "0 1px 4px rgba(0,0,0,0.06)"

  button-primary:
    bg: "{colors.brand.renault-blue}"
    text: "#FFFFFF"
    border-radius: "{rounded.full}"
    padding: "10px 20px"
    font-weight: "600"
    font-size: "14px"
    icon-left: true     # icônes téléphone / email sur les CTA Fiche Client

  button-secondary:
    bg: "transparent"
    border: "1px solid {colors.brand.renault-blue}"
    text: "{colors.brand.renault-blue}"
    border-radius: "{rounded.full}"
    padding: "10px 20px"

  badge:
    border-radius: "{rounded.full}"
    padding: "3px 10px"
    font-size: "12px"
    font-weight: "500"
    # couleurs par statut : voir colors.status-badges

  askrg-bar:
    bg: "{colors.background.askrg}"
    border-radius: "{rounded.full}"
    padding: "12px 20px"
    label-color: "#E8732A"    # texte "AskRG" en orange
    placeholder-color: "{colors.text.muted}"
    icon-search: true

  counter-card:
    bg: "{colors.background.card}"
    border: "1px solid {colors.border.default}"
    border-radius: "{rounded.md}"
    # card "Clients à risque" : border-color danger si action requise
    number-color: "{colors.text.primary}"
    danger-number-color: "{colors.semantic.danger}"
    sublabel-color: "{colors.text.secondary}"

  score-gauge:
    number-color: "{colors.semantic.score-hot}"   # vert si Très chaud
    label-below: true    # "Très chaud" sous le chiffre
    tooltip-on-hover: true    # infobulle facteurs du score

  pipeline-stepper:
    orientation: "horizontal"
    step-size: "10px"
    step-inactive: "{colors.text.muted}"
    step-active: "{colors.brand.renault-blue}"
    step-done: "{colors.semantic.success}"
    connector-height: "2px"

  table:
    header-bg: "transparent"
    header-font: "{typography.label}"
    row-border: "1px solid {colors.border.default}"
    row-hover-bg: "#F5FBF5"
    row-height: "52px"
    cell-font: "{typography.body}"

  avatar:
    shape: "circle"
    initials-bg: "{colors.brand.renault-blue}"
    initials-color: "#FFFFFF"
    sizes: { sm: "28px", md: "36px", lg: "48px" }

  ai-summary:
    font-style: "italic"
    color: "{colors.text.ai-italic}"
    border-left: "3px solid {colors.brand.renault-green}"
    padding-left: "12px"

  action-card-suggested:
    bg: "{colors.action-card.bg}"
    border-radius: "{rounded.lg}"
    cta-link-color: "{colors.brand.renault-blue}"
    font-weight-title: "600"
---

# Brand & Style

OUDE est l'outil de travail quotidien des vendeurs Renault. Il doit dégager **confiance, clarté et efficacité** — un outil professionnel qui aide à prendre de meilleures décisions commerciales, pas un tableau de bord qui impressionne en réunion.

Le style est **moderne et data-driven** : chiffres mis en valeur, hiérarchie visuelle forte, couleurs fonctionnelles (chaque couleur porte un sens). L'esthétique s'inspire du design system Renault tout en restant dans les contraintes Angular Material étendu.

**Principes directeurs :**
- La donnée est le héros — les chiffres et les scores sont larges et immédiatement lisibles
- Vert sauge comme fond de page : apaisant, neutre, laisse les cartes blanches respirer
- Couleur = signification : vert = positif/chaud, orange = attention, rouge = urgent, bleu = action
- Économie de couleur : une seule couleur d'accent par section

**Voix de la marque :** directe, professionnelle, encourageante. "Bonjour Natasha, voici votre journée !" — pas "Bienvenue sur le tableau de bord".

# Colors

Fond de page `#EFF7EE` (vert sauge très clair) + cartes blanches `#FFFFFF` = contraste doux sans fatigue visuelle sur une journée de travail complète.

Le bleu Renault `#1A3EAD` est la seule couleur d'action — tous les boutons CTA, liens cliquables et états actifs utilisent cette couleur. Le vert `#00C853` est réservé aux scores élevés et indicateurs de succès.

# Typography

Police principale : **Renault** (font propriétaire) avec fallback **Inter**. Si la police Renault n'est pas disponible via CDN interne Renault, Inter est visuellement très proche et libre de droits.

Les compteurs (`36px bold`) et scores (`48px bold`) sont intentionnellement grands — ils doivent être lisibles d'un coup d'œil sans que le vendeur ait à s'approcher de l'écran.

# Layout & Spacing

**Desktop :** sidebar fixe `64px` à gauche (icônes uniquement, label sur hover/active), contenu principal avec padding `24px`, grid de cartes en colonnes.

**Mobile :** sidebar devient une bottom navigation bar (4-5 icônes essentielles). Les tableaux passent en liste verticale de cartes. Les cartes de compteurs s'empilent sur 1 colonne.

Grille : `8px` base, multiples de 8 pour tous les espacements.

# Elevation & Depth

Pas de shadows agressives. Ombre très légère (`0 1px 4px rgba(0,0,0,0.06)`) sur les cartes pour les détacher du fond vert sauge. Les cartes actives ou au focus utilisent un border `1px solid {colors.brand.renault-blue}` plutôt qu'une ombre.

# Shapes

Coins arrondis `12px` (medium) pour la majorité des cartes. Entièrement arrondis (`border-radius: 9999px`) pour les badges de statut, les pills et les boutons CTA. L'avatar vendeur est circulaire.

# Components

Voir frontmatter YAML pour les specs complètes de chaque composant. Points notables :

**AskRG Bar :** fond crème-pêche `#FFF5EC`, texte "AskRG" en orange `#E8732A`, icône loupe. Positionné en bandeau horizontal en dessous du header de page. Pleine largeur.

**Compteurs priorités :** 3 cartes côte à côte. La carte "Clients à risque" prend une teinte danger (`border-color: #F44336`) quand la valeur est > 0 et que l'action est requise.

**Score potentiel :** grand chiffre coloré selon le seuil (vert si ≥ 75, orange si 50-74, gris si < 50), libellé qualitatif en dessous, infobulle au hover.

**Carte "Prochaine action suggérée" :** fond rose lavande `#F3E5F5`, texte en violet foncé, CTAs en liens bleus soulignés. Positionnée en colonne droite du header client.

# Do's and Don'ts

**✅ Do**
- Utiliser le vert sauge comme fond de page sur toutes les vues
- Mettre les chiffres clés en grande taille, bold
- Un seul bouton CTA primaire (bleu plein) par section — les autres en secondaire (contour)
- Badges de statut toujours en pill avec couleur sémantique cohérente
- Le résumé IA toujours en italique avec border-left vert

**❌ Don't**
- Utiliser le rouge comme couleur de fond de carte (réservé aux borders d'alerte)
- Mélanger des polices : tout en Renault/Inter
- Afficher plus de 3 CTAs primaires sur un même écran
- Utiliser des ombres portées lourdes
- Afficher des données client sans consentement vérifié (NFR-5)
