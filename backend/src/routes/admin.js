const express = require("express");
const db = require("../db");
const { requireAuth, requireAdmin } = require("../middleware/auth");

const router = express.Router();

// GET /api/admin/stats — chiffres du Dashboard (commandes, clients, produits, chiffre d'affaires)
router.get("/stats", requireAuth, requireAdmin, (req, res) => {
    const orderCount = db.prepare("SELECT COUNT(*) AS count FROM orders").get().count;
    const clientCount = db.prepare("SELECT COUNT(*) AS count FROM accounts WHERE role = 'restaurant'").get().count;
    const productCount = db.prepare("SELECT COUNT(*) AS count FROM products").get().count;
    const revenue = db.prepare("SELECT COALESCE(SUM(total), 0) AS total FROM orders").get().total;

    res.json({
        orderCount,
        clientCount,
        productCount,
        revenue
    });
});

module.exports = router;
