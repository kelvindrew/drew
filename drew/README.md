# Drew - IA d'Analyse et de Sélection d'Événements Sportifs

Drew est un moteur d'analyse sportive intégré dans un logiciel Windows.
Son objectif est de proposer automatiquement des combinaisons de matchs présentant une cote totale proche de celle demandée par l'utilisateur.

## Fonctionnalités Principales

* **Recherche de combinaisons** : L'utilisateur indique une plage de cotes (ex: 5.80 - 6.20). Le moteur recherche automatiquement une combinaison de 2 à 3 événements respectant ces critères.
* **Scoring et IA** : Chaque sélection reçoit un score de confiance (0-100) basé sur de multiples facteurs (forme, Elo, historique).
* **Détection des Risques** : Le moteur pénalise ou ignore les événements présentant des risques élevés (baisse anormale de cotes, blessures, équipes jeunes comme U19/U17).
* **Actualisation Automatique** : Les données sont actualisées périodiquement en arrière-plan.
* **Base de données Locale** : Sauvegarde des événements, cotes et historiques des combinaisons générées via SQLite.
* **Interface Utilisateur** : Une interface moderne et sombre développée avec PySide6 (Qt).

## Architecture du Projet

```
drew/
├── api/          # Gestionnaires d'API (actuellement utilisant des données simulées réalistes)
├── database/     # Gestionnaire SQLite et requêtes (CRUD)
├── engine/       # Cœur de l'application (Scoring, Combinaisons, Coordinateur)
├── ui/           # Interface graphique PySide6
├── models/       # Modèles de données (optionnel pour l'instant)
├── utils/        # Utilitaires divers
├── config.json   # Configuration principale du logiciel
├── main.py       # Point d'entrée de l'application
└── README.md
```

## Installation & Lancement

**Prérequis** : Python 3.10+

1. Installez les dépendances :
   ```bash
   pip install -r requirements.txt
   ```

2. Lancez le logiciel via le script fourni :
   ```bash
   ./run_drew.sh
   ```
   ou manuellement :
   ```bash
   export PYTHONPATH=$PYTHONPATH:$(pwd)
   python3 drew/main.py
   ```

## Note technique sur les données

Actuellement, les clés d'API n'étant pas fournies dans le `config.json`, le module `api_client.py` utilise des données générées localement (mock data) qui simulent le comportement de "The-Odds API" et d'une API de statistiques avancées. Cela permet de tester l'algorithme de génération de combinaisons et l'interface graphique de manière complètement fonctionnelle.
