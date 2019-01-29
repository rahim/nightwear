package im.rah.nightwear

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.time.Duration
import java.util.*
import kotlin.concurrent.schedule

class BloodGlucoseService(context: Context) {
    var nightscoutUrl = "https://hugo-ns.herokuapp.com/api/v1/entries/current"
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    var latestBg:BloodGlucose? = null

    companion object {
        const val TAG:String = "BloodGlucoseService"
        val SENSOR_REFRESH_INTERVAL:Duration = Duration.ofMinutes(5)
    }

    init {
        Log.d(TAG, "initing")
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

    private fun refresh() {
        Log.d(TAG, "refresh")

        if (latestReadingAge() < SENSOR_REFRESH_INTERVAL) return

        Log.d(TAG, "requesting")
        val stringRequest = StringRequest(
            Request.Method.GET, nightscoutUrl,
            Response.Listener<String> { response ->
                Log.d(TAG, "bg received, parsing...")
                latestBg = BloodGlucose.parse_tab_separated_current(response)
            },
            Response.ErrorListener {
                Log.d(TAG, "request error")
            })

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }
}