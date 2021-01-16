package im.rah.nightwear

class BloodGlucoseDelta(val delta_mgdl: Int) {
    companion object {
        fun between(first: BloodGlucose, second: BloodGlucose): BloodGlucoseDelta {
            return BloodGlucoseDelta(second.glucoseLevel_mgdl - first.glucoseLevel_mgdl)
        }
    }
}