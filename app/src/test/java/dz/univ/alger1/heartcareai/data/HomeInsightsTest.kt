package dz.univ.alger1.heartcareai.data

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class HomeInsightsTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private fun utcDate(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance(utc).apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.timeInMillis

    @Test fun `daily status follows local midnight`() {
        val now = utcDate(2026, 11, 17, 12)
        assertTrue(HomeInsights.isToday(utcDate(2026, 11, 17, 0), now, utc))
        assertFalse(HomeInsights.isToday(utcDate(2026, 11, 16, 23), now, utc))
        assertFalse(HomeInsights.isToday(utcDate(2026, 11, 18, 0), now, utc))
    }

    @Test fun `curve shows actual latest seven observations in chronological order`() {
        val data = (0L..9L).reversed().map { HomePoint(it, it.toInt() + 70) }
        assertEquals((3L..9L).toList(), HomeInsights.lastPoints(data).map { it.at })
        assertTrue(HomeInsights.lastPoints(emptyList()).isEmpty())
        assertEquals(1, HomeInsights.lastPoints(listOf(HomePoint(5L, 100))).size)
    }

    @Test fun `absence of bilan yields generic education`() {
        val tips = HomeInsights.education(null)
        assertTrue(tips.isNotEmpty())
        assertTrue(tips.none { it.needsAttention })
    }

    @Test fun `exercise chest symptoms prompt professional evaluation not clearance`() {
        val tips = HomeInsights.education(HomeSnapshot(145, 200, true, true))
        assertTrue(tips.any { it.needsAttention && it.title.contains("effort") })
        assertTrue(tips.none { it.message.contains("vous êtes en bonne santé", true) })
        assertTrue(tips.size <= 3)
    }
}
