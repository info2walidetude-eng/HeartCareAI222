package dz.univ.alger1.heartcareai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dz.univ.alger1.heartcareai.ble.BleDeviceCatalog
import dz.univ.alger1.heartcareai.ble.BleDeviceModel
import dz.univ.alger1.heartcareai.ble.DiscoveredBleDevice
import dz.univ.alger1.heartcareai.ble.SensorState

/**
 * Écran de capteurs : les cartes sont un catalogue, les statuts sont calculés uniquement
 * depuis les événements BLE réels du téléphone. Pas de simulation de connexion.
 */
@Composable
fun SensorScreen(
    modifier: Modifier = Modifier,
    state: SensorState,
    onScan: () -> Unit,
    onConnect: (String) -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    val requested = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    var permissionsError by remember { mutableStateOf(false) }
    val permissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it } && granted.isNotEmpty()) pending?.invoke()
        else permissionsError = true
        pending = null
    }

    fun withBlePermissions(action: () -> Unit) {
        if (requested.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            permissionsError = false
            action()
        } else {
            pending = action
            permissions.launch(requested)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(PageBg).verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Mes capteurs", style = MaterialTheme.typography.headlineMedium, color = Ink)
                Text("Choisissez une montre intelligente, un capteur optique ou une ceinture cardiaque.",
                    style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
            Icon(Icons.Rounded.Bluetooth, null, tint = Mint, modifier = Modifier.size(34.dp))
        }

        Card(
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = Ink)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Favorite, null, tint = Cardio, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(9.dp))
                    Text("Fréquence cardiaque en direct", color = Color.White,
                        style = MaterialTheme.typography.titleMedium)
                }
                Text(state.pulse?.let { "$it bpm" } ?: "— bpm", color = Color.White,
                    style = MaterialTheme.typography.headlineLarge)
                Text(
                    when {
                        state.connected -> "Connecté à ${state.deviceName ?: "un capteur"}"
                        state.connectingAddress != null -> "Connexion en cours à ${state.deviceName.orEmpty()}"
                        else -> "Aucune mesure en direct pour le moment"
                    }, color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(state.message, color = Color.White.copy(alpha = .75f),
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = { withBlePermissions(onScan) },
            enabled = !state.scanning && !state.connected && state.connectingAddress == null,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (state.scanning) CircularProgressIndicator(Modifier.size(18.dp),
                color = Color.White, strokeWidth = 2.dp)
            else Icon(Icons.Rounded.BluetoothSearching, null, Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(if (state.scanning) "Recherche en cours…" else "Rechercher les appareils")
        }
        if (state.connected || state.connectingAddress != null) {
            OutlinedButton(onClick = onStop, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Rounded.BluetoothDisabled, null)
                Spacer(Modifier.width(8.dp))
                Text("Déconnecter", color = Ink)
            }
        }
        if (permissionsError) {
            Text("Autorisez l'accès Bluetooth dans les paramètres du téléphone pour détecter et connecter un capteur.",
                style = MaterialTheme.typography.bodySmall, color = CardioDark)
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Types d'appareils compatibles", color = Ink,
                style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Surface(color = MintSoft, shape = RoundedCornerShape(20.dp)) {
                Text("3 types", Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = Mint, style = MaterialTheme.typography.labelMedium)
            }
        }
        Text("Les photos montrent les principaux types d'appareils compatibles. Le badge CONNECTÉ n'apparaît qu'après une vraie connexion Bluetooth validée.",
            style = MaterialTheme.typography.bodySmall, color = Slate)

        BleDeviceCatalog.models.forEach { model ->
            val available = state.discoveredDevices.firstOrNull {
                it.address == state.deviceAddress && BleDeviceCatalog.match(it.name)?.id == model.id
            } ?: state.discoveredDevices.firstOrNull {
                BleDeviceCatalog.match(it.name)?.id == model.id
            }
            val connected = state.connected && available?.address == state.deviceAddress
            val connecting = state.connectingAddress != null && available?.address == state.connectingAddress
            BleModelCard(model, available, connected, connecting,
                canConnect = !state.scanning && !state.connected && state.connectingAddress == null,
                onConnect = { addr -> withBlePermissions { onConnect(addr) } },
                onStop = onStop)
        }

        val others = state.discoveredDevices.filter { BleDeviceCatalog.match(it.name) == null }
        if (others.isNotEmpty()) {
            Text("Autres appareils cardiaques détectés", color = Ink,
                style = MaterialTheme.typography.titleMedium)
            others.forEach { device ->
                Card(shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium, color = Ink)
                        Text("Signal : ${device.rssi} dBm • Service cardiaque à confirmer",
                            style = MaterialTheme.typography.bodySmall, color = Slate)
                        Text(
                            if (state.connected && state.deviceAddress == device.address) "Connecté" else "Non connecté",
                            color = if (state.connected && state.deviceAddress == device.address) Mint else Slate,
                            style = MaterialTheme.typography.labelMedium
                        )
                        OutlinedButton(
                            onClick = { withBlePermissions { onConnect(device.address) } },
                            enabled = !state.scanning && !state.connected && state.connectingAddress == null
                        ) { Text("Vérifier et connecter") }
                    }
                }
            }
        }

        Card(shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MintSoft)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Avant la connexion", style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("1. Mettez le capteur en marche et portez-le.\n" +
                    "2. Activez le Bluetooth du téléphone.\n" +
                    "3. Lancez la recherche, puis choisissez Connecter sur l'appareil détecté.\n" +
                    "4. Si aucun capteur n'apparaît, vérifiez qu'il n'est pas occupé par une autre application.",
                    style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            }
        }
        Text("Le pouls BLE en direct ne remplace pas MaxHR (fréquence maximale à l'effort) et ne sert pas à établir un diagnostic médical.",
            style = MaterialTheme.typography.bodySmall, color = Slate)
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun BleModelCard(
    model: BleDeviceModel,
    available: DiscoveredBleDevice?,
    connected: Boolean,
    connecting: Boolean,
    canConnect: Boolean,
    onConnect: (String) -> Unit,
    onStop: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                LocalBlePhoto(model.photoResName, model.name)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(model.name, style = MaterialTheme.typography.titleMedium,
                        color = Ink, fontWeight = FontWeight.Bold)
                    Text(model.category, style = MaterialTheme.typography.bodySmall, color = Slate)
                    Surface(color = if (connected) MintSoft else PageBg,
                        shape = RoundedCornerShape(12.dp)) {
                        Text(
                            if (connected) "● Connecté" else "● Non connecté",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = if (connected) Mint else Slate,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Text(model.description, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when {
                        connected -> "Liaison active"
                        connecting -> "Connexion en cours…"
                        available != null -> "Détecté · ${available.rssi} dBm"
                        else -> "Non détecté à proximité"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (connected || available != null) Mint else Slate,
                    modifier = Modifier.weight(1f)
                )
                if (connected) {
                    OutlinedButton(onClick = onStop) { Text("Déconnecter") }
                } else {
                    Button(onClick = { available?.let { onConnect(it.address) } },
                        enabled = available != null && canConnect) {
                        Text(if (connecting) "Connexion…" else "Connecter")
                    }
                }
            }
        }
    }
}

/** Images locales embarquées dans l'application pour un affichage hors ligne. */
@Composable
private fun LocalBlePhoto(photoResName: String, label: String) {
    val context = LocalContext.current
    val resId = remember(photoResName) {
        context.resources.getIdentifier(photoResName, "drawable", context.packageName)
    }
    Box(
        Modifier.size(width = 105.dp, height = 105.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(PageBg)
            .border(1.dp, BorderLight, RoundedCornerShape(17.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Image de " + label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Rounded.Bluetooth, null, tint = Mint, modifier = Modifier.size(30.dp))
                Text(
                    "Image indisponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
