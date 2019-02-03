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
    //invalid crashes out unable to getCacheDir
    //private var bloodGlucoseService: BloodGlucoseService = BloodGlucoseService(this)

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_SHORT_TEXT) {
            Log.d(TAG, "unsupported complication data type requested")
            manager.noUpdateRequired(complicationId)
            return
        }

//        var bg?

        Log.d(TAG, "updating complication data")

        var data: ComplicationData =
             ComplicationData.Builder(type)
                .setShortText(
                    ComplicationText.plainText(
                        "foo"
                    )
                )
                .setIcon(
                    Icon.createWithResource(
                        this, R.drawable.baseline_invert_colors_white_18dp
                    )
                ).setShortTitle(
                    ComplicationText.plainText("bar")
                )
                .build()

//        val thisProvider = ComponentName(this, javaClass)
//        val complicationTogglePendingIntent =
//            ComplicationToggleReceiver.getToggleIntent(this, thisProvider, complicationId)
//
//        val preferences = getSharedPreferences(ComplicationToggleReceiver.PREFERENCES_NAME, 0)
//        val state = preferences.getInt(
//            ComplicationToggleReceiver.getPreferenceKey(thisProvider, complicationId),
//            0
//        )

//        var data: ComplicationData? = null
//        when (state % 4) {
//            0 -> data = ComplicationData.Builder(type)
//                .setShortText(
//                    ComplicationText.plainText(
//                        getString(R.string.short_text_only)
//                    )
//                )
//                .setTapAction(complicationTogglePendingIntent)
//                .build()
//            1 -> data = ComplicationData.Builder(type)
//                .setShortText(
//                    ComplicationText.plainText(
//                        getString(R.string.short_text_with_icon)
//                    )
//                )
//                .setIcon(
//                    Icon.createWithResource(
//                        this, R.drawable.ic_face_vd_theme_24
//                    )
//                )
//                .setTapAction(complicationTogglePendingIntent)
//                .build()
//            2 -> data = ComplicationData.Builder(type)
//                .setShortText(
//                    ComplicationText.plainText(
//                        getString(R.string.short_text_with_title)
//                    )
//                )
//                .setShortTitle(
//                    ComplicationText.plainText(getString(R.string.short_title))
//                )
//                .setTapAction(complicationTogglePendingIntent)
//                .build()
//            3 ->
//                // When short text includes both short title and icon, the watch face should only
//                // display one of those fields.
//                data = ComplicationData.Builder(type)
//                    .setShortText(
//                        ComplicationText.plainText(
//                            getString(R.string.short_text_with_both)
//                        )
//                    )
//                    .setShortTitle(
//                        ComplicationText.plainText(getString(R.string.short_title))
//                    )
//                    .setIcon(
//                        Icon.createWithResource(
//                            this, R.drawable.ic_face_vd_theme_24
//                        )
//                    )
//                    .setTapAction(complicationTogglePendingIntent)
//                    .build()
//        }
        manager.updateComplicationData(complicationId, data)
    }
}