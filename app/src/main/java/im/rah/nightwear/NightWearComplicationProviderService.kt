package im.rah.nightwear

import android.graphics.drawable.Icon
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

        val bg: BloodGlucose
        initBloodGlucoseService(complicationId, type, manager)

        if (!SUPPORTED_TYPED.contains(type)) {
            Log.d(tag, "unsupported complication data type requested")
            manager.noUpdateRequired(complicationId)
            return
        }

        if (bloodGlucoseService.latestBg == null) {
            Log.d(tag, "no bg available")
            manager.noUpdateRequired(complicationId)
            return
        } else {
            bg = bloodGlucoseService.latestBg!!
        }

        val builder = ComplicationData.Builder(type)
        
        if (type == ComplicationData.TYPE_SHORT_TEXT) {
            val bgText: String = bg.combinedString(markOld = true)

            Log.d(tag, "updating complication data (SHORT_TEXT), bgText: " + bgText)

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
            val bgText: String = bg.combinedString(markOld = false) + " (^1)" // the ^1 is interpolated with the age

            Log.d(tag, "updating complication data (LONG_TEXT), bgText: " + bgText)

            builder.setLongText(
                ComplicationText.TimeDifferenceBuilder()
                    .setSurroundingText(bgText)
                    .setReferencePeriodStart(bg.sensorTime)
                    .setReferencePeriodEnd(bg.sensorTime)
                    .setShowNowText(false)
                    .build()
            ).setLongTitle(
                ComplicationText.plainText("Blood glucose")
            ).setIcon(
                Icon.createWithResource(
                    this, R.drawable.baseline_invert_colors_white_18dp
                )
            )
        }

        val data: ComplicationData = builder.build()
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
        initialized = true
    }

    private val tag get() = TAG + "{" + hashCode() + ":" + Thread.currentThread().id + "}"
}