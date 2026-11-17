package dz.univ.alger1.heartcareai.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/** Le statut « Connecté » ne devient vrai qu'après abonnement GATT valide aux notifications 0x2A37. */
data class SensorState(
    val message: String = "Aucun appareil connecté",
    val deviceName: String? = null,
    val deviceAddress: String? = null,
    val connectingAddress: String? = null,
    val pulse: Int? = null,
    val connected: Boolean = false,
    val scanning: Boolean = false,
    val discoveredDevices: List<DiscoveredBleDevice> = emptyList()
)

class HeartRateBleManager(private val context: Context) {
    companion object {
        private val HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        private val HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        private val CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private const val SCAN_DURATION = 15_000L
    }

    private val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val handler = Handler(Looper.getMainLooper())
    private var scanner: BluetoothLeScanner? = null
    private var gatt: BluetoothGatt? = null
    private val discovered = LinkedHashMap<String, Pair<BluetoothDevice, DiscoveredBleDevice>>()
    private val _state = MutableStateFlow(SensorState())
    val state: StateFlow<SensorState> = _state
    private var isScanning = false
    private var scanGeneration = 0

    private fun snapshot(): List<DiscoveredBleDevice> = discovered.values.map { it.second }
        .sortedByDescending { it.rssi }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!isScanning) return
            val device = result.device ?: return
            val address = try { device.address } catch (_: SecurityException) { return }
            val name = try {
                device.name ?: result.scanRecord?.deviceName ?: ""
            } catch (_: SecurityException) { result.scanRecord?.deviceName.orEmpty() }
            val advertised = result.scanRecord?.serviceUuids?.any { it.uuid == HEART_RATE_SERVICE } == true
            // Éviter de lister télévisions et appareils Bluetooth sans rapport avec la santé.
            val interesting = advertised || BleDeviceCatalog.match(name) != null ||
                name.contains("heart", true) || name.contains("hrm", true) ||
                name.contains("cardio", true)
            if (!interesting) return
            discovered[address] = device to DiscoveredBleDevice(
                address = address, name = name.ifBlank { "Capteur cardiaque BLE" },
                rssi = result.rssi, advertisesHeartRate = advertised
            )
            _state.value = _state.value.copy(
                discoveredDevices = snapshot(),
                message = "${discovered.size} appareil(s) détecté(s). Choisissez celui à connecter."
            )
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            _state.value = _state.value.copy(
                scanning = false, message = "Échec de recherche Bluetooth (code $errorCode)."
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (g !== gatt) {
                if (gatt == null && _state.value.connectingAddress != null) gatt = g
                else { try { g.close() } catch (_: Exception) { }; return }
            }
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                _state.value = _state.value.copy(message = "Connexion établie, vérification du service cardiaque…")
                if (!g.discoverServices()) closeGatt(g, "Découverte des services impossible.")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                closeGatt(g, if (status == BluetoothGatt.GATT_SUCCESS) "Appareil déconnecté."
                else "Connexion interrompue (code $status).")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (g !== gatt) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                closeGatt(g, "Impossible de lire les services Bluetooth.")
                return
            }
            val characteristic = g.getService(HEART_RATE_SERVICE)?.getCharacteristic(HEART_RATE_MEASUREMENT)
            if (characteristic == null) {
                closeGatt(g, "Appareil détecté mais profil BLE cardio standard indisponible (0x180D/0x2A37).")
                return
            }
            val descriptor = characteristic.getDescriptor(CLIENT_CONFIG)
            if (descriptor == null || !g.setCharacteristicNotification(characteristic, true)) {
                closeGatt(g, "Notifications cardiaques non prises en charge.")
                return
            }
            try {
                val requested = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    g.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == BluetoothStatusCodes.SUCCESS
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(descriptor)
                }
                if (!requested) closeGatt(g, "Impossible d'activer les notifications cardiaques.")
                else _state.value = _state.value.copy(message = "Activation des mesures du pouls…")
            } catch (_: SecurityException) {
                closeGatt(g, "Permission Bluetooth manquante.")
            }
        }

        override fun onDescriptorWrite(g: BluetoothGatt, d: BluetoothGattDescriptor, status: Int) {
            if (g !== gatt || d.uuid != CLIENT_CONFIG) return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _state.value = _state.value.copy(
                    connected = true, connectingAddress = null,
                    message = "Capteur connecté, attente du premier pouls…"
                )
            } else closeGatt(g, "Notifications refusées (code $status).")
        }

        @Deprecated("Compatibilité Android 12 et inférieur")
        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic) {
            if (g !== gatt || c.uuid != HEART_RATE_MEASUREMENT) return
            @Suppress("DEPRECATION")
            val bytes = c.value ?: return
            receive(bytes)
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic, value: ByteArray) {
            if (g === gatt && c.uuid == HEART_RATE_MEASUREMENT) receive(value)
        }
    }

    private fun receive(bytes: ByteArray) {
        val pulse = BleHeartRateDecoder.decode(bytes) ?: return
        if (_state.value.connected) {
            _state.value = _state.value.copy(pulse = pulse, message = "Mesure de pouls reçue en direct.")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (_state.value.connected || _state.value.connectingAddress != null) {
            _state.value = _state.value.copy(message = "Déconnectez le capteur actuel avant une nouvelle recherche.")
            return
        }
        stopScanOnly()
        discovered.clear()
        val adapter: BluetoothAdapter = manager.adapter ?: run {
            _state.value = SensorState(message = "Bluetooth non disponible sur ce téléphone.")
            return
        }
        if (!adapter.isEnabled) {
            _state.value = SensorState(message = "Activez le Bluetooth du téléphone, puis relancez la recherche.")
            return
        }
        try {
            val targetScanner = adapter.bluetoothLeScanner ?: run {
                _state.value = SensorState(message = "Recherche BLE non disponible.")
                return
            }
            scanner = targetScanner
            isScanning = true
            val currentGeneration = ++scanGeneration
            _state.value = SensorState(scanning = true, message = "Recherche en cours (15 secondes)…")
            // Recherche non filtrée : certains appareils omettent 0x180D dans leurs annonces.
            // La liste visible ne retient que des noms cardiaques ou l'annonce 0x180D.
            targetScanner.startScan(null,
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
            handler.postDelayed({
                if (isScanning && currentGeneration == scanGeneration) {
                    stopScanOnly()
                    val count = discovered.size
                    _state.value = _state.value.copy(scanning = false,
                        message = if (count == 0)
                            "Aucun capteur cardiaque détecté. Réveillez-le et réessayez."
                        else "$count appareil(s) trouvé(s) : choisissez Connecter.")
                }
            }, SCAN_DURATION)
        } catch (_: SecurityException) {
            isScanning = false
            _state.value = SensorState(message = "Autorisation Bluetooth manquante.")
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        val (device, found) = discovered[address] ?: run {
            _state.value = _state.value.copy(message = "Appareil introuvable. Relancez la recherche.")
            return
        }
        stopScanOnly()
        if (gatt != null) {
            _state.value = _state.value.copy(message = "Déconnectez d'abord le capteur actuel.")
            return
        }
        _state.value = _state.value.copy(
            scanning = false, connected = false, pulse = null,
            deviceName = found.name, deviceAddress = address, connectingAddress = address,
            message = "Connexion à ${found.name}…"
        )
        try {
            val newGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            gatt = newGatt
            if (newGatt == null) {
                _state.value = _state.value.copy(message = "Connexion impossible.", connectingAddress = null)
            }
        } catch (_: SecurityException) {
            _state.value = _state.value.copy(message = "Autorisation Bluetooth manquante.", connectingAddress = null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt(g: BluetoothGatt, reason: String) {
        if (g === gatt) gatt = null
        try { g.disconnect() } catch (_: Exception) { }
        try { g.close() } catch (_: Exception) { }
        _state.value = _state.value.copy(
            connected = false, connectingAddress = null, deviceAddress = null,
            deviceName = null, pulse = null, message = reason
        )
    }

    @SuppressLint("MissingPermission")
    private fun stopScanOnly() {
        if (isScanning) {
            try { scanner?.stopScan(scanCallback) } catch (_: Exception) { }
        }
        isScanning = false
        _state.value = _state.value.copy(scanning = false)
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        stopScanOnly()
        val oldGatt = gatt
        gatt = null
        if (oldGatt != null) {
            try { oldGatt.disconnect() } catch (_: Exception) { }
            try { oldGatt.close() } catch (_: Exception) { }
        }
        _state.value = SensorState(message = "Capteur déconnecté.", discoveredDevices = snapshot())
    }
}
