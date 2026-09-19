const express = require("express");
const db = require("../db");
const { requireAuth, requireAdmin } = require("../middleware/auth");

const router = express.Router();

// GET /api/notifications — toutes les notifications de nouvelles commandes (admin)
router.get("/", requireAuth, requireAdmin, (req, res) => {
    const rows = db.prepare("SELECT * FROM notifications ORDER BY id DESC").all();
    res.json({
        notifications: rows.map((n) => ({
            id: n.id,
            orderId: n.order_id,
            title: n.title,
            message: n.message,
            read: !!n.read,
            createdAt: n.created_at
        }))
    });
});

// PATCH /api/notifications/:id/read — marquer comme lue (admin)
router.patch("/:id/read", requireAuth, requireAdmin, (req, res) => {
    const result = db.prepare("UPDATE notifications SET read = 1 WHERE id = ?").run(req.params.id);
    if (result.changes === 0) return res.status(404).json({ error: "Notification introuvable." });
    res.json({ ok: true });
});

module.exports = router;
