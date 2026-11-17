package dz.univ.alger1.heartcareai.ble

import java.util.Locale

/**
 * Catalogue indicatif. Une photo ou un nom ne prouvent JAMAIS qu'un appareil est présent ou connecté.
 * La compatibilité effective est vérifiée par la présence du service BLE 0x180D et de 0x2A37.
 */
data class BleDeviceModel(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val photoResName: String,
    val tokens: List<String>
)

object BleDeviceCatalog {
    val models = listOf(
        BleDeviceModel(
            id = "smart_watch",
            name = "Montre intelligente",
            category = "Montre connectée · Bluetooth",
            description = "Montre connectée compatible avec la diffusion Bluetooth de la fréquence cardiaque.",
            photoResName = "ble_smartwatch",
            tokens = listOf("WATCH", "GALAXY WATCH", "APPLE WATCH", "HUAWEI WATCH", "GARMIN", "FITBIT", "MI WATCH")
        ),
        BleDeviceModel(
            id = "optical_sensor",
            name = "Capteur optique",
            category = "Capteur au bras / brassard",
            description = "Capteur optique porté au bras pour mesurer le pouls en temps réel.",
            photoResName = "ble_optical_sensor",
            tokens = listOf("OH1", "OH1 ", "VERITY", "SCOSCHE", "OPTICAL")
        ),
        BleDeviceModel(
            id = "chest_strap",
            name = "Ceinture cardiaque",
            category = "Ceinture thoracique · BLE",
            description = "Ceinture thoracique compatible BLE pour les mesures cardiaques plus stables.",
            photoResName = "ble_chest_strap",
            tokens = listOf("H10", "POLAR H10", "TICKR", "HRM", "CHEST", "HEART RATE")
        )
    )

    fun match(name: String?): BleDeviceModel? {
        if (name.isNullOrBlank()) return null
        val canonicalName = name.uppercase(Locale.ROOT)
        return models.firstOrNull { model -> model.tokens.any(canonicalName::contains) }
    }
}

data class DiscoveredBleDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val advertisesHeartRate: Boolean
)

/** Standard BLE Heart Rate Measurement 0x2A37, flags bit 0 : 8/16 bits. */
object BleHeartRateDecoder {
    fun decode(value: ByteArray): Int? {
        if (value.size < 2) return null
        val sixteenBit = (value[0].toInt() and 1) != 0
        if (sixteenBit && value.size < 3) return null
        val pulse = if (sixteenBit)
            (value[1].toInt() and 0xff) or ((value[2].toInt() and 0xff) shl 8)
        else (value[1].toInt() and 0xff)
        return pulse.takeIf { it in 25..240 }
    }
}
