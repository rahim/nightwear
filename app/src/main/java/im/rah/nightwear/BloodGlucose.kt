package im.rah.nightwear

import java.text.DecimalFormat

class BloodGlucose(val glucoseLevel_mgdl : Int, val sensorTime : Long, val direction : Direction) {
    // see also https://github.com/nightscout/cgm-remote-monitor/blob/11c6086678415883f7d7a110a032bb26a4be8543/lib/plugins/direction.js#L53
    enum class Direction(val index : Int, val label : String) {
        NONE(0, "⇼"),
        DoubleUp(1, "⬆⬆"),
        SingleUp(2, "⬆"),
        FortyFiveUp(3, "⬈"),
        Flat(4, "➡"),
        FortyFiveDown(5, "⬊"),
        SingleDown(6, "⬇"),
        DoubleDown(7, "⬇⬇"),
        NOT_COMPUTABLE(8, "-"),
        RATE_OUT_OF_RANGE(9, "⇕")
    }

    companion object {
        const val MMOLL_TO_MGDL = 18.0182

        @JvmStatic fun parse_tab_separated_current(str : String) : BloodGlucose {
            // example "2019-01-07T21:20:50.000Z	1546896050000\t109\tFlat\tshare2"
            // ISO8601 datetime with timezone,
            // unix timestamp with milliseconds
            // sensor glucose value in mg/dL
            // direction, see https://github.com/ktind/sgvdata/blob/master/lib/utils.js#L9 for possible values
            val parts = str.split("\t")
            val sensorTime: Long = parts[1].toLong()
            val glucoseLevel = parts[2].toInt()
            val direction = Direction.valueOf(parts[3])
            return BloodGlucose(glucoseLevel, sensorTime, direction)
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
    fun directionLabel() = direction.label
    override fun toString() = glucose() + " " + directionLabel()
}