package com.takeawaypro.app.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.takeawaypro.app.AppData
import com.takeawaypro.app.BusinessType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    appData: AppData,
    onBack: () -> Unit,
    onAccountCreated: () -> Unit
) {
    var businessType by remember { mutableStateOf(BusinessType.CAFE) }
    var businessName by remember { mutableStateOf("") }
    var managerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun captureLocation() {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var best: android.location.Location? = null
        for (provider in providers) {
            val loc = try { locationManager.getLastKnownLocation(provider) } catch (e: SecurityException) { null } ?: continue
            if (best == null || loc.accuracy < best.accuracy) best = loc
        }
        if (best != null) {
            latitude = best.latitude
            longitude = best.longitude
            error = null
        } else {
            error = "Position indisponible. Activez le GPS et réessayez."
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) captureLocation()
        else error = "Autorisation de localisation refusée."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un compte") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←") }
                }
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
            Text("Type d'activité", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BusinessType.entries.toList()) { type ->
                    FilterChip(
                        selected = businessType == type,
                        onClick = { businessType = type },
                        label = { Text("${type.emoji} ${type.label}") }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it; error = null },
                label = { Text("Nom de l'établissement") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = managerName,
                onValueChange = { managerName = it; error = null },
                label = { Text("Nom du responsable") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; error = null },
                label = { Text("Numéro de téléphone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail (optionnel)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Mot de passe") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it; error = null },
                label = { Text("Ville") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; error = null },
                label = { Text("Adresse") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) captureLocation()
                    else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (latitude != null) "📍 Position enregistrée ✓ (modifier)"
                    else "📍 Partager ma position précise (optionnel)"
                )
            }
            if (latitude != null && longitude != null) {
                Spacer(Modifier.height(6.dp))
                TextButton(
                    onClick = {
                        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voir cette position sur Google Maps →")
                }
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (businessName.isBlank() || managerName.isBlank() || phone.isBlank() ||
                        password.isBlank() || city.isBlank() || address.isBlank()
                    ) {
                        error = "Merci de remplir tous les champs obligatoires."
                    } else {
                        isLoading = true
                        error = null
                        scope.launch {
                            val result = appData.register(
                                businessType, businessName, managerName,
                                phone, email, password, city, address,
                                latitude, longitude
                            )
                            isLoading = false
                            result.onSuccess { onAccountCreated() }
                                .onFailure { error = it.message ?: "Une erreur est survenue." }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Créer mon compte")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
