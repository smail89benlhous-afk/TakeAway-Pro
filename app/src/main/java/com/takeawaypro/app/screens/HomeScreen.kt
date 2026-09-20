package com.takeawaypro.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takeawaypro.app.AppData
import com.takeawaypro.app.Product
import com.takeawaypro.app.sampleCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appData: AppData,
    onOpenCart: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }
    val cartCount by remember { derivedStateOf { appData.cart.sumOf { it.quantity } } }

    LaunchedEffect(retryKey) {
        isLoading = true
        loadError = null
        appData.loadProducts().onFailure { loadError = it.message ?: "Impossible de charger les produits." }
        isLoading = false
    }

    val filtered = appData.adminProducts.filter { product ->
        (selectedCategory == null || product.category == selectedCategory) &&
            product.name.contains(search, ignoreCase = true)
    }
    val popular = appData.adminProducts.filter { it.popular }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bonjour, ${appData.account.value?.businessName?.ifBlank { "Invité" } ?: "Invité"} 👋"
                    )
                },
                actions = {
                    TextButton(onClick = onOpenCart) {
                        Text("🛒 $cartCount")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && appData.adminProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (loadError != null && appData.adminProducts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(loadError!!, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { retryKey++ }) { Text("Réessayer") }
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("🔍 Rechercher un produit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Tous") }
                        )
                    }
                    items(sampleCategories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category) }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            if (selectedCategory == null && search.isBlank()) {
                item {
                    Text(
                        "🔥 Produits populaires",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(popular) { product ->
                            PopularProductCard(
                                product = product,
                                quantity = appData.quantityOf(product),
                                onAdd = { appData.addToCart(product, 1) }
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Tous les produits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            items(filtered) { product ->
                ProductRow(
                    product = product,
                    quantity = appData.quantityOf(product),
                    onQuantityChange = { qty -> appData.setQuantity(product, qty) }
                )
                Spacer(Modifier.height(10.dp))
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PopularProductCard(product: Product, quantity: Int, onAdd: () -> Unit) {
    Card(modifier = Modifier.width(150.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            ProductThumbnail(product, size = 56.dp)
            Spacer(Modifier.height(6.dp))
            Text(product.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                String.format("%.2f DH / unité", product.price),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Text(if (quantity == 0) "Ajouter" else "Ajouté ($quantity)")
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductThumbnail(product, size = 44.dp, modifier = Modifier.padding(end = 10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold)
                Text(
                    String.format("%.2f DH / unité", product.price),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (quantity == 0) {
                Button(onClick = { onQuantityChange(1) }) { Text("Ajouter au panier") }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { onQuantityChange(quantity - 1) }) { Text("-") }
                    Text(
                        "$quantity",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(onClick = { onQuantityChange(quantity + 1) }) { Text("+") }
                }
            }
        }
    }
}
