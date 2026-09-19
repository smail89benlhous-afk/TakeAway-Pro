# TakeAway Pro

Plateforme pour restaurants, cafés et snacks : commande d'emballages et consommables Take Away.

Ce dépôt contient deux parties :
- `app/` — l'application Android (Jetpack Compose) : écrans client + console administrateur.
  **Fonctionne dès maintenant, toute seule**, avec sauvegarde locale sur l'appareil.
- `backend/` — l'API REST + base de données (Node.js/Express + SQLite), **prête mais pas
  encore branchée** à l'application. C'est la prochaine grande étape.

## Statut : Version 1.0

### Application Android (`app/`) — utilisable directement
- Écran d'accueil / splash, connexion, inscription restaurant/café
- Accueil : recherche, catégories, produits populaires, catalogue complet
- Panier, confirmation de commande (paiement à la livraison), suivi de commande
- **Console administrateur** (via « 🛠️ Espace administrateur », protégée par mot de passe —
  voir BUILD_INSTRUCTIONS.txt) : Dashboard, gestion des produits (avec vraies photos depuis
  la galerie), gestion des restaurants/cafés, notifications, détail de commande avec
  changement de statut
- **Sauvegarde locale** : compte, catalogue, commandes et notifications sont stockés sur
  l'appareil (SharedPreferences en JSON) et survivent à la fermeture de l'app
- ⚠️ Tout reste local à l'appareil : rien ne se synchronise entre plusieurs téléphones tant
  que l'app n'est pas branchée sur `backend/`

### Backend (`backend/`) — scaffold prêt, pas encore connecté
- API REST complète : comptes (inscription/connexion restaurant + admin avec JWT),
  catalogue produits (CRUD), commandes (création avec recalcul des prix côté serveur, suivi,
  changement de statut), restaurants (gestion admin), notifications, stats du dashboard
- Base de données SQLite avec le catalogue de démonstration pré-rempli
- Voir `backend/README.md` pour l'installation, l'authentification et la liste complète des
  routes

## Prochaines étapes
1. **Connecter l'app Android au backend** (Retrofit/OkHttp à la place de `LocalStore` local)
   — c'est l'étape la plus importante maintenant que l'API existe et que l'app fonctionne
   bien en local
2. Upload réel des photos produits vers un serveur/stockage (S3, Cloudinary...) — actuellement
   les photos choisies par l'admin restent sur l'appareil qui les a ajoutées
3. Vrai système de comptes/rôles pour l'espace administrateur (actuellement un simple mot de
   passe partagé, local à l'app)
4. Sélection de la position sur une carte
5. Paiement électronique (actuellement refusé volontairement par l'API et désactivé côté app)
6. Web Admin séparé (actuellement la console admin est dans la même app Android — peut
   réutiliser directement l'API backend)
7. Notifications push réelles (Firebase Cloud Messaging) et factures PDF
8. Migration SQLite → PostgreSQL/MySQL pour un déploiement en production multi-serveurs
