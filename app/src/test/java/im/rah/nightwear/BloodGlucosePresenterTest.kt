package im.rah.nightwear

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

class BloodGlucosePresenterTest {

    @Test
    fun `#combinedString mgdl Flat`() {
        val bg = BloodGlucose(42, 1546896050000, BloodGlucose.Direction.Flat)
        assertThat(
            BloodGlucosePresenter(bg, false).combinedString()
        ).isEqualTo("42 ➡")
    }

    @Test
    fun `#combinedString mgdl FortyFiveUp`() {
        val bg = BloodGlucose(42, 1546896050000, BloodGlucose.Direction.FortyFiveUp)
        assertThat(
            BloodGlucosePresenter(bg, false).combinedString()
        ).isEqualTo("42 ⬈")
    }

    @Test
    fun `#combinedString mgdl DoubleUp`() {
        val bg = BloodGlucose(42, 1546896050000, BloodGlucose.Direction.DoubleUp)
        assertThat(
            BloodGlucosePresenter(bg, false).combinedString()
        ).isEqualTo("42 ⬆⬆")
    }

    @Test
    fun `#combinedString mgdl DoubleUp safer unicode`() {
        val bg = BloodGlucose(42, 1546896050000, BloodGlucose.Direction.DoubleUp)
        assertThat(
            BloodGlucosePresenter(bg, mmol = false, saferUnicode = true).combinedString()
        ).isEqualTo("42 ⇧⇧")
    }

    @Test
    fun `#combinedString mmol`() {
        val mgdl = (4.2* BloodGlucose.MMOLL_TO_MGDL).roundToInt()
        val bg = BloodGlucose(mgdl, 1546896050000, BloodGlucose.Direction.Flat)
        assertThat(
            BloodGlucosePresenter(bg, true).combinedString()
        ).isEqualTo("4.2 ➡")
    }

    @Test
    fun `#combinedString mgdl mark old, older than 11 minutes`() {
        val timestamp = Instant.now().minus(Duration.ofMinutes(15)).toEpochMilli()
        val bg = BloodGlucose(42, timestamp, BloodGlucose.Direction.Flat)
        assertThat(
            BloodGlucosePresenter(bg, false, markOld = true).combinedString()
        ).isEqualTo("42 OLD")
    }

    @Test
    fun `#combinedString mgdl mark old, newer than 11 minutes`() {
        val timestamp = Instant.now().minus(Duration.ofMinutes(5)).toEpochMilli()
        val bg = BloodGlucose(42, timestamp, BloodGlucose.Direction.Flat)
        assertThat(
            BloodGlucosePresenter(bg, false, markOld = true).combinedString()
        ).isEqualTo("42 ➡")
    }
}