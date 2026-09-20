# TakeAway Pro

Plateforme pour restaurants, cafés et snacks : commande d'emballages et consommables Take Away.

## Statut : Version 1.1 — connectée au vrai Backend

L'application Android est maintenant branchée sur l'API réelle (Node.js/Express/SQLite,
hébergée sur Railway). Les comptes, le catalogue, les commandes, les restaurants et les
notifications sont partagés entre tous les téléphones qui utilisent l'app — ce n'est plus
du stockage local uniquement.

### Ce qui marche maintenant en réseau
- Inscription / connexion restaurant & café (mot de passe vérifié côté serveur, JWT)
- Connexion administrateur (même mécanisme, avec vérification du rôle)
- Catalogue produits : visible sans compte (guest browsing), à jour pour tout le monde
- Passage de commande : le serveur recalcule les prix à partir du catalogue réel
- Suivi de commande en direct (le statut vient du serveur)
- Console admin : Dashboard (vrais chiffres), gestion des produits (ajout/modif/masquer/
  supprimer), liste des restaurants avec détail et historique, notifications de nouvelles
  commandes, changement de statut de commande

### Limites connues (prochaines étapes)
- Les photos choisies par l'admin restent locales à son téléphone (pas encore d'upload
  réel vers un serveur/stockage) — à faire quand un service de stockage d'images sera
  branché
- Pas encore de vrai système de rôles/permissions multi-admin (un seul compte admin créé
  automatiquement au démarrage du serveur, via `ADMIN_PHONE`/`ADMIN_PASSWORD`)
- Paiement électronique toujours désactivé (refusé volontairement côté API)
- Le backend tourne sur le plan gratuit de Railway : peut se mettre en veille après une
  période d'inactivité (léger délai au premier appel après une pause)

## Structure du dépôt
- `app/` — application Android (Jetpack Compose), voir `BUILD_INSTRUCTIONS.txt`
- `backend/` — API + base de données (Node.js/Express/SQLite), voir `backend/README.md`
- `.github/workflows/build-apk.yml` — build automatique de l'APK sur GitHub Actions à
  chaque push (pas besoin d'ordinateur pour obtenir un APK installable)

## Prochaines étapes
1. Upload réel des photos produits vers un stockage (S3, Cloudinary...)
2. Vrai système de comptes/rôles multi-administrateurs
3. Sélection de la position sur une carte à l'inscription
4. Paiement électronique
5. Web Admin séparé (peut réutiliser directement l'API existante)
6. Notifications push réelles (Firebase Cloud Messaging) et factures PDF
7. Passage à une offre payante sur Railway (ou migration hébergement) avant un vrai
   lancement commercial, pour éviter la mise en veille du serveur gratuit
