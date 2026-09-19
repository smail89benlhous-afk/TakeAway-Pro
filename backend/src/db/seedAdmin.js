require("dotenv").config();
const bcrypt = require("bcryptjs");
const db = require("./index");

const phone = process.env.ADMIN_PHONE || "0600000000";
const password = process.env.ADMIN_PASSWORD || "admin123";

const existing = db.prepare("SELECT id FROM accounts WHERE phone = ?").get(phone);

if (existing) {
    console.log(`Un compte existe déjà pour le numéro ${phone} (id ${existing.id}). Rien à faire.`);
    process.exit(0);
}

const passwordHash = bcrypt.hashSync(password, 10);

db.prepare(`
    INSERT INTO accounts (role, business_name, manager_name, phone, password_hash, city, address)
    VALUES ('admin', 'TakeAway Pro - Administration', 'Administrateur', ?, ?, '-', '-')
`).run(phone, passwordHash);

console.log("Compte administrateur créé avec succès.");
console.log(`  Téléphone : ${phone}`);
console.log(`  Mot de passe : ${password}`);
console.log("Pensez à changer ce mot de passe et à définir ADMIN_PASSWORD dans .env avant la mise en production.");
