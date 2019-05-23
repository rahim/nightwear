package im.rah.nightwear

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.wearable.complications.ProviderUpdateRequester
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.text.ParseException
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule
import android.content.ComponentName



class BloodGlucoseService(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    var latestBg:BloodGlucose? = null
    var onDataUpdateListeners: MutableList<(BloodGlucose)->Unit> = mutableListOf()

    private var nightscoutBaseUrl = ""
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private var prefs: SharedPreferences
    private var lastRequestAdded: Instant = Instant.EPOCH

    companion object {
        const val TAG:String = "BloodGlucoseService"
        val SENSOR_REFRESH_INTERVAL:Duration = Duration.ofMinutes(5)

        const val NS_CURRENT_ENTRY_PATH = "/api/v1/entries/current"

        private var instance:BloodGlucoseService? = null

        // WARN: implementation not thread safe
        fun getInstance(context: Context) : BloodGlucoseService {
            if (instance == null) {
                instance = BloodGlucoseService(context.applicationContext)
            }
            return instance!!
        }
    }

    init {
        Log.d(tag, "init, with context: " + context.hashCode())
        prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        prefs.registerOnSharedPreferenceChangeListener(this)
        nightscoutBaseUrl = prefs.getString("nightscoutBaseUrl", "")!!

        Timer().schedule(0, 1000 * 15) { refresh() }

        addDataUpdateListener {
            Log.d(tag, "sending provider update request...")
            val provider = ComponentName(context, NightWearComplicationProviderService::class.java)
            val requester = ProviderUpdateRequester(context, provider)
            requester.requestUpdateAll()
        }
    }

    fun tick() {
        Log.d(tag, "tick received")
    }

    fun latestReadingAge() : Duration {
        return if (latestBg != null) {
            latestBg!!.readingAge() // the !! here still feels like a smell
        } else {
            Duration.ofSeconds(Long.MAX_VALUE)
        }
    }

    fun addDataUpdateListener(listener: (BloodGlucose)->Unit) {
        onDataUpdateListeners.add(listener)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        Log.d(tag, "prefs changed")
        if (key == "nightscoutBaseUrl") {
          nightscoutBaseUrl = prefs.getString("nightscoutBaseUrl", "")!!
        }
    }

    private fun nsCurrentEntryUrl() : String {
        return nightscoutBaseUrl + NS_CURRENT_ENTRY_PATH
    }

    private fun refresh() {
        Log.d(tag, "Refresh, latest reading age: " + latestReadingAge().seconds)

        if (nightscoutBaseUrl == "") return
        // remember: the lastReadingAge is not when we last successfully received a response
        // the reading age reflects the last sensor reading we're aware of, that means:
        // - it can grow if we've not made successful API requests (eg no net connection available)
        // - it can grow if the sensor is not reporting or is in warm up
        // - it can grow if the collector is unable to push to Share or NightScout
        // - it can grow if NightScout is unable to pull from Share
        if (latestReadingAge() < SENSOR_REFRESH_INTERVAL) return
        // don't make requests more than once every 30s
        if (Duration.between(lastRequestAdded, Instant.now()) < Duration.ofSeconds(30)) return

        Log.d(tag, "clearing queue, then requesting, nightscoutBaseUrl: " + nightscoutBaseUrl)
        requestQueue.cancelAll(this)

        lastRequestAdded = Instant.now()
        val stringRequest = StringRequest(
            Request.Method.GET, nsCurrentEntryUrl(),
            Response.Listener<String> { response ->
                Log.d(tag, "bg received, parsing...")
                try {
                    latestBg = BloodGlucose.parseTabSeparatedCurrent(response)
                    Log.d(tag, "  " + latestBg!!.combinedString() +  " notifying " + onDataUpdateListeners.size + " listeners")
                    onDataUpdateListeners.forEach {
                        it.invoke(latestBg!!)
                    }
                }
                catch (e: ParseException) {
                    Log.d(tag, "ParseException for response: " + response)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "request error")
            })
        stringRequest.tag = this

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }

    private val tag get() = TAG + "{" + hashCode() + ":" + Thread.currentThread().id + "}"
}