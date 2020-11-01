package im.rah.nightwear

import java.text.DecimalFormat

class BloodGlucosePresenter(private val bg: BloodGlucose,
                            private val mmol: Boolean = true,
                            private val markOld: Boolean = false,
                            private val saferUnicode: Boolean = false) {
    private fun glucose(): String {
        return if (mmol) {
            DecimalFormat("##.0").format(bg.glucoseLevel_mgdl / BloodGlucose.MMOLL_TO_MGDL)
        }
        else {
            bg.glucoseLevel_mgdl.toString()
        }
    }
    private fun directionLabel() =
        if (saferUnicode) bg.direction.saferLabel else bg.direction.bolderLabel
    private fun annotation(): String {
        return when {
            markOld && bg.readingAge() > BloodGlucose.OLD_READING_THRESHOLD -> "OLD"
            else -> directionLabel()
        }
    }
    fun combinedString() = glucose() + " " + annotation()
}