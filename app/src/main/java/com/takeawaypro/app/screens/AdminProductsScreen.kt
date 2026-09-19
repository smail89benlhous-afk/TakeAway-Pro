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
import com.takeawaypro.app.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    appData: AppData,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit
) {
    var hiddenIds by remember { mutableStateOf(setOf<String>()) }
    var productPendingDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produits") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
                actions = {
                    TextButton(onClick = onAddProduct) { Text("➕ Ajouter") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(appData.adminProducts.toList(), key = { it.id }) { product ->
                val hidden = hiddenIds.contains(product.id)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProductThumbnail(product, size = 44.dp, modifier = Modifier.padding(end = 8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    product.name,
                                    fontWeight = FontWeight.SemiBold,
                                    style = if (hidden) MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.outline
                                    ) else MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "${product.category} • ${String.format("%.2f DH", product.price)} • Stock: ${product.stock}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (hidden) {
                                    Text(
                                        "Masqué",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { onEditProduct(product.id) }) {
                                Text("✏️ Modifier")
                            }
                            OutlinedButton(onClick = {
                                hiddenIds = if (hidden) hiddenIds - product.id else hiddenIds + product.id
                            }) {
                                Text(if (hidden) "👁️ Afficher" else "👁️ Masquer")
                            }
                            OutlinedButton(
                                onClick = { productPendingDelete = product },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("🗑️ Supprimer")
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingDelete = null },
            title = { Text("Supprimer le produit ?") },
            text = { Text("« ${product.name} » sera supprimé du catalogue.") },
            confirmButton = {
                TextButton(onClick = {
                    appData.deleteProduct(product.id)
                    productPendingDelete = null
                }) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { productPendingDelete = null }) { Text("Annuler") }
            }
        )
    }
}
