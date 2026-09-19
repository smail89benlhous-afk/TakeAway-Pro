package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takeawaypro.app.AppData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    appData: AppData,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val subtotal = appData.subtotal()
    val total = subtotal + appData.deliveryFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Votre panier") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←") }
                }
            )
        },
        bottomBar = {
            if (appData.cart.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow("Sous-total", subtotal)
                        SummaryRow("Frais de livraison", appData.deliveryFee)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        SummaryRow("Total", total, bold = true)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
                            Text("✅ Passer la commande")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (appData.cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Votre panier est vide.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(appData.cart.toList()) { line ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductThumbnail(line.product, size = 40.dp, modifier = Modifier.padding(end = 10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.product.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    String.format("%.2f DH / unité", line.product.price),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedButton(onClick = {
                                appData.setQuantity(line.product, line.quantity - 1)
                            }) { Text("-") }
                            Text(
                                "${line.quantity}",
                                modifier = Modifier.padding(horizontal = 10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(onClick = {
                                appData.setQuantity(line.product, line.quantity + 1)
                            }) { Text("+") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            String.format("%.2f DH", amount),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}
