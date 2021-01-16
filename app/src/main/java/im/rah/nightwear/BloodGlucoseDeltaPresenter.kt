package im.rah.nightwear

import java.text.DecimalFormat

class BloodGlucoseDeltaPresenter(private val bgDelta: BloodGlucoseDelta,
                                 private val mmol: Boolean = true) {
    override fun toString(): String {
        return if (mmol) {
            DecimalFormat("##.0").format(bgDelta.delta_mgdl / BloodGlucose.MMOLL_TO_MGDL)
        }
        else {
            bgDelta.delta_mgdl.toString()
        }
    }
}