package im.rah.nightwear

import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.util.Log

class NightWearComplicationProviderService : ComplicationProviderService() {
    companion object {
        const val TAG: String = "NightWearComplicationProviderService"
    }
    private lateinit var bloodGlucoseService: BloodGlucoseService

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        bloodGlucoseService = BloodGlucoseService(this.applicationContext)
        bloodGlucoseService.onDataUpdate = { latestBg ->
            Log.d(TAG, "onDataUpdate callback, latestBg: " + latestBg)
        }
    }

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_SHORT_TEXT) {
            Log.d(TAG, "unsupported complication data type requested")
            manager.noUpdateRequired(complicationId)
            return
        }

        val latestBg = bloodGlucoseService.latestBg
        val bgText:String
        if (latestBg == null) {
            bgText = "---"
        } else {
            bgText = latestBg.combinedString()
        }

        Log.d(TAG, "updating complication data, bgText: " + bgText)

        val data: ComplicationData =
             ComplicationData.Builder(type)
                .setShortText(
                    ComplicationText.plainText(
                        bgText
                    )
                )
                .setIcon(
                    Icon.createWithResource(
                        this, R.drawable.baseline_invert_colors_white_18dp
                    )
                ).setShortTitle(
                    ComplicationText.plainText("BG")
                )
                .build()
        manager.updateComplicationData(complicationId, data)
    }

    override fun onComplicationActivated(complicationId: Int, type: Int, manager: ComplicationManager?) {
        super.onComplicationActivated(complicationId, type, manager)
    }

    override fun onComplicationDeactivated(complicationId: Int) {
        super.onComplicationDeactivated(complicationId)
    }
}