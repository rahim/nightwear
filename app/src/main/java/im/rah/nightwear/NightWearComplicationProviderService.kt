package im.rah.nightwear

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.preference.PreferenceManager
import android.support.wearable.complications.*
import android.util.Log

class NightWearComplicationProviderService : ComplicationProviderService() {
    companion object {
        const val TAG: String = "NightWearComplicationProviderService"
        val SUPPORTED_TYPED: Array<Int> = arrayOf(
            ComplicationData.TYPE_SHORT_TEXT,
            ComplicationData.TYPE_LONG_TEXT
        )
    }

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        Log.d(TAG, "onComplicationUpdate")

        val bg: BloodGlucose
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val mmol = prefs.getBoolean("mmol", true)

        if (!SUPPORTED_TYPED.contains(type)) {
            Log.d(TAG, "unsupported complication data type requested")
            manager.noUpdateRequired(complicationId)
            return
        }

        if (bloodGlucoseService.latestBg == null) {
            Log.d(TAG, "no bg available")
            manager.noUpdateRequired(complicationId)
            return
        } else {
            bg = bloodGlucoseService.latestBg!!
        }

        val builder = ComplicationData.Builder(type)
        
        if (type == ComplicationData.TYPE_SHORT_TEXT) {
            val bgText: String = BloodGlucosePresenter(bg).combinedString(mmol = mmol, markOld = true, saferUnicode = true)

            Log.d(TAG, "updating complication data (SHORT_TEXT), bgText: " + bgText)

            builder.setShortText(
                ComplicationText.plainText(
                    bgText
                )
            ).setShortTitle(
                ComplicationText.TimeDifferenceBuilder()
                    .setSurroundingText("(^1)")
                    .setReferencePeriodStart(bg.sensorTime)
                    .setReferencePeriodEnd(bg.sensorTime)
                    .setShowNowText(false)
                    .build()
            )
        }

        if (type == ComplicationData.TYPE_LONG_TEXT) {
            val bgText: String = BloodGlucosePresenter(bg).combinedString(mmol = mmol, markOld = false, saferUnicode = true) + " (^1)" // the ^1 is interpolated with the age

            Log.d(TAG, "updating complication data (LONG_TEXT), bgText: " + bgText)

            builder.setLongText(
                ComplicationText.TimeDifferenceBuilder()
                    .setSurroundingText(bgText)
                    .setReferencePeriodStart(bg.sensorTime)
                    .setReferencePeriodEnd(bg.sensorTime)
                    .setShowNowText(false)
                    .build()
            ).setIcon(
                Icon.createWithResource(
                    this, R.drawable.baseline_invert_colors_white_18dp
                )
            )
        }

        val data: ComplicationData = builder.build()
        manager.updateComplicationData(complicationId, data)
    }

    private val bloodGlucoseService get() = BloodGlucoseService.getInstance(this.applicationContext)
}