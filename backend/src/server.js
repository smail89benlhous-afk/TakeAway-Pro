require("dotenv").config();
const express = require("express");
const cors = require("cors");

const authRoutes = require("./routes/auth");
const productRoutes = require("./routes/products");
const orderRoutes = require("./routes/orders");
const restaurantRoutes = require("./routes/restaurants");
const notificationRoutes = require("./routes/notifications");
const adminRoutes = require("./routes/admin");

const app = express();
app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
    res.json({ name: "TakeAway Pro API", status: "ok" });
});

app.use("/api/auth", authRoutes);
app.use("/api/products", productRoutes);
app.use("/api/orders", orderRoutes);
app.use("/api/restaurants", restaurantRoutes);
app.use("/api/notifications", notificationRoutes);
app.use("/api/admin", adminRoutes);

// 404
app.use((req, res) => {
    res.status(404).json({ error: "Route introuvable." });
});

// Gestion d'erreurs générique
// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
    console.error(err);
    res.status(500).json({ error: "Erreur interne du serveur." });
});

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
    console.log(`TakeAway Pro API en écoute sur http://localhost:${PORT}`);
});
