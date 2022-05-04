package im.rah.nightwear

import java.text.DecimalFormat

// Initially this was modelled, storing a single mg/dL difference, but this leads to rounding
// inconsistencies when displaying in mmol/L - instead we have to calculate the difference in the
// display units.
class BloodGlucoseDelta(val first: BloodGlucose, val second: BloodGlucose) {
    fun inMgdl() = second.glucoseLevel_mgdl - first.glucoseLevel_mgdl
    fun inMmol() = roundedMmol(second) - roundedMmol(first)

    private fun roundedMmol(bg: BloodGlucose): Double {
        val mmol : Double = bg.glucoseLevel_mgdl / BloodGlucose.MMOLL_TO_MGDL
        return DecimalFormat("#.#").format(mmol).toDouble()
    }
}