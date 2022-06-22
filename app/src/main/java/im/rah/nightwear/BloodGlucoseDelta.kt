package im.rah.nightwear

import org.threeten.bp.Duration
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

// Initially this was modelled, storing a single mg/dL difference, but this leads to rounding
// inconsistencies when displaying in mmol/L - instead we have to calculate the difference in the
// display units.
class BloodGlucoseDelta(val first: BloodGlucose, val second: BloodGlucose) {
    companion object {
        val MAX_INTERVAL = Duration.ofMinutes(7)
    }

    fun inMgdl() = second.glucoseLevel_mgdl - first.glucoseLevel_mgdl
    fun inMmol() = roundedMmol(second) - roundedMmol(first)

    fun unexpectedInterval(): Boolean {
        return second.sensorTimeInstant().isAfter(first.sensorTimeInstant().plus(MAX_INTERVAL))
    }

    private fun roundedMmol(bg: BloodGlucose): Double {
        val mmol : Double = bg.glucoseLevel_mgdl / BloodGlucose.MMOLL_TO_MGDL
        //return DecimalFormat("#.#").format(mmol).toDouble()
        val bd : BigDecimal = BigDecimal.valueOf(mmol).setScale(1, RoundingMode.HALF_UP)
        return bd.toDouble()
    }
}