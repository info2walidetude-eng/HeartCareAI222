package dz.univ.alger1.heartcareai.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MedicalProfileTest {
    @Test fun acceptsValidRegistrationValues() {
        val profile = MedicalProfile(27, "F", "NAP")
        profile.validate()
        assertEquals(27, profile.age)
    }

    @Test fun rejectsInvalidAge() {
        assertThrows(IllegalArgumentException::class.java) {
            MedicalProfile(0, "M", "ASY").validate()
        }
    }

    @Test fun rejectsUnknownSex() {
        assertThrows(IllegalArgumentException::class.java) {
            MedicalProfile(30, "X", "TA").validate()
        }
    }

    @Test fun rejectsUnknownChestPainType() {
        assertThrows(IllegalArgumentException::class.java) {
            MedicalProfile(40, "M", "UNKNOWN").validate()
        }
    }
}
