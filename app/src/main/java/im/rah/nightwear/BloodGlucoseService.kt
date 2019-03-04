package im.rah.nightwear

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
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

class BloodGlucoseService(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    var latestBg:BloodGlucose? = null

    private var nightscoutBaseUrl = ""
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private var prefs: SharedPreferences
    private var lastRequestAdded:Instant = Instant.EPOCH

    companion object {
        const val TAG:String = "BloodGlucoseService"
        val SENSOR_REFRESH_INTERVAL:Duration = Duration.ofMinutes(5)

        const val NS_CURRENT_ENTRY_PATH = "/api/v1/entries/current"
    }

    init {
        Log.d(TAG, "initing")
        prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        prefs.registerOnSharedPreferenceChangeListener(this)
        nightscoutBaseUrl = prefs.getString("nightscoutBaseUrl", "")

        Timer().schedule(0, 1000 * 15) { refresh() }
    }

    fun tick() {
        Log.d(TAG, "tick received")
    }

    fun latestReadingAge() : Duration {
        return if (latestBg != null) {
            latestBg!!.readingAge() // the !! here still feels like a smell
        } else {
            Duration.ofSeconds(Long.MAX_VALUE)
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        Log.d(TAG, "prefs changed")
        if (key == "nightscoutBaseUrl") {
          nightscoutBaseUrl = prefs.getString("nightscoutBaseUrl", "")
        }
    }

    private fun nsCurrentEntryUrl() : String {
        return nightscoutBaseUrl + NS_CURRENT_ENTRY_PATH
    }

    private fun refresh() {
        Log.d(TAG, "refresh")
        Log.d(TAG, "Latest reading age: " + latestReadingAge().seconds)
        Log.d(TAG, "nightscoutBaseUrl: " + nightscoutBaseUrl)

        if (nightscoutBaseUrl == "") return
        if (latestReadingAge() < SENSOR_REFRESH_INTERVAL) return
        if (Duration.between(lastRequestAdded, Instant.now()) < Duration.ofSeconds(30)) return

        Log.d(TAG, "requesting")
        lastRequestAdded = Instant.now()
        val stringRequest = StringRequest(
            Request.Method.GET, nsCurrentEntryUrl(),
            Response.Listener<String> { response ->
                Log.d(TAG, "bg received, parsing...")
                try {
                    latestBg = BloodGlucose.parseTabSeparatedCurrent(response)
                }
                catch (e: ParseException) {
                    Log.d(TAG, "ParseException for response: " + response)
                }
            },
            Response.ErrorListener {
                Log.d(TAG, "request error")
            })

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }
}