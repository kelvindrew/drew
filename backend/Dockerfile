# Utiliser l'image officielle Playwright qui contient déjà tous les navigateurs et dépendances OS nécessaires
FROM node:22-bookworm

# Créer et définir le dossier de travail
WORKDIR /usr/src/app

# Copier les fichiers de package
COPY package*.json ./

# Installer les dépendances Node.js (express, body-parser, tesseract.js, playwright)
RUN npm ci

# Installer les dépendances OS et les navigateurs Playwright
RUN npx playwright install --with-deps chromium

# Copier le reste du code source
COPY . .

# Exposer le port du serveur Express
EXPOSE 3000

# Démarrer le serveur
CMD [ "npm", "start" ]
