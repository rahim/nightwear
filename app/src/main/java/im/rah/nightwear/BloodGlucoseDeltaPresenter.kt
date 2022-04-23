package im.rah.nightwear

import java.text.DecimalFormat

class BloodGlucoseDeltaPresenter(private val bgDelta: BloodGlucoseDelta,
                                 private val mmol: Boolean = true) {
    override fun toString(): String {
        return if (mmol) {
            prefix() + DecimalFormat("0.0").format(bgDelta.delta_mgdl / BloodGlucose.MMOLL_TO_MGDL)
        }
        else {
            prefix() + bgDelta.delta_mgdl.toString()
        }
    }

    private fun prefix(): String {
        return when {
            bgDelta.delta_mgdl > 0 -> { "+" }
            else -> { "" }
        }
    }
}