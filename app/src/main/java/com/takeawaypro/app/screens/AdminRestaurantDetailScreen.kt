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
import com.takeawaypro.app.RestaurantDetailResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRestaurantDetailScreen(
    appData: AppData,
    restaurantId: Int,
    onBack: () -> Unit,
    onOpenOrder: (Int) -> Unit
) {
    var detail by remember { mutableStateOf<RestaurantDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(restaurantId) {
        isLoading = true
        error = null
        appData.loadRestaurantDetail(restaurantId)
            .onSuccess { detail = it }
            .onFailure { error = it.message ?: "Impossible de charger cet établissement." }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.restaurant?.businessName ?: "Établissement") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        if (isLoading && detail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (error != null && detail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }
        val data = detail ?: return@Scaffold
        val restaurant = data.restaurant

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
                        Text("Bienvenue à", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${restaurant.businessType?.let { runCatching { com.takeawaypro.app.BusinessType.valueOf(it).emoji }.getOrNull() } ?: ""} ${restaurant.businessName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Ville: ${restaurant.city ?: "-"}")
                        Text("Téléphone: ${restaurant.phone}")
                        Text("Adresse de livraison: ${restaurant.address ?: "-"}")
                        if (restaurant.latitude != null && restaurant.longitude != null) {
                            Spacer(Modifier.height(8.dp))
                            val context = androidx.compose.ui.platform.LocalContext.current
                            OutlinedButton(
                                onClick = {
                                    val uri = android.net.Uri.parse(
                                        "geo:${restaurant.latitude},${restaurant.longitude}?q=${restaurant.latitude},${restaurant.longitude}"
                                    )
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📍 Voir sur Google Maps")
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Commandes", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${restaurant.orderCount}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Achats (app)", style = MaterialTheme.typography.labelMedium)
                            Text(
                                String.format("%.0f DH", data.totalPurchases),
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

            if (data.orders.isEmpty()) {
                item { Text("Aucune commande passée depuis l'application pour le moment.") }
            } else {
                items(data.orders, key = { it.id }) { order ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Commande #${order.id}", fontWeight = FontWeight.SemiBold)
                                Text(
                                    runCatching { com.takeawaypro.app.OrderStatus.valueOf(order.status).label }
                                        .getOrDefault(order.status),
                                    style = MaterialTheme.typography.bodySmall
                                )
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
