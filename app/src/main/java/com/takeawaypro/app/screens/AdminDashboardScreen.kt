package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takeawaypro.app.AppData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    appData: AppData,
    onBack: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenRestaurants: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        appData.loadAdminStats()
        appData.loadProducts(includeHidden = true)
        appData.loadRestaurants()
        appData.loadNotifications()
        isLoading = false
    }

    val unreadCount = appData.notifications.count { !it.read }
    val stats = appData.adminStats.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
                actions = {
                    TextButton(onClick = onOpenNotifications) {
                        Text(if (unreadCount > 0) "🔔 $unreadCount" else "🔔")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && stats == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Aujourd'hui", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Commandes", "${stats?.orderCount ?: 0}", Modifier.weight(1f))
                StatCard("Clients", "${stats?.clientCount ?: 0}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Produits", "${stats?.productCount ?: 0}", Modifier.weight(1f))
                StatCard("Chiffre d'affaires", String.format("%.0f DH", stats?.revenue ?: 0.0), Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))
            Text("Gestion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                ListItemRow("📦 Produits", "${appData.adminProducts.size} articles", onOpenProducts)
            }
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItemRow("🏪 Restaurants & Cafés", "${appData.restaurants.size} établissements", onOpenRestaurants)
            }
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItemRow(
                    "🔔 Notifications",
                    if (unreadCount > 0) "$unreadCount non lues" else "Aucune nouvelle notification",
                    onOpenNotifications
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ListItemRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onClick) { Text("Ouvrir →") }
    }
}
