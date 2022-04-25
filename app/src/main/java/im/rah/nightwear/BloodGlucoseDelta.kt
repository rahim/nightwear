package im.rah.nightwear

// The modelling here is clean and intuitive, but using mg/dL differences will lead to rounding
// inconsistencies for mmol/L with both the NightScout displayed deltas, but also with what we
// we expect from the change observed.
//
// To overcome this we'll need to convert units before calculating the difference.
class BloodGlucoseDelta(val delta_mgdl: Int) {
    companion object {
        fun between(first: BloodGlucose, second: BloodGlucose): BloodGlucoseDelta {
            return BloodGlucoseDelta(second.glucoseLevel_mgdl - first.glucoseLevel_mgdl)
        }
    }
}