# BETPRO - Le Syndicat d'Arbitrage Temporel 🚀

Une application hybride (Android + Node.js) conçue pour exploiter les failles temporelles entre les bookmakers mondiaux et locaux.

## Fonctionnement "Dingue"
1. **Radar Temporel (Android)** : L'application Android interroge *The Odds API* (tier gratuit) pour détecter les chutes de cotes massives ("dropping odds").
2. **Notification & Exécution** : Dès qu'une faille est trouvée, l'utilisateur est notifié et l'ordre est envoyé immédiatement au bot Node.js.
3. **Le Bot (Node.js)** : Un script Playwright se connecte sur Betika.cd ou Betpawa.cd et place le pari avant que le bookmaker local n'ait eu le temps de mettre à jour sa cote.

## L'Interface (UI)
L'application Android est développée en Jetpack Compose avec un thème "Premium Dark Mode" (Fond : #121212, #1E1E1E) et des accents Vert Néon (#00E676). Elle intègre une sécurité biométrique (empreinte digitale) pour déverrouiller le "Radar".

## Installation

### Backend (Node.js)
\`\`\`bash
cd backend
npm install
npm run build
node dist/index.js
\`\`\`
*Variables d'environnement requises* : \`.env\` avec \`BACKEND_API_KEY\`, etc.

### Android
Ouvrez le dossier \`android\` dans Android Studio.
Créez un fichier \`local.properties\` à la racine de \`android/\` avec :
\`\`\`properties
ODDS_API_KEY=votre_cle_gratuite
BACKEND_URL=http://votre-serveur:3000/api/bot/place-bet
BACKEND_TOKEN=votre_token_secret
\`\`\`

Compilez et installez sur votre appareil. Déverrouillez avec votre empreinte. Fixez le montant, et activez le radar !
