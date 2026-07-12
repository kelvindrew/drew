# 🚀 Guide d'Exécution et de Développement

Ce guide explique comment ouvrir, compiler et exécuter les deux parties du projet "Smart Media Transfer AI" (Le client Windows et l'application Android).

---

## 🖥️ 1. Logiciel Windows (SmartMediaTransferAIDesktop)

Le projet Windows a été développé en **C# .NET 9** avec **WPF-UI** (pour reproduire le Fluent Design de Windows 11).

### Méthode A : Avec Visual Studio (Recommandé)
Visual Studio (Community, Professional ou Enterprise) est l'outil le plus adapté pour développer des applications de bureau Windows avec interface graphique.

1. **Prérequis :**
   - Assurez-vous d'avoir installé **Visual Studio 2022** (mis à jour à la dernière version).
   - Lors de l'installation de Visual Studio, cochez la charge de travail (workload) : **Développement Desktop .NET** (qui inclut WPF).
   - Installez le **SDK .NET 9** s'il n'est pas déjà inclus.

2. **Ouverture :**
   - Lancez Visual Studio.
   - Cliquez sur **Ouvrir un projet ou une solution**.
   - Naviguez jusqu'au dossier `SmartMediaTransferAIDesktop` et sélectionnez le fichier `SmartMediaTransferAIDesktop.csproj`.

3. **Exécution :**
   - Laissez Visual Studio restaurer automatiquement les paquets NuGet (cela peut prendre quelques secondes en bas de l'écran).
   - En haut de l'écran, assurez-vous que `SmartMediaTransferAIDesktop` est défini comme projet de démarrage.
   - Cliquez sur le bouton vert **Play (SmartMediaTransferAIDesktop)** ou appuyez sur `F5` pour compiler et lancer l'application avec le débogueur attaché.

### Méthode B : Avec Visual Studio Code (VS Code)
Si vous préférez un environnement plus léger, vous pouvez utiliser VS Code.

1. **Prérequis (Extensions) :**
   - Installez l'extension **C# Dev Kit** (par Microsoft) dans VS Code.
   - Installez le **SDK .NET 9** sur votre machine.

2. **Ouverture :**
   - Ouvrez VS Code.
   - Faites `Fichier > Ouvrir le dossier` et sélectionnez le dossier racine du projet contenant le dossier `SmartMediaTransferAIDesktop`.

3. **Exécution via Terminal :**
   - Ouvrez un nouveau terminal intégré (`Ctrl` + `\`` ou `Terminal > Nouveau Terminal`).
   - Naviguez dans le dossier du projet :
     ```bash
     cd SmartMediaTransferAIDesktop
     ```
   - Restaurez les dépendances et compilez :
     ```bash
     dotnet restore
     dotnet build
     ```
   - Lancez l'application :
     ```bash
     dotnet run
     ```
   *(Note : Pour le débogage (F5) dans VS Code, le C# Dev Kit vous proposera automatiquement de générer les fichiers `.vscode/launch.json` et `.vscode/tasks.json` la première fois que vous irez dans l'onglet Exécuter et Déboguer).*

---

## 📱 2. Application Android (SmartMediaTransfer AI)

L'application Android est développée en **Kotlin**, avec **Jetpack Compose** et **Clean Architecture**.

1. **Prérequis :**
   - Téléchargez et installez **Android Studio** (version Hedgehog, Iguana ou plus récente).
   - Assurez-vous d'avoir installé le **SDK Android 34**.

2. **Ouverture :**
   - Lancez Android Studio.
   - Cliquez sur **Open** (Ouvrir).
   - Sélectionnez le dossier racine **`android-app`** (celui qui contient le fichier `build.gradle.kts` ou `settings.gradle.kts`).
   - Cliquez sur OK.

3. **Synchronisation :**
   - Android Studio va automatiquement télécharger Gradle 8.x et synchroniser les dépendances (Hilt, Room, Compose). Patientez jusqu'à ce que la barre de progression en bas à droite soit terminée.

4. **Exécution :**
   - Branchez votre téléphone Android (avec le mode Débogage USB activé) ou lancez un émulateur Android (AVD) depuis le Device Manager.
   - Cliquez sur le bouton vert **Play (Run 'app')** ou appuyez sur `Shift + F10` pour compiler, installer l'APK et lancer l'application sur le téléphone.

---

### ⚠️ Notes Importantes pour le réseau
- Pour que la **découverte automatique (mDNS)** et les **transferts** fonctionnent, l'ordinateur Windows et le téléphone Android doivent impérativement être connectés au **même réseau local (même réseau Wi-Fi)**.
- Lors du premier lancement du logiciel Windows, le **Pare-feu Windows Defender** va probablement afficher une alerte demandant d'autoriser l'application à communiquer sur les réseaux privés. Vous devez impérativement cliquer sur **"Autoriser l'accès"** pour que le PC puisse recevoir les transferts.
