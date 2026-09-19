const jwt = require("jsonwebtoken");

const JWT_SECRET = process.env.JWT_SECRET || "change-this-secret-before-going-to-production";

function signToken(account) {
    return jwt.sign(
        { accountId: account.id, role: account.role },
        JWT_SECRET,
        { expiresIn: "30d" }
    );
}

function requireAuth(req, res, next) {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7) : null;

    if (!token) {
        return res.status(401).json({ error: "Authentification requise." });
    }

    try {
        const payload = jwt.verify(token, JWT_SECRET);
        req.auth = payload; // { accountId, role }
        next();
    } catch (err) {
        return res.status(401).json({ error: "Session invalide ou expirée." });
    }
}

function requireAdmin(req, res, next) {
    if (!req.auth || req.auth.role !== "admin") {
        return res.status(403).json({ error: "Accès réservé à l'administrateur." });
    }
    next();
}

module.exports = { signToken, requireAuth, requireAdmin, JWT_SECRET };
