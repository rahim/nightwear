package im.rah.nightwear

class BloodGlucosePresenter(private val bg: BloodGlucose,
                            private val mmol: Boolean = true,
                            private val markOld: Boolean = false,
                            private val saferUnicode: Boolean = false) {
    private fun glucose() = BloodGlucose.glucose(bg.glucoseLevel_mgdl, mmol)
    private fun directionLabel() =
        if (saferUnicode) bg.direction.saferLabel else bg.direction.bolderLabel
    private fun annotation() : String {
        return when {
            markOld && bg.readingAge() > BloodGlucose.OLD_READING_THRESHOLD -> "OLD"
            else -> directionLabel()
        }
    }
    fun combinedString() = glucose() + " " + annotation()
}