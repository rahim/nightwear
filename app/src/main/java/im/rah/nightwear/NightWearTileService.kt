package im.rah.nightwear

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.DimensionBuilders.*
import androidx.wear.tiles.LayoutElementBuilders.*
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

/**
 * Creates a tile, showing current blood glucose value and the trend arrow.
 */
class NightWearTileService : TileService() {
    companion object {
        private const val RESOURCES_VERSION = "1"

        private const val BG_RANGE_LOW = 80
        private const val BG_RANGE_HIGH = 180

        private const val ARC_TOTAL_DEGREES = 360f
        private const val TILE_REFRESH_INTERVAL_MINUTES = 1
        private const val ID_CLICK_REFRESH_BG = "click_refresh_bg"
        private val PROGRESS_BAR_THICKNESS = dp(6f)
    }

    // For coroutines, use a custom scope we can cancel when the service is destroyed
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onTileRequest(requestParams: TileRequest) = serviceScope.future {

        if (requestParams.state!!.lastClickableId == ID_CLICK_REFRESH_BG) {
            Log.d(tag,"force refresh bg value")
            bloodGlucoseService.refresh(true)
        }

        val deviceParams = requestParams.deviceParameters!!
        val bg = bloodGlucoseService.latestBg

        Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(TILE_REFRESH_INTERVAL_MINUTES * 60 * 1000L)
            .setTimeline(
                Timeline.Builder()
                    .addTimelineEntry(
                        TimelineEntry.Builder()
                            .setLayout(
                                Layout.Builder()
                                    .setRoot(
                                        layout(bg, deviceParams)
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            ).build()
    }

    private fun layout(bloodGlucose: BloodGlucose?, deviceParameters: DeviceParameters) =
        Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .addContent(statusArc(bloodGlucose))
            .addContent(
                Column.Builder()
                    .addContent(currentBloodGlucoseText(bloodGlucose, deviceParameters))
                    .addContent(bloodGlucoseUnitText(deviceParameters))
                    .addContent(readingAgeText(bloodGlucose, deviceParameters))
                    .build())
            .build()

    private fun statusArc(bloodGlucose: BloodGlucose?) = Arc.Builder()
        .addContent(
            ArcLine.Builder()
                .setLength(degrees(ARC_TOTAL_DEGREES))
                .setColor(argb(ContextCompat.getColor(this, arcColor(bloodGlucose))))
                .setThickness(PROGRESS_BAR_THICKNESS)
                .build()
        )
        .setAnchorAngle(degrees(0.0f))
        .setAnchorType(ARC_ANCHOR_START)
        .build()

    private fun arcColor(bloodGlucose: BloodGlucose?): Int {
        return when (bloodGlucose?.glucoseLevel_mgdl) {
            in 0..BG_RANGE_LOW -> R.color.bg_low
            in BG_RANGE_LOW..BG_RANGE_HIGH -> R.color.bg_in_range
            in BG_RANGE_HIGH..Int.MAX_VALUE -> R.color.bg_high
            else -> R.color.background
        }
    }

    private fun currentBloodGlucoseText(bg: BloodGlucose?, deviceParameters: DeviceParameters): Text {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        val mmol = prefs.getBoolean("mmol", true)
        val text = bg?.let {
            BloodGlucosePresenter(it, mmol = mmol).combinedString()
        } ?: getString(R.string.bg_placeholder)

        return Text.Builder()
            .setText(text)
            .setFontStyle(FontStyles.display2(deviceParameters).build())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        ModifiersBuilders.Clickable.Builder()
                            .setId(ID_CLICK_REFRESH_BG)
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                    .setSemantics(
                        ModifiersBuilders.Semantics.Builder()
                            .setContentDescription(getString(R.string.tile_semantics_bg))
                            .build()
                    )
                    .build())
            .build()
    }

    private fun bloodGlucoseUnitText(deviceParameters: DeviceParameters): Text {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        val unit = if (prefs.getBoolean("mmol", true)) BloodGlucose.Unit.MMOL.label
                   else BloodGlucose.Unit.MGDL.label

        return Text.Builder()
            .setText(unit)
            .setFontStyle(FontStyles.title3(deviceParameters).build())
            .build()
    }

    private fun readingAgeText(bg: BloodGlucose?, deviceParameters: DeviceParameters): Text {
        val readingAgeInMinutes = bg?.readingAge()?.toMinutes()?.toInt() ?: Int.MAX_VALUE
        val readingAgeString =
            if (bg != null)
                resources.getQuantityString(
                    R.plurals.minutes_ago, readingAgeInMinutes, readingAgeInMinutes
                )
            else ""

        return Text.Builder()
            .setText(readingAgeString)
            .setFontStyle(FontStyles.title3(deviceParameters).build())
            .build()
    }

    override fun onResourcesRequest(requestParams: ResourcesRequest) = serviceScope.future {
        Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            // No Resources quite yet!
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleans up the coroutine
        serviceScope.cancel()
    }

    private val bloodGlucoseService get() = BloodGlucoseService.getInstance(this.applicationContext)

    private val tag get() = BloodGlucoseService.TAG + "{" + hashCode() + ":" + Thread.currentThread().id + "}"
}