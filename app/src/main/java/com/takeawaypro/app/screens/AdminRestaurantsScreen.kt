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
fun AdminRestaurantsScreen(
    appData: AppData,
    onBack: () -> Unit,
    onOpenRestaurant: (Int) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        appData.loadRestaurants().onFailure { error = it.message ?: "Impossible de charger les établissements." }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurants & Cafés") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        if (isLoading && appData.restaurants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (error != null && appData.restaurants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(appData.restaurants.toList(), key = { it.id }) { restaurant ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "${restaurant.businessType.emoji} ${restaurant.businessName}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(restaurant.city, style = MaterialTheme.typography.bodySmall)
                            Text(restaurant.phone, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${restaurant.orderCount} commandes",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        TextButton(onClick = { onOpenRestaurant(restaurant.id) }) {
                            Text("Ouvrir →")
                        }
                    }
                }
            }
        }
    }
}
