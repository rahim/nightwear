package im.rah.nightwear

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BloodGlucoseTest {
    @Test
    fun testParseTabSeparatedCurrent() {
        val exampleResponse = "2019-01-07T21:20:50.000Z	1546896050000\t109\tFlat\tshare2"

        val bloodGlucose = BloodGlucose.parseTabSeparatedCurrent(exampleResponse)

        assertThat(bloodGlucose?.glucoseLevel_mgdl).isEqualTo(109)
        assertThat(bloodGlucose?.direction).isEqualTo(BloodGlucose.Direction.Flat)
        assertThat(bloodGlucose?.sensorTime).isEqualTo(1546896050000)
    }

    @Test
    fun testParseTabSeparatedCurrentWithQuoteWrapping() {
        val exampleResponse = "2019-01-07T21:20:50.000Z	1546896050000\t109\t\"Flat\"\t\"share2\""

        val bloodGlucose = BloodGlucose.parseTabSeparatedCurrent(exampleResponse)

        assertThat(bloodGlucose?.glucoseLevel_mgdl).isEqualTo(109)
        assertThat(bloodGlucose?.direction).isEqualTo(BloodGlucose.Direction.Flat)
        assertThat(bloodGlucose?.sensorTime).isEqualTo(1546896050000)
    }

}