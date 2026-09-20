# TakeAway Pro — Backend

API REST + base de données SQLite pour l'application TakeAway Pro : comptes (restaurants/cafés + administrateur),
catalogue de produits, commandes, statuts de livraison et notifications.

## Démarrage

```bash
cd backend
npm install
cp .env.example .env      # puis modifiez JWT_SECRET, ADMIN_PHONE, ADMIN_PASSWORD si besoin
npm run seed:admin        # crée le compte administrateur (une seule fois)
npm start                 # démarre l'API sur http://localhost:4000
```

La base de données (`data/takeaway.sqlite`) est créée automatiquement au premier démarrage, avec le catalogue
de produits de démonstration déjà rempli.

> **Note** : `better-sqlite3` installe un module natif précompilé pour la plupart des plateformes (Windows/macOS/Linux, Node 18+).
> Si `npm install` échoue dessus, vérifiez que vous utilisez une version de Node récente (LTS) ou installez les outils de
> compilation de votre système (`build-essential` sur Linux, Xcode Command Line Tools sur macOS, "Desktop development with C++" sur Windows).

## Authentification

Toutes les routes (sauf `/api/auth/register` et `/api/auth/login`) nécessitent un header :

```
Authorization: Bearer <token>
```

Le token est renvoyé par `/api/auth/register` ou `/api/auth/login`, valable 30 jours.

## Résumé des routes

### Auth
| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Inscription restaurant/café → retourne `{ token, account }` |
| POST | `/api/auth/login` | public | Connexion restaurant/café ou admin (même route, rôle détecté par le téléphone) |
| GET | `/api/auth/me` | connecté | Infos du compte connecté |

### Produits
| Méthode | Route | Accès | Description |
|---|---|---|---|
| GET | `/api/products` | connecté | Catalogue (masqués exclus, sauf admin avec `?all=1`) |
| POST | `/api/products` | admin | Ajouter un produit |
| PUT | `/api/products/:id` | admin | Modifier un produit |
| PATCH | `/api/products/:id/visibility` | admin | Masquer / afficher (`{ "hidden": true }`) |
| DELETE | `/api/products/:id` | admin | Supprimer |

### Commandes
| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/api/orders` | restaurant | Passer commande — prix recalculés côté serveur, stock décrémenté, notification créée |
| GET | `/api/orders/mine` | restaurant | Historique + suivi du compte connecté |
| GET | `/api/orders/:id` | propriétaire ou admin | Détail d'une commande |
| GET | `/api/orders` | admin | Toutes les commandes |
| PATCH | `/api/orders/:id/status` | admin | Avancer le statut (`RECEIVED → CONFIRMED → PREPARING → DELIVERING → DELIVERED`) |

### Restaurants (admin)
| Méthode | Route | Description |
|---|---|---|
| GET | `/api/restaurants` | Liste des établissements inscrits + nombre de commandes |
| GET | `/api/restaurants/:id` | Fiche détaillée + historique de commandes |

### Notifications (admin)
| Méthode | Route | Description |
|---|---|---|
| GET | `/api/notifications` | Liste des notifications de nouvelles commandes |
| PATCH | `/api/notifications/:id/read` | Marquer comme lue |

### Dashboard (admin)
| Méthode | Route | Description |
|---|---|---|
| GET | `/api/admin/stats` | `{ orderCount, clientCount, productCount, revenue }` |

## Exemple : inscription puis commande

```bash
# 1. Inscription d'un café
curl -X POST http://localhost:4000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "businessType": "CAFE",
    "businessName": "Café Atlas",
    "managerName": "Yassine",
    "phone": "0611111111",
    "password": "motdepasse",
    "city": "Marrakech",
    "address": "Rue Mohammed V"
  }'
# → { "token": "...", "account": { ... } }

# 2. Passer une commande avec le token reçu
curl -X POST http://localhost:4000/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "items": [{ "productId": "p1", "quantity": 500 }, { "productId": "p3", "quantity": 500 }],
    "paymentMethod": "CASH_ON_DELIVERY"
  }'
```

## Prochaines étapes suggérées
- Upload réel des photos produits (actuellement `emoji` + `photo_url` optionnel dans le schéma, prêt pour un service de stockage type S3/Cloudinary)
- Passer de SQLite à PostgreSQL/MySQL pour un déploiement multi-serveurs
- Paiement électronique (actuellement refusé volontairement par l'API — `payment_method = 'ELECTRONIC'`)
- Notifications push (actuellement uniquement en base ; à brancher sur Firebase Cloud Messaging)
- Brancher l'application Android (Retrofit/OkHttp) et un futur Web Admin sur cette même API
