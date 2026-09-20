package com.takeawaypro.app.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.takeawaypro.app.AppData
import com.takeawaypro.app.sampleCategories
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductFormScreen(
    appData: AppData,
    productId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val existing = productId?.let { id -> appData.adminProducts.firstOrNull { it.id == id } }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: sampleCategories.first()) }
    var price by remember { mutableStateOf(existing?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(existing?.stock?.toString() ?: "") }
    var emoji by remember { mutableStateOf(existing?.emoji ?: "📦") }
    var imageUri by remember { mutableStateOf(existing?.imageUri) }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) imageUri = uri.toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Ajouter un produit" else "Modifier le produit") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = { Text("Nom") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Text("Catégorie", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleCategories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it; error = null },
                label = { Text("Prix (DH)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it; error = null },
                label = { Text("Stock") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("📷 Photo du produit", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    OutlinedButton(onClick = {
                        photoPicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) {
                        Text(if (imageUri == null) "Choisir une photo" else "Changer la photo")
                    }
                    if (imageUri != null) {
                        TextButton(onClick = { imageUri = null }) {
                            Text("Retirer la photo (utiliser l'icône)")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = emoji,
                onValueChange = { if (it.length <= 2) emoji = it },
                label = { Text("Icône de secours (emoji, utilisée si pas de photo)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val priceValue = price.replace(",", ".").toDoubleOrNull()
                    val stockValue = stock.toIntOrNull()
                    if (name.isBlank() || priceValue == null || stockValue == null) {
                        error = "Merci de remplir le nom, un prix et un stock valides."
                    } else {
                        isSaving = true
                        error = null
                        val finalEmoji = emoji.ifBlank { "📦" }
                        val finalDescription = description.ifBlank { null }
                        scope.launch {
                            val result = if (existing == null) {
                                appData.addProduct(name, category, priceValue, finalEmoji, finalDescription, stockValue)
                            } else {
                                appData.updateProduct(existing.id, name, category, priceValue, finalEmoji, finalDescription, stockValue)
                            }
                            isSaving = false
                            result.onSuccess { savedProduct ->
                                if (imageUri != null) {
                                    val idx = appData.adminProducts.indexOfFirst { it.id == savedProduct.id }
                                    if (idx >= 0) appData.adminProducts[idx] = appData.adminProducts[idx].copy(imageUri = imageUri)
                                }
                                onSaved()
                            }.onFailure {
                                error = it.message ?: "Impossible d'enregistrer le produit."
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enregistrer")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
