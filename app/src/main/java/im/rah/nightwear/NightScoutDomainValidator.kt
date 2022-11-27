package im.rah.nightwear

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.common.hash.Hashing
import java.util.HashMap

class NightScoutDomainValidator(val context: Context, val url: String, val secret: String) {
    private lateinit var validationSucessCallback: (String) -> Unit
    private lateinit var validationErrorCallback: (java.lang.Exception) -> Unit

    companion object {
        const val TAG: String = "NightScoutUrlValidator"
        const val STATUS_PATH: String = "/api/v1/status.json"
    }

    fun onValidationSuccess(callback: (String) -> Unit) {
        validationSucessCallback = callback
    }

    //FIXME: either add error to signature, or probably better split out to multiple callbacks
    //       eg onAuthenticationError, etc
    fun onValidationError(callback: (java.lang.Exception) -> Unit) {
        validationErrorCallback = callback
    }

    fun run() {
        val queue = Volley.newRequestQueue(context)
        val testUrl = url + STATUS_PATH
        Log.d(TAG, "testing " + testUrl)
        val stringRequest = object: StringRequest(
            Method.GET, testUrl,
            { if (it.contains("nightscout")) validationSucessCallback.invoke(url) },
            // 401 -> class com.android.volley.AuthFailureError null
            // - wrong API key
            // - API key provided when no API key set
            // 404 -> class com.android.volley.ClientError null
            // - wrong domain, but resolves
            //
            // class com.android.volley.NoConnectionError java.net.UnknownHostException: Unable to resolve host "acme.com": No address associated with hostname
            // - unresolvable domain
            // - (after some internal volley timeout) no network connection

            { validationErrorCallback.invoke(it) }
        )
        {
            //@Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                if (secret != "") {
                    Log.d(TAG, "adding API-SECRET header")
                    headers.put("API-SECRET", nsSha1HashedSecret())
                }
                return headers
            }
        }
        // we want failure to occur as soon and as clearly as possible so we can infer what the
        // problem is
        stringRequest.setShouldCache(false)
        stringRequest.setShouldRetryConnectionErrors(false)
        stringRequest.setShouldRetryServerErrors(false)
        stringRequest.retryPolicy =
            DefaultRetryPolicy(0, 0,  DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(stringRequest)
    }

    private fun nsSha1HashedSecret() = Hashing.sha1().hashString(secret,Charsets.UTF_8).toString()
}