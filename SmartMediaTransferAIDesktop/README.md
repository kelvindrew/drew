# Smart Media Transfer AI Desktop

## Description
Un logiciel Windows premium pour le transfert et l'organisation automatique de médias intelligents entre un smartphone Android et un PC Windows. Conçu avec une architecture .NET 9 moderne, MVVM, et WPF-UI pour offrir l'apparence native de Windows 11 Fluent Design (Mica, ombres, animations fluides).

## Fonctionnalités Principales
- **Découverte Automatique (mDNS) :** Plus besoin d'entrer d'IP, l'application détecte automatiquement le téléphone Android sur le réseau local.
- **Transfert Sécurisé et Ultra-Rapide :** Transmission TCP multi-threadée, protégée par TLS et AES-256.
- **Organisation par Intelligence (Auto-Categorization) :** Classe les photos, vidéos et documents par catégorie et par date, et détecte automatiquement les doublons (via hash SHA-256).
- **Interface Premium :** Tableau de bord interactif avec animations fluides 120Hz et indicateurs graphiques.
- **Mode Portable & Arrière-plan :** Conçu pour fonctionner sans nécessiter de droits administrateurs, avec une intégration profonde en arrière-plan via System Tray.

## Technologies Utilisées
- **C# .NET 9 (Windows App SDK / WPF-UI)**
- **CommunityToolkit.Mvvm**
- **SQLite (sqlite-net-pcl)**
- **Sockets (TCP/UDP, SSLStream)**

## Compilation
1. Assurez-vous d'avoir le SDK .NET 9 installé.
2. `cd SmartMediaTransferAIDesktop`
3. `dotnet restore`
4. `dotnet build`
5. `dotnet run`
