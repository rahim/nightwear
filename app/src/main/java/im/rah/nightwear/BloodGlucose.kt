package im.rah.nightwear

import java.lang.Exception
import java.text.DecimalFormat
import java.text.ParseException
import java.time.Duration
import java.time.Instant

class BloodGlucose(val glucoseLevel_mgdl: Int, val sensorTime: Long, val direction: Direction) {
    // see also https://github.com/nightscout/cgm-remote-monitor/blob/11c6086678415883f7d7a110a032bb26a4be8543/lib/plugins/direction.js#L53
    enum class Direction(val label: String) {
        NONE("⇼"),
        TripleUp("⬆⬆⬆"),
        DoubleUp("⬆⬆"),
        SingleUp("⬆"),
        FortyFiveUp( "⬈"),
        Flat("➡"),
        FortyFiveDown("⬊"),
        SingleDown("⬇"),
        DoubleDown("⬇⬇"),
        TripleDown("⬇⬇⬇"),
        NOT_COMPUTABLE("-"),
        RATE_OUT_OF_RANGE("⇕")
    }

    companion object {
        const val MMOLL_TO_MGDL = 18.0182
        val OLD_READING_THRESHOLD = Duration.ofMinutes(11)

        @Throws(ParseException::class)
        @JvmStatic fun parseTabSeparatedCurrent(str: String): BloodGlucose? {
            try {
                // example "2019-01-07T21:20:50.000Z	1546896050000\t109\tFlat\tshare2"
                // ISO8601 datetime with timezone,
                // unix timestamp with milliseconds
                // sensor glucose value in mg/dL
                // direction, see https://github.com/ktind/sgvdata/blob/master/lib/utils.js#L9 for possible values
                // In NightScout 0.11.1 we started seeing non-numeric fields wrapped in double quotes
                val parts = str.split("\t").map { s -> s.removeSurrounding("\"") }
                val sensorTime: Long = parts[1].toLong()
                val glucoseLevel = parts[2].toInt()
                val direction = Direction.valueOf(parts[3].replace(" ","_"))
                return BloodGlucose(glucoseLevel, sensorTime, direction)
            }
            catch (e: Exception) {
                throw ParseException(str, 0)
            }
        }

        fun glucose(mgdl: Int, mmol: Boolean = true): String {
            return if (mmol) {
                DecimalFormat("##.0").format(mgdl / MMOLL_TO_MGDL)
            }
            else {
                mgdl.toString()
            }
        }
    }

    fun glucose(mmol: Boolean = true) = glucose(glucoseLevel_mgdl, mmol)
    fun directionLabel() = direction.label
    fun annotation(markOld: Boolean) : String {
        return when {
            markOld && readingAge() > OLD_READING_THRESHOLD -> "OLD"
            else -> directionLabel()
        }
    }
    fun combinedString(mmol: Boolean = true, markOld: Boolean = false) = glucose(mmol) + " " + annotation(markOld)
    override fun toString() = combinedString()

    fun sensorTimeInstant() = Instant.ofEpochMilli(sensorTime)
    fun readingAge() = Duration.between(sensorTimeInstant(), Instant.now())
}