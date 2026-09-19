package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun AdminOrderDetailScreen(
    appData: AppData,
    orderId: Int,
    onBack: () -> Unit
) {
    val order = appData.orderById(orderId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Commande #$orderId") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        if (order == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Commande introuvable.") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        order.account.businessName.ifBlank { "Client" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(order.account.city)
                    Text(order.account.phone)
                    Text("Adresse: ${order.account.address}")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Produits", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            order.lines.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${line.product.emoji} ${line.product.name} × ${line.quantity}")
                    Text(String.format("%.2f DH", line.product.price * line.quantity))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text(String.format("%.2f DH", order.total), fontWeight = FontWeight.Bold)
            }
            Text(
                "Paiement: ${order.paymentMethod.label}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))
            Text("Statut de la commande", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            OrderStatus.entries.forEach { status ->
                val reached = status.ordinal <= order.status.ordinal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (reached) "✓" else "○", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Text(status.label, fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal)
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(16.dp))
            val nextStatus = OrderStatus.entries.getOrNull(order.status.ordinal + 1)
            if (nextStatus != null) {
                Button(
                    onClick = { appData.updateOrderStatus(order.id, nextStatus) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Passer à : ${nextStatus.label}")
                }
            } else {
                Text("Commande livrée ✅", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
