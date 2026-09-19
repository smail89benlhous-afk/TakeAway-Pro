const express = require("express");
const db = require("../db");
const { requireAuth, requireAdmin } = require("../middleware/auth");

const router = express.Router();

function toPublicProduct(row) {
    return {
        id: row.id,
        name: row.name,
        category: row.category,
        price: row.price,
        emoji: row.emoji,
        photoUrl: row.photo_url,
        description: row.description,
        popular: !!row.popular,
        stock: row.stock,
        hidden: !!row.hidden
    };
}

// GET /api/products — catalogue. Les restaurants ne reçoivent que les produits visibles ;
// l'admin (?all=1 avec un token admin) reçoit aussi les produits masqués, pour la console de gestion.
router.get("/", requireAuth, (req, res) => {
    const wantsAll = req.query.all === "1" && req.auth.role === "admin";
    const rows = wantsAll
        ? db.prepare("SELECT * FROM products ORDER BY category, name").all()
        : db.prepare("SELECT * FROM products WHERE hidden = 0 ORDER BY category, name").all();
    res.json({ products: rows.map(toPublicProduct) });
});

// POST /api/products — ajouter un produit (admin)
router.post("/", requireAuth, requireAdmin, (req, res) => {
    const { name, category, price, emoji, description, stock } = req.body || {};
    if (!name || !category || typeof price !== "number" || typeof stock !== "number") {
        return res.status(400).json({ error: "Nom, catégorie, prix et stock sont obligatoires." });
    }

    const id = "p" + Date.now();
    db.prepare(`
        INSERT INTO products (id, name, category, price, emoji, description, stock)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    `).run(id, name, category, price, emoji || "📦", description || null, stock);

    const row = db.prepare("SELECT * FROM products WHERE id = ?").get(id);
    res.status(201).json({ product: toPublicProduct(row) });
});

// PUT /api/products/:id — modifier un produit (admin)
router.put("/:id", requireAuth, requireAdmin, (req, res) => {
    const existing = db.prepare("SELECT * FROM products WHERE id = ?").get(req.params.id);
    if (!existing) return res.status(404).json({ error: "Produit introuvable." });

    const {
        name = existing.name,
        category = existing.category,
        price = existing.price,
        emoji = existing.emoji,
        description = existing.description,
        stock = existing.stock
    } = req.body || {};

    db.prepare(`
        UPDATE products
        SET name = ?, category = ?, price = ?, emoji = ?, description = ?, stock = ?, updated_at = datetime('now')
        WHERE id = ?
    `).run(name, category, price, emoji, description, stock, req.params.id);

    const row = db.prepare("SELECT * FROM products WHERE id = ?").get(req.params.id);
    res.json({ product: toPublicProduct(row) });
});

// PATCH /api/products/:id/visibility — masquer / afficher (admin)
router.patch("/:id/visibility", requireAuth, requireAdmin, (req, res) => {
    const { hidden } = req.body || {};
    const result = db.prepare("UPDATE products SET hidden = ? WHERE id = ?").run(hidden ? 1 : 0, req.params.id);
    if (result.changes === 0) return res.status(404).json({ error: "Produit introuvable." });
    res.json({ ok: true });
});

// DELETE /api/products/:id — supprimer (admin)
router.delete("/:id", requireAuth, requireAdmin, (req, res) => {
    const result = db.prepare("DELETE FROM products WHERE id = ?").run(req.params.id);
    if (result.changes === 0) return res.status(404).json({ error: "Produit introuvable." });
    res.json({ ok: true });
});

module.exports = router;
