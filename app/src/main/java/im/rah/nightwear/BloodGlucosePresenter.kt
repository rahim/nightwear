package im.rah.nightwear

class BloodGlucosePresenter(private val bg: BloodGlucose, private val mmol: Boolean = true) {
    private fun glucose() = BloodGlucose.glucose(bg.glucoseLevel_mgdl, mmol)
    private fun directionLabel(saferUnicode: Boolean = false) =
        if (saferUnicode) bg.direction.saferLabel else bg.direction.bolderLabel
    private fun annotation(markOld: Boolean, saferUnicode: Boolean = false) : String {
        return when {
            markOld && bg.readingAge() > BloodGlucose.OLD_READING_THRESHOLD -> "OLD"
            else -> directionLabel(saferUnicode)
        }
    }
    fun combinedString(markOld: Boolean = false, saferUnicode: Boolean = false) =
        glucose() + " " + annotation(markOld, saferUnicode)
}