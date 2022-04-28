package im.rah.nightwear

import java.text.DecimalFormat

// Initially this was modelled, storing a single mg/dL difference, but this leads to rounding
// inconsistencies when displaying in mmol/L - instead we have to calculate the difference in the
// display units.
class BloodGlucoseDelta(val first: BloodGlucose, val second: BloodGlucose) {
    fun in_mgdl() = second.glucoseLevel_mgdl - first.glucoseLevel_mgdl
    fun in_mmol() = rounded_mmol(second) - rounded_mmol(first)

    private fun rounded_mmol(bg: BloodGlucose): Double {
        val mmol : Double = bg.glucoseLevel_mgdl / BloodGlucose.MMOLL_TO_MGDL
        return DecimalFormat("#.#").format(mmol).toDouble()
    }
}