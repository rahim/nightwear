package im.rah.nightwear

import java.text.DecimalFormat

class BloodGlucoseDeltaPresenter(private val bgDelta: BloodGlucoseDelta,
                                 private val mmol: Boolean = true,
                                 private val showUnits: Boolean = true) {
    override fun toString(): String {
        return if (mmol) {
            prefix() + DecimalFormat("0.0").format(bgDelta.delta_mgdl / BloodGlucose.MMOLL_TO_MGDL) + postfix()
        }
        else {
            prefix() + bgDelta.delta_mgdl.toString() + postfix()
        }
    }

    private fun prefix(): String {
        return when {
            bgDelta.delta_mgdl > 0 -> { "+" }
            else -> { "" }
        }
    }

    private fun postfix(): String {
        if (!showUnits) return ""

        return if (mmol) " " + BloodGlucose.Unit.MMOL.label
        else " " + BloodGlucose.Unit.MGDL.label
    }
}