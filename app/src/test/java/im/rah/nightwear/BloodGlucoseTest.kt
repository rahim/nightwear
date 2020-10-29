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

    @Test
    fun testParseTabSeparatedRecent() {
        val exampleResponse =
            """
            "2020-10-26T22:08:21.000Z"	1603750101000	73	"NOT COMPUTABLE"	"share2"
            "2020-10-26T22:03:21.000Z"	1603749801000	67	"NOT COMPUTABLE"	"share2"
            "2020-10-26T21:48:22.000Z"	1603748902000	53	"Flat"	"share2"
            "2020-10-26T21:43:21.000Z"	1603748601000	69	"Flat"	"share2"
            "2020-10-26T21:38:21.000Z"	1603748301000	85	"Flat"	"share2"
            "2020-10-26T21:33:22.000Z"	1603748002000	84	"FortyFiveDown"	"share2"
            "2020-10-26T21:28:21.000Z"	1603747701000	85	"Flat"	"share2"
            "2020-10-26T21:23:21.000Z"	1603747401000	87	"Flat"	"share2"
            "2020-10-26T21:18:21.000Z"	1603747101000	89	"Flat"	"share2"
            "2020-10-26T21:13:21.000Z"	1603746801000	91	"Flat"	"share2"
            """.trimIndent()

        val recentBloodGlucoseArray = BloodGlucose.parseTabSeparatedRecent(exampleResponse)

        assertThat(recentBloodGlucoseArray.size).isEqualTo(10)

        assertThat(recentBloodGlucoseArray.first()?.glucoseLevel_mgdl).isEqualTo(73)
        assertThat(recentBloodGlucoseArray.first()?.direction).isEqualTo(BloodGlucose.Direction.NOT_COMPUTABLE)
        assertThat(recentBloodGlucoseArray.first()?.sensorTime).isEqualTo(1603750101000)
    }
}
