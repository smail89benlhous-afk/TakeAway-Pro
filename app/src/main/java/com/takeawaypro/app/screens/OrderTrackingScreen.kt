package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takeawaypro.app.AppData
import com.takeawaypro.app.OrderStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    appData: AppData,
    orderId: Int,
    onBackToHome: () -> Unit
) {
    val order = appData.orderById(orderId)
    val currentStatus = order?.status ?: OrderStatus.RECEIVED

    Scaffold(
        topBar = { TopAppBar(title = { Text("Commande #$orderId") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                "✅ Commande passée avec succès",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))

            OrderStatus.entries.forEach { status ->
                val reached = status.ordinal <= currentStatus.ordinal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (reached) "✓" else "○", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        status.label,
                        fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (status != OrderStatus.entries.last()) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .height(20.dp)
                            .width(2.dp)
                    ) {}
                }
            }

            Spacer(Modifier.height(40.dp))
            Button(onClick = onBackToHome, modifier = Modifier.fillMaxWidth()) {
                Text("Retour à l'accueil")
            }
        }
    }
}
