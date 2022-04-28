package im.rah.nightwear

import java.text.DecimalFormat

class BloodGlucoseDeltaPresenter(private val bgDelta: BloodGlucoseDelta,
                                 private val mmol: Boolean = true,
                                 private val showUnits: Boolean = true) {
    override fun toString(): String {
        return if (mmol) {
            prefix() + DecimalFormat("0.0").format(bgDelta.in_mmol()) + postfix()
        }
        else {
            prefix() + bgDelta.in_mgdl().toString() + postfix()
        }
    }

    private fun prefix(): String {
        return when {
            (mmol && bgDelta.in_mmol() > 0) || (!mmol && bgDelta.in_mgdl() > 0) -> { "+" }
            else -> { "" }
        }
    }

    private fun postfix(): String {
        if (!showUnits) return ""

        return if (mmol) " " + BloodGlucose.Unit.MMOL.label
        else " " + BloodGlucose.Unit.MGDL.label
    }
}