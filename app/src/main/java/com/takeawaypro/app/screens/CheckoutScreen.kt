package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takeawaypro.app.AppData
import com.takeawaypro.app.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    appData: AppData,
    onBack: () -> Unit,
    onOrderConfirmed: (Int) -> Unit
) {
    val account = appData.account.value
    val subtotal = appData.subtotal()
    val total = subtotal + appData.deliveryFee
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmation de la commande") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            String.format("%.2f DH", total),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val order = appData.placeOrder(paymentMethod)
                            onOrderConfirmed(order.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirmer la commande")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        account?.businessName?.ifBlank { "Invité" } ?: "Invité",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(account?.city ?: "-")
                    Text(account?.phone ?: "-")
                    Spacer(Modifier.height(6.dp))
                    Text("Adresse de livraison", style = MaterialTheme.typography.labelMedium)
                    Text(account?.address?.ifBlank { "-" } ?: "-")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Produits", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            appData.cart.forEach { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${line.product.emoji} ${line.product.name} × ${line.quantity}")
                    Text(String.format("%.2f DH", line.product.price * line.quantity))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sous-total")
                Text(String.format("%.2f DH", subtotal))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Frais de livraison")
                Text(String.format("%.2f DH", appData.deliveryFee))
            }

            Spacer(Modifier.height(20.dp))
            Text("Mode de paiement", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            PaymentMethod.entries.forEach { method ->
                val enabled = method == PaymentMethod.CASH_ON_DELIVERY
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = paymentMethod == method,
                        onClick = { if (enabled) paymentMethod = method },
                        enabled = enabled
                    )
                    Text(
                        (if (method == PaymentMethod.CASH_ON_DELIVERY) "💵 " else "💳 ") + method.label
                    )
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
