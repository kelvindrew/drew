# Architecture & Choix Techniques - BETPRO Analyst (Windows Desktop)

## 1. Vue d'ensemble
L'application est conçue comme un client lourd (Desktop) natif Windows, 100% autonome. Elle agrège des données sportives via des API et du Web Scraping (Playwright/OCR), les analyse statistiquement (Pandas/NumPy), et utilise une IA locale (LLM) pour fournir des recommandations et des rapports détaillés.

## 2. Choix Techniques

* **Langage** : Python 3.13+. Idéal pour l'analyse de données (Data Science), l'interfaçage avec des API locales d'IA, et la rapidité de développement.
* **Interface Graphique (GUI)** : PySide6 (Qt pour Python). Permet de créer une interface moderne, fluide, inspirée de Windows 11 (Mica/Fluent Design), avec gestion native du Light/Dark mode.
* **Base de données** : SQLite. Parfait pour une application Desktop autonome. Ne nécessite pas l'installation d'un serveur tiers (contrairement à PostgreSQL) tout en offrant d'excellentes performances pour le stockage local (historiques, stats, rapports).
* **Intelligence Artificielle** : Exécution d'un LLM en local (ex: Ollama, Llama.cpp) via une API REST locale. Cela garantit la confidentialité des données, l'absence de coûts d'API (OpenAI) et le fonctionnement hors-ligne pour la partie analyse.
* **Collecte de données** :
  * API : `httpx` avec `asyncio` pour des requêtes concurrentes non-bloquantes.
  * Scraping / Vision : `Playwright` pour l'interaction web automatisée et `OpenCV` + `Tesseract OCR` pour extraire des données visuelles si aucune API n'est disponible.
* **Moteur d'analyse** : `Pandas` et `NumPy` pour le calcul matriciel, les agrégations de statistiques et la génération d'indicateurs de performance (xG, formes, séries).

## 3. Architecture du Projet (Modèle MVC / MVVM)

L'architecture respecte les principes SOLID, en séparant la logique métier (Business Logic), la collecte des données (Data Access), l'intelligence artificielle, et l'interface utilisateur (Presentation).

```
project/
├── main.py              # Point d'entrée, initialisation de l'App PySide6
├── config.json          # Configuration centralisée (clés, thèmes, BDD)
├── ui/                  # Vues (PySide6) et ViewModels
├── api/                 # Connecteurs API externes (httpx, asyncio)
├── analysis/            # Moteur mathématique (Pandas, Numpy)
├── ai/                  # Interface avec le LLM local (Ollama/LMStudio)
├── database/            # ORM, requêtes SQLite
├── models/              # Classes métier (Team, Match, Player, Report)
├── vision/              # Playwright, OpenCV, OCR Tesseract
├── utils/               # Helpers, Logging
├── logs/                # Fichiers de log locaux
└── screenshots/         # Captures d'écran pour l'OCR / Logs visuels
```

## 4. Respect des bonnes pratiques (SOLID)
* **Single Responsibility** : Chaque module a une tâche unique (ex: `api/` ne fait que fetch les données, `analysis/` ne fait que calculer).
* **Open/Closed** : Le système de collecte est extensible. On peut rajouter un connecteur API sans modifier le moteur d'analyse.
* **Asynchronisme** : L'interface PySide6 ne "freeze" jamais. Toutes les tâches lourdes (Scraping, requêtes API, inférence IA) s'exécutent dans des threads dédiés (`QThread` ou boucle `asyncio`).
* **Robustesse** : Utilisation d'un système de logging complet (INFO, WARNING, ERROR, DEBUG). Les erreurs (API down, timeout) sont catchées, stockées et l'application utilise le cache SQLite local en fallback.
