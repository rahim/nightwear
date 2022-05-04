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
            BloodGlucoseDeltaPresenter(deltaInMgdl(42), mmol = false).toString()
        ).isEqualTo("+42 mg/dL")
    }

    @Test
    fun `mgdl negative`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMgdl(-42), mmol = false).toString()
        ).isEqualTo("-42 mg/dL")
    }

    @Test
    fun `mgdl zero`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMgdl(0), mmol = false).toString()
        ).isEqualTo("+0 mg/dL")
    }

    @Test
    fun `mgdl units hidden`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMgdl(42), mmol = false, showUnits = false).toString()
        ).isEqualTo("+42")
    }

    // mmol/L ---------------------

    @Test
    fun `mmol positive more than 1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMmol(2.3),true).toString()
        ).isEqualTo("+2.3 mmol/L")
    }

    @Test
    fun `mmol positive smaller than 1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMmol(0.1),true).toString()
        ).isEqualTo("+0.1 mmol/L")
    }

    @Test
    fun `mmol negative greater than 1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMmol(-2.3),true).toString()
        ).isEqualTo("-2.3 mmol/L")
    }

    @Test
    fun `mmol negative smaller than -1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMmol(-0.1),true).toString()
        ).isEqualTo("-0.1 mmol/L")
    }

    @Test
    fun `mmol zero shows plus sign`() {
        assertThat(
            BloodGlucoseDeltaPresenter(deltaInMmol(0.0),true).toString()
        ).isEqualTo("+0.0 mmol/L")
    }

    private fun bgInMgdl(mgdl : Int) =
        BloodGlucose(mgdl, direction = BloodGlucose.Direction.NONE, sensorTime = 0)

    private fun bgInMmol(mmol : Double) : BloodGlucose {
        val mgdl = (mmol*MMOLL_TO_MGDL).roundToInt()
        return BloodGlucose(mgdl, direction = BloodGlucose.Direction.NONE, sensorTime = 0)
    }

    private fun deltaInMgdl(mgdl : Int) : BloodGlucoseDelta =
        BloodGlucoseDelta(bgInMgdl(0), bgInMgdl(0 + mgdl))

    private fun deltaInMmol(mmol : Double) : BloodGlucoseDelta =
        BloodGlucoseDelta(bgInMmol(0.0), bgInMmol(0.0 + mmol))
}