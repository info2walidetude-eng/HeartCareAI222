package dz.univ.alger1.heartcareai.ble

import org.junit.Assert.*
import org.junit.Test

class BleDeviceCatalogTest {
    @Test fun `trois modèles sont documentés`() {
        assertEquals(3, BleDeviceCatalog.models.size)
        assertEquals(3, BleDeviceCatalog.models.map { it.id }.distinct().size)
        assertTrue(BleDeviceCatalog.models.all { it.photoResName.startsWith("ble_") })
    }

    @Test fun `les noms publics sont reconnus`() {
        assertEquals("chest_strap", BleDeviceCatalog.match("Polar H10 83DDFE")?.id)
        assertEquals("optical_sensor", BleDeviceCatalog.match("Polar OH1 123456")?.id)
        assertEquals("chest_strap", BleDeviceCatalog.match("TICKR 7BCE")?.id)
        assertEquals("smart_watch", BleDeviceCatalog.match("Galaxy Watch 7")?.id)
        assertNull(BleDeviceCatalog.match("TV-Salon"))
    }

    @Test fun `les paquets 8 et 16 bits sont correctement décodés`() {
        assertEquals(72, BleHeartRateDecoder.decode(byteArrayOf(0, 72)))
        assertEquals(145, BleHeartRateDecoder.decode(byteArrayOf(1, -111, 0)))
        assertNull(BleHeartRateDecoder.decode(byteArrayOf(1, 42)))
        assertNull(BleHeartRateDecoder.decode(byteArrayOf(0, 3)))
        assertNull(BleHeartRateDecoder.decode(byteArrayOf()))
    }
}
