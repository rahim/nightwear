package im.rah.nightwear

import android.content.*
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.view.SurfaceHolder
import android.view.WindowInsets

import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.TimeZone

/**
 * Minimal NightScout blood glucose display with direction.
 * In non-ambient mode, the reading age and an ISO 8601 style time is also shown.
 */
class NightWearDigitalFace : CanvasWatchFaceService() {

    companion object {
        private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        /**
         * Updates rate in milliseconds for interactive mode. We update once a second since seconds
         * are displayed in interactive mode.
         */
        private const val INTERACTIVE_UPDATE_RATE_MS = 1000

        /**
         * Handler message id for updating the time periodically in interactive mode.
         */
        private const val MSG_UPDATE_TIME = 0

        private lateinit var bloodGlucoseService: BloodGlucoseService
    }

    override fun onCreateEngine(): Engine {
        bloodGlucoseService = BloodGlucoseService(this)

        return Engine()
    }

    private class EngineHandler(reference: NightWearDigitalFace.Engine) : Handler() {
        private val mWeakReference: WeakReference<NightWearDigitalFace.Engine> = WeakReference(reference)

        override fun handleMessage(msg: Message) {
            val engine = mWeakReference.get()
            if (engine != null) {
                when (msg.what) {
                    MSG_UPDATE_TIME -> engine.handleUpdateTimeMessage()
                }
            }
        }
    }

    inner class Engine : CanvasWatchFaceService.Engine(), SharedPreferences.OnSharedPreferenceChangeListener {
        private var mPrefs: SharedPreferences
        private var mCalendar: Calendar

        private var mRegisteredTimeZoneReceiver = false

        private var mXOffset: Float = 0F
        private var mYOffset: Float = 0F

        private lateinit var mBackgroundPaint: Paint
        private lateinit var mTextPaint: Paint

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false
        private var mAmbient: Boolean = false
        private var mMmol: Boolean

        init {
            mCalendar = Calendar.getInstance()
            mPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            mPrefs.registerOnSharedPreferenceChangeListener(this)
            mMmol = mPrefs.getBoolean("mmol", true)
        }

        private val mUpdateTimeHandler: Handler = EngineHandler(this)

        private val mTimeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@NightWearDigitalFace)
                    .setAcceptsTapEvents(true)
                    .build()
            )

            val resources = this@NightWearDigitalFace.resources
            mYOffset = resources.getDimension(R.dimen.digital_y_offset)

            // Initializes background.
            mBackgroundPaint = Paint().apply {
                color = ContextCompat.getColor(applicationContext, R.color.background)
            }

            // Initializes Watch Face.
            mTextPaint = Paint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                color = ContextCompat.getColor(applicationContext, R.color.digital_text)
            }

        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            mLowBitAmbient = properties.getBoolean(
                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
            )
            mBurnInProtection = properties.getBoolean(
                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
            )
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()

            bloodGlucoseService.tick()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            mAmbient = inAmbientMode

            if (mLowBitAmbient) {
                mTextPaint.isAntiAlias = !inAmbientMode
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            // Draw the background.
            if (mAmbient) {
                canvas.drawColor(Color.BLACK)
            } else {
                canvas.drawRect(
                    0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), mBackgroundPaint
                )
            }

            if (bloodGlucoseService.latestBg == null) {
                val text = "---"
                canvas.drawText(text, mXOffset, mYOffset, mTextPaint)
                return
            }

            val bgText = (bloodGlucoseService.latestBg as BloodGlucose).combinedString(mMmol)
            canvas.drawText(bgText, mXOffset, mYOffset, mTextPaint)

            if (!mAmbient) {
                val readingAgeText = bloodGlucoseService.latestReadingAge().toMinutes().toString() + "m"
                canvas.drawText(readingAgeText, mXOffset, mYOffset + mTextPaint.textSize, mTextPaint)

                val now = System.currentTimeMillis()
                mCalendar.timeInMillis = now

                val timeText = String.format(
                    "%02d:%02d:%02d",
                    mCalendar.get(Calendar.HOUR_OF_DAY),
                    mCalendar.get(Calendar.MINUTE),
                    mCalendar.get(Calendar.SECOND)
                )
                canvas.drawText(timeText, mXOffset, mYOffset + 2*mTextPaint.textSize, mTextPaint)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()

                // Update time zone in case it changed while we weren't visible.
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            } else {
                unregisterReceiver()
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }

        override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
            if (key == "mmol") mMmol = prefs.getBoolean("mmol", true)
        }

        private fun registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@NightWearDigitalFace.registerReceiver(mTimeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = false
            this@NightWearDigitalFace.unregisterReceiver(mTimeZoneReceiver)
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            // Load resources that have alternate values for round watches.
            val resources = this@NightWearDigitalFace.resources
            val isRound = insets.isRound
            mXOffset = resources.getDimension(
                if (isRound)
                    R.dimen.digital_x_offset_round
                else
                    R.dimen.digital_x_offset
            )

            val textSize = resources.getDimension(
                if (isRound)
                    R.dimen.digital_text_size_round
                else
                    R.dimen.digital_text_size
            )

            mTextPaint.textSize = textSize
        }

        /**
         * Starts the [.mUpdateTimeHandler] timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private fun updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        /**
         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        fun handleUpdateTimeMessage() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val timeMs = System.currentTimeMillis()
                val delayMs = INTERACTIVE_UPDATE_RATE_MS - timeMs % INTERACTIVE_UPDATE_RATE_MS
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
            }
        }
    }
}
