package im.rah.wearhello

import java.text.DecimalFormat

class BloodGlucose(val glucoseLevel_mgdl : Int, val sensorTime : Long) {
    companion object {
        const val MMOLL_TO_MGDL = 18.0182

        @JvmStatic fun parse_tab_separated_current(str: String) : BloodGlucose {
            // example "2019-01-07T21:20:50.000Z	1546896050000\t109\tFlat\tshare2"
            // ISO8601 datetime with timezone,
            // unix timestamp with milliseconds
            // sensor glucose value in mg/dL
            // direction, see https://github.com/ktind/sgvdata/blob/master/lib/utils.js#L9 for possible values
            val parts = str.split("\t")
            val sensorTime: Long = parts[1].toLong()
            val glucoseLevel = parts[2].toInt()
            return BloodGlucose(glucoseLevel, sensorTime)
        }

        fun glucose(mgdl : Int, mmol : Boolean = true) : String {
            return if (mmol) {
                DecimalFormat("##.0").format(mgdl / BloodGlucose.MMOLL_TO_MGDL)
            }
            else {
                mgdl.toString()
            }
        }
    }

    fun glucose(mmol : Boolean = true) = BloodGlucose.glucose(glucoseLevel_mgdl, mmol)

}