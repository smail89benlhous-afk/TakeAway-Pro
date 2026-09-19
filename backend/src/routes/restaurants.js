const express = require("express");
const db = require("../db");
const { requireAuth, requireAdmin } = require("../middleware/auth");

const router = express.Router();

// GET /api/restaurants — liste des restaurants/cafés inscrits (admin)
router.get("/", requireAuth, requireAdmin, (req, res) => {
    const rows = db.prepare(`
        SELECT a.*, (
            SELECT COUNT(*) FROM orders o WHERE o.account_id = a.id
        ) AS order_count
        FROM accounts a
        WHERE a.role = 'restaurant'
        ORDER BY a.business_name
    `).all();

    res.json({
        restaurants: rows.map((r) => ({
            id: r.id,
            businessType: r.business_type,
            businessName: r.business_name,
            managerName: r.manager_name,
            phone: r.phone,
            email: r.email,
            city: r.city,
            address: r.address,
            orderCount: r.order_count
        }))
    });
});

// GET /api/restaurants/:id — fiche détaillée + commandes (admin)
router.get("/:id", requireAuth, requireAdmin, (req, res) => {
    const account = db.prepare("SELECT * FROM accounts WHERE id = ? AND role = 'restaurant'").get(req.params.id);
    if (!account) return res.status(404).json({ error: "Établissement introuvable." });

    const orders = db.prepare("SELECT * FROM orders WHERE account_id = ? ORDER BY id DESC").all(account.id);
    const totalPurchases = orders.reduce((sum, o) => sum + o.total, 0);

    res.json({
        restaurant: {
            id: account.id,
            businessType: account.business_type,
            businessName: account.business_name,
            managerName: account.manager_name,
            phone: account.phone,
            email: account.email,
            city: account.city,
            address: account.address
        },
        orders: orders.map((o) => ({
            id: o.id,
            status: o.status,
            total: o.total,
            createdAt: o.created_at
        })),
        totalPurchases
    });
});

module.exports = router;
