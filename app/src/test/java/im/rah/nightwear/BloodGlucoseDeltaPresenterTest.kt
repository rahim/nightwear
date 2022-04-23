package im.rah.nightwear

import com.google.common.truth.Truth.assertThat
import im.rah.nightwear.BloodGlucose.Companion.MMOLL_TO_MGDL
import org.junit.Test
import kotlin.math.roundToInt

class BloodGlucoseDeltaPresenterTest {

    // mg/dL ---------------------

    @Test
    fun `mgdl positive`() {
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(42),false).toString()
        ).isEqualTo("+42")
    }

    @Test
    fun `mgdl negative`() {
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(-42),false).toString()
        ).isEqualTo("-42")
    }

    @Test
    fun `mgdl zero`() {
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(0),false).toString()
        ).isEqualTo("0")
    }

    // mmol/L ---------------------

    @Test
    fun `mmol positive more than 1`() {
        val mgdl = (2.3*MMOLL_TO_MGDL).roundToInt()
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(mgdl),true).toString()
        ).isEqualTo("+2.3")
    }

    @Test
    fun `mmol positive smaller than 1`() {
        val mgdl = (0.1*MMOLL_TO_MGDL).roundToInt()
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(mgdl),true).toString()
        ).isEqualTo("+0.1")
    }

    @Test
    fun `mmol negative greater than 1`() {
        val mgdl = (-2.3*MMOLL_TO_MGDL).roundToInt()
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(mgdl),true).toString()
        ).isEqualTo("-2.3")
    }

    @Test
    fun `mmol negative smaller than -1`() {
        val mgdl = (-0.1*MMOLL_TO_MGDL).roundToInt()
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(mgdl),true).toString()
        ).isEqualTo("-0.1")
    }

    @Test
    fun `mmol zero has no sign`() {
        assertThat(
            BloodGlucoseDeltaPresenter(BloodGlucoseDelta(0),true).toString()
        ).isEqualTo("0.0")
    }

    // With decimal mg/dL values there would be an edge case where a non-zero value converted to
    // 0.0mmol/L, but would still show a sign. We don't need to worry about that because 1mg/dL
    // rounds to 0.1mmol/L.
}