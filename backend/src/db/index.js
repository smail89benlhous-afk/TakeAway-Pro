const path = require("path");
const Database = require("better-sqlite3");

const dbPath = path.join(__dirname, "..", "..", "data", "takeaway.sqlite");
const db = new Database(dbPath);

db.pragma("journal_mode = WAL");
db.pragma("foreign_keys = ON");

db.exec(`
CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role TEXT NOT NULL CHECK (role IN ('restaurant', 'admin')),
    business_type TEXT,
    business_name TEXT,
    manager_name TEXT,
    phone TEXT NOT NULL UNIQUE,
    email TEXT,
    password_hash TEXT NOT NULL,
    city TEXT,
    address TEXT,
    latitude REAL,
    longitude REAL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS products (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price REAL NOT NULL,
    emoji TEXT DEFAULT '📦',
    photo_url TEXT,
    description TEXT,
    popular INTEGER NOT NULL DEFAULT 0,
    stock INTEGER NOT NULL DEFAULT 0,
    hidden INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL REFERENCES accounts(id),
    subtotal REAL NOT NULL,
    delivery_fee REAL NOT NULL,
    total REAL NOT NULL,
    payment_method TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'RECEIVED'
        CHECK (status IN ('RECEIVED','CONFIRMED','PREPARING','DELIVERING','DELIVERED')),
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id TEXT NOT NULL,
    product_name TEXT NOT NULL,
    unit_price REAL NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    read INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);
`);

// Seed the product catalog once, on first run, so the API isn't empty out of the box.
const productCount = db.prepare("SELECT COUNT(*) AS count FROM products").get().count;
if (productCount === 0) {
    const insert = db.prepare(`
        INSERT INTO products (id, name, category, price, emoji, popular, stock)
        VALUES (@id, @name, @category, @price, @emoji, @popular, @stock)
    `);
    const seedProducts = [
        { id: "p1", name: "Gobelet plastique 30cl", category: "Gobelets", price: 0.50, emoji: "🥤", popular: 1, stock: 5000 },
        { id: "p2", name: "Gobelet café 25cl", category: "Gobelets", price: 0.45, emoji: "☕", popular: 1, stock: 4000 },
        { id: "p3", name: "Couvercle pour gobelet", category: "Gobelets", price: 0.25, emoji: "🔘", popular: 0, stock: 6000 },
        { id: "p4", name: "Boîte repas Take Away", category: "Boîtes", price: 1.20, emoji: "📦", popular: 1, stock: 2000 },
        { id: "p5", name: "Boîte burger", category: "Boîtes", price: 0.90, emoji: "🍔", popular: 0, stock: 1500 },
        { id: "p6", name: "Sac papier kraft", category: "Sacs", price: 0.80, emoji: "🛍️", popular: 0, stock: 1000 },
        { id: "p7", name: "Sac plastique", category: "Sacs", price: 0.30, emoji: "👜", popular: 0, stock: 2000 },
        { id: "p8", name: "Pailles", category: "Pailles", price: 0.10, emoji: "🥢", popular: 0, stock: 8000 },
        { id: "p9", name: "Cuillère plastique", category: "Couverts", price: 0.15, emoji: "🥄", popular: 0, stock: 3000 },
        { id: "p10", name: "Fourchette plastique", category: "Couverts", price: 0.15, emoji: "🍴", popular: 0, stock: 3000 },
        { id: "p11", name: "Serviette", category: "Serviettes", price: 0.08, emoji: "🧻", popular: 0, stock: 10000 },
        { id: "p12", name: "Film alimentaire", category: "Autres", price: 15.0, emoji: "🎞️", popular: 0, stock: 200 }
    ];
    const insertMany = db.transaction((rows) => rows.forEach((row) => insert.run(row)));
    insertMany(seedProducts);
}

// Seed the admin account on first run, from ADMIN_PHONE / ADMIN_PASSWORD env vars,
// so the hosted deployment has a working admin login without a separate manual step.
const bcrypt = require("bcryptjs");
const adminPhone = process.env.ADMIN_PHONE || "0600000000";
const adminPassword = process.env.ADMIN_PASSWORD || "admin123";
const existingAdmin = db.prepare("SELECT id FROM accounts WHERE phone = ?").get(adminPhone);
if (!existingAdmin) {
    const passwordHash = bcrypt.hashSync(adminPassword, 10);
    db.prepare(`
        INSERT INTO accounts (role, business_name, manager_name, phone, password_hash, city, address)
        VALUES ('admin', 'TakeAway Pro - Administration', 'Administrateur', ?, ?, '-', '-')
    `).run(adminPhone, passwordHash);
    console.log(`Compte administrateur initial créé pour le numéro ${adminPhone}.`);
}

module.exports = db;
