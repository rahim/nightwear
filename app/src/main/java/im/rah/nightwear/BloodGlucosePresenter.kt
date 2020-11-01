package im.rah.nightwear

class BloodGlucosePresenter(private val bg: BloodGlucose, private val mmol: Boolean = true) {
    fun glucose() = BloodGlucose.glucose(bg.glucoseLevel_mgdl, mmol)
    fun directionLabel(saferUnicode: Boolean = false) =
        if (saferUnicode) bg.direction.saferLabel else bg.direction.bolderLabel
    fun annotation(markOld: Boolean, saferUnicode: Boolean = false) : String {
        return when {
            markOld && bg.readingAge() > BloodGlucose.OLD_READING_THRESHOLD -> "OLD"
            else -> directionLabel(saferUnicode)
        }
    }
    fun combinedString(markOld: Boolean = false, saferUnicode: Boolean = false) =
        glucose() + " " + annotation(markOld, saferUnicode)
}