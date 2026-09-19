package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

// v1: a single local password protects the admin console on this device.
// This is a placeholder until real backend authentication (separate admin
// accounts, roles, etc.) exists.
const val ADMIN_PASSWORD = "admin123"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onBack: () -> Unit,
    onUnlocked: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Espace administrateur") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("🛠️ Accès réservé", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Entrez le mot de passe administrateur pour ouvrir le tableau de bord.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Mot de passe administrateur") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (password == ADMIN_PASSWORD) onUnlocked()
                    else error = "Mot de passe incorrect."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ouvrir le tableau de bord")
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Mot de passe par défaut (v1, à changer) : $ADMIN_PASSWORD",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
