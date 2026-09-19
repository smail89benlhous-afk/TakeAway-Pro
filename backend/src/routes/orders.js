const express = require("express");
const db = require("../db");
const { requireAuth, requireAdmin } = require("../middleware/auth");

const router = express.Router();
const DELIVERY_FEE = Number(process.env.DELIVERY_FEE || 20);
const STATUS_ORDER = ["RECEIVED", "CONFIRMED", "PREPARING", "DELIVERING", "DELIVERED"];

function serializeOrder(orderRow) {
    const items = db.prepare("SELECT * FROM order_items WHERE order_id = ?").all(orderRow.id);
    const account = db.prepare("SELECT * FROM accounts WHERE id = ?").get(orderRow.account_id);
    return {
        id: orderRow.id,
        status: orderRow.status,
        subtotal: orderRow.subtotal,
        deliveryFee: orderRow.delivery_fee,
        total: orderRow.total,
        paymentMethod: orderRow.payment_method,
        createdAt: orderRow.created_at,
        account: account ? {
            businessName: account.business_name,
            city: account.city,
            phone: account.phone,
            address: account.address
        } : null,
        items: items.map((it) => ({
            productId: it.product_id,
            productName: it.product_name,
            unitPrice: it.unit_price,
            quantity: it.quantity
        }))
    };
}

// POST /api/orders — passer une commande (restaurant connecté).
// Le serveur recalcule les prix à partir du catalogue actuel : on ne fait jamais confiance
// aux prix envoyés par le client.
router.post("/", requireAuth, (req, res) => {
    if (req.auth.role !== "restaurant") {
        return res.status(403).json({ error: "Seul un compte restaurant/café peut passer commande." });
    }

    const { items, paymentMethod } = req.body || {};
    if (!Array.isArray(items) || items.length === 0) {
        return res.status(400).json({ error: "Le panier est vide." });
    }
    if (!["CASH_ON_DELIVERY", "ELECTRONIC"].includes(paymentMethod)) {
        return res.status(400).json({ error: "Mode de paiement invalide." });
    }
    if (paymentMethod === "ELECTRONIC") {
        return res.status(400).json({ error: "Le paiement électronique sera disponible prochainement." });
    }

    const placeOrder = db.transaction(() => {
        let subtotal = 0;
        const resolvedItems = [];

        for (const item of items) {
            const product = db.prepare("SELECT * FROM products WHERE id = ? AND hidden = 0").get(item.productId);
            if (!product) throw new Error(`Produit introuvable: ${item.productId}`);
            const quantity = Number(item.quantity);
            if (!Number.isInteger(quantity) || quantity <= 0) throw new Error("Quantité invalide.");

            subtotal += product.price * quantity;
            resolvedItems.push({ product, quantity });
        }

        const total = subtotal + DELIVERY_FEE;

        const orderResult = db.prepare(`
            INSERT INTO orders (account_id, subtotal, delivery_fee, total, payment_method, status)
            VALUES (?, ?, ?, ?, ?, 'RECEIVED')
        `).run(req.auth.accountId, subtotal, DELIVERY_FEE, total, paymentMethod);

        const orderId = orderResult.lastInsertRowid;

        const insertItem = db.prepare(`
            INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity)
            VALUES (?, ?, ?, ?, ?)
        `);
        for (const { product, quantity } of resolvedItems) {
            insertItem.run(orderId, product.id, product.name, product.price, quantity);
            db.prepare("UPDATE products SET stock = MAX(stock - ?, 0) WHERE id = ?").run(quantity, product.id);
        }

        const account = db.prepare("SELECT business_name FROM accounts WHERE id = ?").get(req.auth.accountId);
        const totalItems = resolvedItems.reduce((sum, { quantity }) => sum + quantity, 0);
        db.prepare(`
            INSERT INTO notifications (order_id, title, message)
            VALUES (?, ?, ?)
        `).run(
            orderId,
            `Nouvelle commande #${orderId}`,
            `${account?.business_name || "Un client"} vient de passer une commande de ${totalItems} produits.`
        );

        return orderId;
    });

    try {
        const orderId = placeOrder();
        const orderRow = db.prepare("SELECT * FROM orders WHERE id = ?").get(orderId);
        res.status(201).json({ order: serializeOrder(orderRow) });
    } catch (err) {
        res.status(400).json({ error: err.message || "Impossible de créer la commande." });
    }
});

// GET /api/orders/mine — historique + suivi pour le restaurant connecté
router.get("/mine", requireAuth, (req, res) => {
    const rows = db.prepare("SELECT * FROM orders WHERE account_id = ? ORDER BY id DESC").all(req.auth.accountId);
    res.json({ orders: rows.map(serializeOrder) });
});

// GET /api/orders/:id — détail d'une commande (le propriétaire ou l'admin)
router.get("/:id", requireAuth, (req, res) => {
    const orderRow = db.prepare("SELECT * FROM orders WHERE id = ?").get(req.params.id);
    if (!orderRow) return res.status(404).json({ error: "Commande introuvable." });
    if (req.auth.role !== "admin" && orderRow.account_id !== req.auth.accountId) {
        return res.status(403).json({ error: "Accès non autorisé à cette commande." });
    }
    res.json({ order: serializeOrder(orderRow) });
});

// GET /api/orders — toutes les commandes (admin)
router.get("/", requireAuth, requireAdmin, (req, res) => {
    const rows = db.prepare("SELECT * FROM orders ORDER BY id DESC").all();
    res.json({ orders: rows.map(serializeOrder) });
});

// PATCH /api/orders/:id/status — avancer le statut de la commande (admin)
router.patch("/:id/status", requireAuth, requireAdmin, (req, res) => {
    const { status } = req.body || {};
    if (!STATUS_ORDER.includes(status)) {
        return res.status(400).json({ error: "Statut invalide." });
    }
    const result = db.prepare("UPDATE orders SET status = ? WHERE id = ?").run(status, req.params.id);
    if (result.changes === 0) return res.status(404).json({ error: "Commande introuvable." });

    const orderRow = db.prepare("SELECT * FROM orders WHERE id = ?").get(req.params.id);
    res.json({ order: serializeOrder(orderRow) });
});

module.exports = router;
