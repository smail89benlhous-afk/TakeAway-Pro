package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takeawaypro.app.AppData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRestaurantDetailScreen(
    appData: AppData,
    businessName: String,
    onBack: () -> Unit,
    onOpenOrder: (Int) -> Unit
) {
    val restaurant = appData.restaurants.firstOrNull { it.businessName == businessName }
    val orders = appData.orders.filter { it.account.businessName == businessName }
    val totalPurchases = orders.sumOf { it.total }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(businessName) },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Établissement", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${restaurant?.businessType?.emoji ?: ""} $businessName",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Ville: ${restaurant?.city ?: "-"}")
                        Text("Téléphone: ${restaurant?.phone ?: "-"}")
                        Text("Adresse de livraison: ${restaurant?.address ?: "-"}")
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Commandes", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${(restaurant?.baselineOrderCount ?: 0) + orders.size}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Achats (app)", style = MaterialTheme.typography.labelMedium)
                            Text(
                                String.format("%.0f DH", totalPurchases),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }

            item {
                Text("Commandes & factures", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            if (orders.isEmpty()) {
                item { Text("Aucune commande passée depuis l'application pour le moment.") }
            } else {
                items(orders, key = { it.id }) { order ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Commande #${order.id}", fontWeight = FontWeight.SemiBold)
                                Text(order.status.label, style = MaterialTheme.typography.bodySmall)
                                Text(String.format("%.2f DH", order.total), style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { onOpenOrder(order.id) }) { Text("Voir →") }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
