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
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMgdl(42), mmol = false).toString()
        ).isEqualTo("+42 mg/dL")
    }

    @Test
    fun `mgdl negative`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMgdl(-42), mmol = false).toString()
        ).isEqualTo("-42 mg/dL")
    }

    @Test
    fun `mgdl zero`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMgdl(0), mmol = false).toString()
        ).isEqualTo("+0 mg/dL")
    }

    @Test
    fun `mgdl units hidden`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMgdl(42), mmol = false, showUnits = false).toString()
        ).isEqualTo("+42")
    }

    // mmol/L ---------------------

    @Test
    fun `mmol positive more than 1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMmol(2.3),true).toString()
        ).isEqualTo("+2.3 mmol/L")
    }

    @Test
    fun `mmol positive smaller than 1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMmol(0.1),true).toString()
        ).isEqualTo("+0.1 mmol/L")
    }

    @Test
    fun `mmol negative greater than 1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMmol(-2.3),true).toString()
        ).isEqualTo("-2.3 mmol/L")
    }

    @Test
    fun `mmol negative smaller than -1`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMmol(-0.1),true).toString()
        ).isEqualTo("-0.1 mmol/L")
    }

    @Test
    fun `mmol zero shows plus sign`() {
        assertThat(
            BloodGlucoseDeltaPresenter(fiveMinDeltaInMmol(0.0),true).toString()
        ).isEqualTo("+0.0 mmol/L")
    }

    // delta over non standard time span

    @Test
    fun `mmol more than 7 min time difference between sensor times`() {
        assertThat(
            BloodGlucoseDeltaPresenter(tenMinDeltaInMmol(2.3),true).toString()
        ).isEqualTo("+? mmol/L")
    }

    @Test
    fun `mgdl more than 7 min time difference between sensor times`() {
        assertThat(
            BloodGlucoseDeltaPresenter(tenMinDeltaInMgdl(42),false).toString()
        ).isEqualTo("+? mg/dL")
    }

    // -------------------------------------------------------------

    private fun bgInMgdl(mgdl : Int, sensorTime : Long) : BloodGlucose =
        BloodGlucose(mgdl, direction = BloodGlucose.Direction.NONE, sensorTime = sensorTime)

    private fun bgInMmol(mmol : Double, sensorTime : Long) : BloodGlucose {
        val mgdl = (mmol*MMOLL_TO_MGDL).roundToInt()
        return BloodGlucose(mgdl, direction = BloodGlucose.Direction.NONE, sensorTime = sensorTime)
    }

    private fun fiveMinDeltaInMgdl(mgdl : Int) : BloodGlucoseDelta {
        // These are pulled from real data, it's important to note that they're ot exactly
        // 5 minutes in milliseconds, just /very/ close
        val time1 = 1652650868916
        val time2 = 1652651168692
        return BloodGlucoseDelta(bgInMgdl(0, time1), bgInMgdl(0 + mgdl, time2))
    }

    private fun fiveMinDeltaInMmol(mmol : Double) : BloodGlucoseDelta {
        val time1 = 1652650868916
        val time2 = 1652651168692
        return BloodGlucoseDelta(bgInMmol(0.0, time1), bgInMmol(0.0 + mmol, time2))
    }

    private fun tenMinDeltaInMgdl(mgdl : Int) : BloodGlucoseDelta {
        // also taken from real data
        val time1 = 1651862694422
        val time2 = 1651863294490
        return BloodGlucoseDelta(bgInMgdl(0, time1), bgInMgdl(0 + mgdl, time2))
    }

    private fun tenMinDeltaInMmol(mmol : Double) : BloodGlucoseDelta {
        val time1 = 1651862694422
        val time2 = 1651863294490
        return BloodGlucoseDelta(bgInMmol(0.0, time1), bgInMmol(0.0 + mmol, time2))
    }
}