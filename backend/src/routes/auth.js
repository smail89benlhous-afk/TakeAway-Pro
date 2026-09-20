const express = require("express");
const bcrypt = require("bcryptjs");
const db = require("../db");
const { signToken, requireAuth } = require("../middleware/auth");

const router = express.Router();

function toPublicAccount(row) {
    return {
        id: row.id,
        role: row.role,
        businessType: row.business_type,
        businessName: row.business_name,
        managerName: row.manager_name,
        phone: row.phone,
        email: row.email,
        city: row.city,
        address: row.address,
        latitude: row.latitude,
        longitude: row.longitude
    };
}

// POST /api/auth/register  — inscription restaurant/café (écran "Créer un compte")
router.post("/register", (req, res) => {
    const {
        businessType, businessName, managerName,
        phone, email, password, city, address, latitude, longitude
    } = req.body || {};

    if (!businessName || !managerName || !phone || !password || !city || !address) {
        return res.status(400).json({ error: "Merci de remplir tous les champs obligatoires." });
    }

    const existing = db.prepare("SELECT id FROM accounts WHERE phone = ?").get(phone);
    if (existing) {
        return res.status(409).json({ error: "Un compte existe déjà avec ce numéro de téléphone." });
    }

    const passwordHash = bcrypt.hashSync(password, 10);

    const result = db.prepare(`
        INSERT INTO accounts (role, business_type, business_name, manager_name, phone, email, password_hash, city, address, latitude, longitude)
        VALUES ('restaurant', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(
        businessType || "AUTRE", businessName, managerName, phone, email || null,
        passwordHash, city, address,
        typeof latitude === "number" ? latitude : null,
        typeof longitude === "number" ? longitude : null
    );

    const account = db.prepare("SELECT * FROM accounts WHERE id = ?").get(result.lastInsertRowid);
    const token = signToken(account);

    res.status(201).json({ token, account: toPublicAccount(account) });
});

// POST /api/auth/login — connexion restaurant/café OU administrateur (même écran, rôle détecté côté serveur)
router.post("/login", (req, res) => {
    const { phone, password } = req.body || {};
    if (!phone || !password) {
        return res.status(400).json({ error: "Merci de remplir le téléphone et le mot de passe." });
    }

    const account = db.prepare("SELECT * FROM accounts WHERE phone = ?").get(phone);
    if (!account || !bcrypt.compareSync(password, account.password_hash)) {
        return res.status(401).json({ error: "Numéro de téléphone ou mot de passe incorrect." });
    }

    const token = signToken(account);
    res.json({ token, account: toPublicAccount(account) });
});

// GET /api/auth/me — infos du compte connecté (pour restaurer la session au démarrage de l'app)
router.get("/me", requireAuth, (req, res) => {
    const account = db.prepare("SELECT * FROM accounts WHERE id = ?").get(req.auth.accountId);
    if (!account) return res.status(404).json({ error: "Compte introuvable." });
    res.json({ account: toPublicAccount(account) });
});

module.exports = router;
