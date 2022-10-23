package im.rah.nightwear

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.support.wearable.complications.ProviderUpdateRequester
import android.util.Log
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.text.ParseException
import java.util.*
import kotlin.concurrent.schedule
import android.content.ComponentName
import com.android.volley.*
import com.google.common.hash.Hashing

class BloodGlucoseService(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    var recentEntries:List<BloodGlucose?> = List<BloodGlucose?>(0) { null }
    val latestBg get() = recentEntries.firstOrNull()
    val penultimateBg get() = recentEntries.elementAtOrNull(1)
    val latestDelta: BloodGlucoseDelta? get() {
        if (recentEntries.size < 2) return null
        if (latestBg == null) return null
        if (penultimateBg == null) return null

        return BloodGlucoseDelta(penultimateBg!!, latestBg!!)
    }
    var onDataUpdateListeners: MutableList<(BloodGlucose)->Unit> = mutableListOf()

    private var nightscoutBaseUrl = ""
    private var nightscoutApiSecret = ""
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private var prefs: SharedPreferences
    private var lastRequestAdded: Instant = Instant.EPOCH

    companion object {
        const val TAG:String = "BloodGlucoseService"
        val SENSOR_REFRESH_INTERVAL:Duration = Duration.ofMinutes(5)

        const val NS_RECENT_ENTRIES_PATH = "/api/v1/entries/sgv"

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
        nightscoutApiSecret = prefs.getString("nightscoutApiSecret", "")!!
        
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
        if (key == "nightscoutApiSecret") {
            nightscoutApiSecret = prefs.getString("nightscoutApiSecret", "")!!
        }
    }

    private fun nsRecentEntriesUrl() : String {
        return nightscoutBaseUrl + NS_RECENT_ENTRIES_PATH
    }

    private fun nsSha1HashedSecret() : String {
        return Hashing.sha1().hashString(nightscoutApiSecret,Charsets.UTF_8).toString()
    }

    fun refresh(force: Boolean = false) {
        Log.d(tag, "Refresh, latest reading age: " + latestReadingAge().seconds)

        if (nightscoutBaseUrl == "") return
        // remember: the lastReadingAge is not when we last successfully received a response
        // the reading age reflects the last sensor reading we're aware of, that means:
        // - it can grow if we've not made successful API requests (eg no net connection available)
        // - it can grow if the sensor is not reporting or is in warm up
        // - it can grow if the collector is unable to push to Share or NightScout
        // - it can grow if NightScout is unable to pull from Share
        if (!force && latestReadingAge() < SENSOR_REFRESH_INTERVAL) {
            Log.d(tag, "Latest reading too fresh and force not set, returning without refresh")
            return
        }
        // don't make requests more than once every 30s
        if (Duration.between(lastRequestAdded, Instant.now()) < Duration.ofSeconds(30)) return

        Log.d(tag, "clearing queue, then requesting")
        Log.d(tag, "  nightscoutBaseUrl: " + nightscoutBaseUrl)
        Log.d(tag, "  nightscoutApiSecret: " + nightscoutApiSecret)
        Log.d(tag, "  nsSha1HashedSecret: " + nsSha1HashedSecret())

        requestQueue.cancelAll(this)

        lastRequestAdded = Instant.now()
        val stringRequest = object: StringRequest(
            Method.GET, nsRecentEntriesUrl(),
            { response ->
                Log.d(tag, "recent bgs received, parsing...")
                try {
                    recentEntries = BloodGlucose.parseTabSeparatedRecent(response)
                    Log.d(tag, "  " + latestBg!! +  " notifying " + onDataUpdateListeners.size + " listeners")
                    Log.d(tag, "  𝚫 " + BloodGlucoseDeltaPresenter(latestDelta!!))
                    onDataUpdateListeners.forEach {
                        it.invoke(latestBg!!)
                    }
                }
                catch (e: ParseException) {
                    // TODO: see error handling comment below
                    Log.d(tag, "ParseException for response: " + response)
                }
            },
            {
                // TODO: it would be nice to do something with the UI state to indicate a problem
                //       In the most common case slightly stale data is still useful, so what we
                //       choose to show shouldn't obscure that.
                Log.d(tag, "request error: " + it.toString())
            })
            {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()

                    if (nightscoutApiSecret != "") {
                        Log.d(TAG, "adding API-SECRET header")
                        headers.put("API-SECRET", nsSha1HashedSecret())
                    }
                    return headers
                }
            }
        stringRequest.tag = this

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }

    private val tag get() = TAG + "{" + hashCode() + ":" + Thread.currentThread().id + "}"
}