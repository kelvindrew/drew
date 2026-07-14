# 🎨 Smart Media Transfer AI - Guide de Style et Spécifications UI/UX
*(Document destiné à l'équipe UI/UX "Nano Banana" et aux développeurs frontend)*

## 1. Fondations Visuelles
Le thème est sombre, profond, premium, et utilise la transparence pour s'intégrer parfaitement à l'esthétique Windows (Mica/Acrylic).

| Élément | Valeur Hexadécimale / Description |
|---|---|
| Fond Principal | `#0F111A` (Midnight Black) |
| Conteneurs (Glassmorphism)| `#FFFFFF` avec 3% à 5% d'opacité + Effet de floutage d'arrière-plan (Blur) |
| Bordures | Très fines, `#FFFFFF` avec 10% à 15% d'opacité |
| Texte Primaire | `#FFFFFF` (Blanc pur) |
| Texte Secondaire | `#8C90A6` (Gris moyen) |
| Ombres (Neumorphism) | Subtiles, portées vers le bas pour les éléments en relief, vers le haut pour les boutons pressés. |

## 2. Palette de Couleurs
Les couleurs sont vibrantes, utilisées pour les états, les actions et la visualisation de données.

| Couleur | Rôle | Code Hex | Exemple d'utilisation |
|---|---|---|---|
| Bleu Électrique | Primaire / Action | `#0077FF` | Bouton principal, liens, jauges de progression |
| Violet Profond | Secondaire / IA | `#7F00FF` | Éléments de l'IA, icônes spécifiques |
| Turquoise | Accent | `#00E5FF` | Point culminant de l'animation de transfert |
| Vert | Succès | `#00E676` | Indicateurs "Terminé", disques sains, confirmation |
| Orange | Attention | `#FF9100` | État "S.M.A.R.T : Attention", avertissements |
| Rouce | Erreur | `#FF1744` | État "Erreur", disques critiques |

## 3. Motion Design & Micro-interactions (Lottie)
Toute transition ou interaction doit être fluide et donner une impression de haute technologie.
- **Courbes d'animation :** Ease-in-out (doux au démarrage, rapide au centre, doux à la fin).
- **Durée :** Rapide (entre 200ms et 350ms) pour ne pas bloquer l'utilisateur.
- **Particules :** Utilisation de Lottie pour simuler la "matière" numérique des fichiers en transit.

---

## 🖥️ Maquettes d'Interface (Screens & States)

### Écran 1 : Démarrage (Splash Screen)
Un écran simple, centré, pour masquer le chargement du moteur IA.
- **Arrière-plan :** Noir profond `#0F111A`.
- **Centre :**
  - Le logo du projet (stylisé en "SMT AI").
  - Un anneau lumineux (effet néon) pulsant lentement.
  - Sous l'anneau, une animation Lottie de particules représentant l'initialisation de la base de données.
- **Transition :** L'écran s'estompe en douceur vers le Dashboard lorsque le moteur est prêt.

### Écran 2 : Tableau de Bord (Dashboard Principal)
Le centre de contrôle. Il doit être lisible et donner l'état global du système en un coup d'œil.
- **Structure :** Barre de navigation latérale (gauche) + Contenu principal.
- **Zone Principale (Grille) :**
  - **Carte 1 (Jauges Circulaires) :** Un ensemble de trois jauges animées (Lottie) affichant la capacité (Totale / Utilisée / Libre) de l'ordinateur. Le code couleur change si l'espace devient critique.
  - **Carte 2 (Indicateurs) :** Cartes avec des nombres animés : "Transferts en attente", "Fichiers traités ce mois", "Appareils connectés".
  - **Carte 3 (Suggestion IA) :** Une carte premium (légèrement plus lumineuse) avec une icône IA violette. Texte : "Vos disques sont pleins à 85%. Souhaitez-vous archiver automatiquement les fichiers > 1Go ?". Boutons : "Plus tard" (Gris) / "Oui, archiver" (Bleu Électrique).
  - **Carte 4 (Derniers transferts) :** Une liste compacte des 5 derniers fichiers reçus.

### Écran 3 : Transfert Immersif
Le cœur de l'application. Une expérience visuelle en temps réel.
- **Zone Principale :** Une visualisation 2.5D isométrique (téléphone mobile vers PC).
- **État A : En attente :** Le QR Code s'affiche, ondes Lottie autour du PC.
- **État B : En cours :** Particules de lumière (Bleu et Turquoise) volant du téléphone au PC. La densité et vitesse dépendent des Mo/s réels.

### Écran 4 : Gestionnaire de Stockage & Disques (Storage Page)
- **Structure :** Vue en grille. Chaque disque est une carte "Glassmorphism" interactive.
- **Détails Carte :** Icône (SSD, USB...), Nom, Lettre, Format. Barre de progression colorée.
- **Indicateur S.M.A.R.T :** Vert (Bon, ex: 35°C), Orange (Attention, ex: 52°C), Rouge (Critique, ex: 65°C).
- **Section Archivage :** Bas de page affichant la file d'attente indépendante "PC vers Externe".

### Écran 5 : Intelligence & Règles (AI & Rules Page)
Le cerveau du système automatisé.
- **Liste des règles :** P1, P2, P3. Résumé (ex: "Si Taille > 500Mo, Déplacer"). Toggle switch.
- **Bouton d'ajout :** Bouton flottant Bleu électrique.
- **Pop-up de Confirmation (Overlay) :** Overlay avec floutage fort (Blur). Affiche le Fichier Source, Destination, et la Règle déclenchée. Boutons : Autoriser, Refuser, Modifier.

### Écran 6 : Composants Transverses
- **Icône System Tray :** Logo SMT AI discret près de l'horloge.
- **Notifications Toast Windows :** Exemples : "Transfert terminé", "Disque déconnecté".
