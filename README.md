# Premium Companion App - Core Architecture

Ce projet contient les fondations architecturales pour l'application "Premium Companion", conçue pour devenir une plateforme mondiale de mise en relation.

## 🗄️ Base de Données (Supabase / PostgreSQL)
Le fichier `SCHEMA.sql` contient :
- Les tables robustes pour gérer les Utilisateurs, les profils Companions, et les Réservations.
- Les systèmes de sécurité **Row Level Security (RLS)** pour assurer l'étanchéité des données (RGPD).
- Les tables pour le Chat (Messages), les transactions de paiements (Escrow) et les signalements.
- Index PostGIS pour la géolocalisation.

## ⚙️ Backend (NestJS)
Situé dans `/backend/`, le backend est orchestré par NestJS.
- **Clean Architecture** via des modules séparés (users, bookings, payments, ai).
- Préconfiguration de Supabase et OpenAI pour gérer la logique métier critique (matching, modération de sécurité, gestion des séquestres Stripe).

## 📱 Mobile (Flutter)
Situé dans `/frontend_flutter/`, l'application mobile utilise Flutter pour des performances natives multiplateformes.
- Architecture basée sur des Features (Auth, Profile, Booking) séparées en `Data`, `Domain`, `Presentation`.
- Fichier `app_theme.dart` implémentant le style **Ultra Premium** (Dark mode profond, accents Gold et Neon Green).
- Dépendances intégrées pour le paiement (Stripe), le BLoC, et le rendu UI avancé (Glassmorphism).

## 📄 Architecture Détaillée
Lisez `ARCHITECTURE.md` pour le raisonnement complet derrière chaque choix technique.
