package im.rah.nightwear

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.roundToInt

class BloodGlucoseDeltaTest {
    @Test
    fun `#inMgdl is a simple subtraction`() {
        val bg1 = BloodGlucose(84, direction = BloodGlucose.Direction.NONE, sensorTime = 0)
        val bg2 = BloodGlucose(42, direction = BloodGlucose.Direction.NONE, sensorTime = 0)

        assertThat(
            BloodGlucoseDelta(bg1, bg2).inMgdl()
        ).isEqualTo(-42)
    }

    @Test
    fun `#inMgdl is the post rounded difference`() {
        val mgdl1 = (4.2* BloodGlucose.MMOLL_TO_MGDL).roundToInt()
        val mgdl2 = (8.4* BloodGlucose.MMOLL_TO_MGDL).roundToInt()
        val bg1 = BloodGlucose(mgdl1, direction = BloodGlucose.Direction.NONE, sensorTime = 0)
        val bg2 = BloodGlucose(mgdl2, direction = BloodGlucose.Direction.NONE, sensorTime = 0)

        assertThat(
            BloodGlucoseDelta(bg1, bg2).inMmol()
        ).isEqualTo(4.2)
    }
}