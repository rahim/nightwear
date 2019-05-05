package im.rah.nightwear

import android.graphics.drawable.Icon
import android.support.wearable.complications.*
import android.util.Log

class NightWearComplicationProviderService : ComplicationProviderService() {
    companion object {
        const val TAG: String = "NightWearComplicationProviderService"
    }

    private var initialized = false

    override fun onCreate() {
        Log.d(tag, "onCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        super.onDestroy()
    }

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        Log.d(tag, "onComplicationUpdate")

        if (type != ComplicationData.TYPE_SHORT_TEXT) {
            Log.d(tag, "unsupported complication data type requested")
            manager.noUpdateRequired(complicationId)
            return
        }

        val bgText: String =
            bloodGlucoseService.latestBg?.combinedString(markOld = true) ?: getString(R.string.complication_no_data)

        Log.d(tag, "updating complication data, bgText: " + bgText)

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
        Log.d(tag, "onComplicationActivated")

        initBloodGlucoseService(complicationId, type, manager)
    }

    override fun onComplicationDeactivated(complicationId: Int) {
        super.onComplicationDeactivated(complicationId)
        Log.d(tag, "onComplicationDeactivated")
    }

    private val bloodGlucoseService get() = BloodGlucoseService.getInstance(this.applicationContext)

    private fun initBloodGlucoseService(complicationId: Int, type: Int, manager: ComplicationManager) {
        Log.d(tag, "initBloodGlucoseService")

        if (initialized) {
            Log.d(tag, "already initialized")
            return
        }

        bloodGlucoseService.addDataUpdateListener { latestBg ->
            Log.d(tag, "onDataUpdate callback, latestBg: " + latestBg)
            this.onComplicationUpdate(complicationId, type, manager)
        }
        initialized = true
    }

    private val tag get() = TAG + "{" + hashCode() + ":" + Thread.currentThread().id + "}"
}