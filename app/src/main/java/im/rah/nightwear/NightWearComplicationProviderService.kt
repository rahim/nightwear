package im.rah.nightwear

import android.graphics.drawable.Icon
import android.support.wearable.complications.*
import android.util.Log

class NightWearComplicationProviderService : ComplicationProviderService() {
    companion object {
        const val TAG: String = "NightWearComplicationProviderService"
    }
    private lateinit var bloodGlucoseService: BloodGlucoseService

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        Log.d(TAG, "onComplicationUpdate")

        if (type != ComplicationData.TYPE_SHORT_TEXT) {
            Log.d(TAG, "unsupported complication data type requested")
            manager.noUpdateRequired(complicationId)
            return
        }

        val bgText:String

        if (this::bloodGlucoseService.isInitialized) {
            bgText = bloodGlucoseService.latestBg?.combinedString() ?: getString(R.string.complication_no_data)
        }
        else {
            Log.d(TAG, "bloodGlucoseService not initialized, initializing...")
            initBloodGlucoseService(complicationId, type, manager)
            bgText = getString(R.string.complication_no_data)
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

    override fun onComplicationActivated(complicationId: Int, type: Int, manager: ComplicationManager) {
        super.onComplicationActivated(complicationId, type, manager)
        Log.d(TAG, "onComplicationActivated")

        initBloodGlucoseService(complicationId, type, manager)
    }

    override fun onComplicationDeactivated(complicationId: Int) {
        super.onComplicationDeactivated(complicationId)
        Log.d(TAG, "onComplicationDeactivated")
    }

    private fun initBloodGlucoseService(complicationId: Int, type: Int, manager: ComplicationManager) {
        bloodGlucoseService = BloodGlucoseService(this.applicationContext)
        bloodGlucoseService.onDataUpdate = { latestBg ->
            Log.d(TAG, "onDataUpdate callback, latestBg: " + latestBg)
            this.onComplicationUpdate(complicationId, type, manager)
        }
    }
}