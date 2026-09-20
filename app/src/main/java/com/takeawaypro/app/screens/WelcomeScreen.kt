package com.takeawaypro.app.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.takeawaypro.app.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onBrowse: () -> Unit,
    onAdminAccess: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Long-press the logo to reach the admin console — no visible entry point for customers.
            Image(
                painter = painterResource(R.drawable.ic_launcher),
                contentDescription = "TakeAway Pro",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                        onLongClick = onAdminAccess
                    )
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "TakeAway Pro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Fournitures pour restaurants et cafés",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                "Tout pour votre Take Away",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Se connecter")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Créer un compte")
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBrowse, modifier = Modifier.fillMaxWidth()) {
                Text("Parcourir les produits")
            }
        }
    }
}
