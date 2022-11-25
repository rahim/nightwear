package im.rah.nightwear

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.wearable.activity.WearableActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.common.hash.Hashing
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.ParseException
import java.util.HashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

class ConfigurationActivity : WearableActivity() {

    companion object {
        const val TAG:String = "ConfigurationActivity"
        private val COMMON_TLDS = arrayOf("herokuapp.com", "azurewebsites.net", "fly.dev", "up.railway.app")
        private const val DEFAULT_URL = "https://domain.herokuapp.com"
    }

    private val scheme = "https://"

    private lateinit var urlTextView:TextView
    private lateinit var domainEditText:EditText
    private lateinit var tldSpinner:Spinner
    private lateinit var apiSecretEditText:EditText
    private lateinit var confirmButton:Button
    private lateinit var unitToggleButton:ToggleButton
    private lateinit var timeFormatToggleButton:ToggleButton

    private lateinit var prefs:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)

        setContentView(R.layout.activity_configuration)

        tldSpinner = findViewById(R.id.tld)
        val tldOptions = COMMON_TLDS + arrayOf("[other]")
        ArrayAdapter(this, R.layout.centered_spinner_dropdown_item, tldOptions).also { adapter ->
            tldSpinner.adapter = adapter
        }

        domainEditText = findViewById(R.id.domain)
        urlTextView = findViewById(R.id.url)
        apiSecretEditText = findViewById(R.id.api_secret)

        tldSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                refreshUrlText()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                refreshUrlText()
            }
        }
        domainEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "onTextChanged")
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged")
                refreshUrlText()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged")
            }
        })

        confirmButton = findViewById(R.id.confirm_button)
        confirmButton.setOnClickListener { handleUrlConfirmation() }

        unitToggleButton = findViewById(R.id.unit_toggle_button)
        unitToggleButton.setOnClickListener { persistUnit() }

        timeFormatToggleButton = findViewById(R.id.time_format_toggle_button)
        timeFormatToggleButton.setOnClickListener { persistTimeFormat() }

        loadUrlFromPrefs()
        loadSecretFromPrefs()
        loadUnitFromPrefs()
        loadTimeFormatFromPrefs()

        // Enables Always-on
        setAmbientEnabled()
    }

    private fun loadUrlFromPrefs() {
        val url = prefs.getString("nightscoutBaseUrl", DEFAULT_URL)!!
        val domain = domainFromUrl(url)
        domainEditText.setText(domain)

        val tld = tldFromUrl(url)
        val adapter = tldSpinner.adapter as ArrayAdapter<String>
        val tldPosition = adapter.getPosition(tld)
        tldSpinner.setSelection(tldPosition)
    }

    private fun loadSecretFromPrefs() {
        val secret = prefs.getString("nightscoutApiSecret", "")
        apiSecretEditText.setText(secret)
    }

    private fun loadUnitFromPrefs() {
        val mmol = prefs.getBoolean("mmol", true)
        unitToggleButton.isChecked = mmol
    }

    private fun loadTimeFormatFromPrefs() {
        val twentyFourHour = prefs.getBoolean("twentyFourHour", true)
        timeFormatToggleButton.isChecked = twentyFourHour
    }

    private fun domainFromUrl(url:String) : String {
        val tld = tldFromUrl(url)
        val tldStrippedUrl = if (tld.isNotBlank())
                               url.replace(tld, "")
                             else
                               url
        return tldStrippedUrl.replace(scheme, "").removeSuffix(".")
    }

    private fun tldFromUrl(url:String) : String {
        for (tld in COMMON_TLDS) if (url.endsWith(tld)) return tld
        return ""
    }

    private fun refreshUrlText() {
        Log.d(TAG, "refreshUrlText")
        val url = url()
        if (urlTextView.text != url) {
            Log.d(TAG, "updating urlTextView")
            urlTextView.text = url
        }
    }

    private fun persistUrlAndSecret() {
        Log.d(TAG, "persistUrlAndSecret")
        val url = url()
        if (prefs.getString("nighscoutBaseUrl", "") != url) {
            Log.d(TAG, "updating nighscoutBaseUrl pref")
            val edit = prefs.edit()
            edit.putString("nightscoutBaseUrl", url())
            edit.apply()
        }
        val secret:String = "" + apiSecretEditText.text
        if (prefs.getString("nighscoutApiSecret", "") != secret) {
            Log.d(TAG, "updating nighscoutApiSecret pref")
            val edit = prefs.edit()
            edit.putString("nightscoutApiSecret", secret)
            edit.apply()
        }
    }

    private fun persistUnit() {
        Log.d(TAG, "persistUnit")
        val edit = prefs.edit()
        edit.putBoolean("mmol", unitToggleButton.isChecked)
        edit.apply()
    }

    private fun persistTimeFormat() {
        Log.d(TAG, "persistTimeFormat")
        val edit = prefs.edit()
        edit.putBoolean("twentyFourHour", timeFormatToggleButton.isChecked)
        edit.apply()
    }

    private fun url() : String {
        return if (tldSpinner.selectedItem in COMMON_TLDS) {
            scheme + domainEditText.text + "." + tldSpinner.selectedItem
        } else {
            scheme + domainEditText.text
        }
    }

    private fun handleUrlConfirmation() {
        thread {
            if (urlValid()) {
                persistUrlAndSecret()
                setResult(Activity.RESULT_OK)
                finish()
            }
            else {
                this.runOnUiThread {
                    Toast.makeText(this, "Invalid URL: ${url()}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun testUrl() = url() + "/api/v1/status.json"

    // TODO: this following duplicates much of what we're doing in BloodGlucoseService
    //       extract a shared class or some utility there

    private val nightscoutApiSecret get() = "" + apiSecretEditText.text
    private fun nsSha1HashedSecret() = Hashing.sha1().hashString(nightscoutApiSecret,Charsets.UTF_8).toString()

    // TODO: ideally we'd differentiate the toast if we detect unauthorized headers...
    // It's okay if this is blocking because it's run within a separate thread
    private fun urlValid() : Boolean {
        val queue = Volley.newRequestQueue(this)
        val requestFuture : RequestFuture<String> = RequestFuture.newFuture()

        Log.d(TAG, "testing " + testUrl())
        val stringRequest = object: StringRequest(Method.GET, testUrl(), requestFuture, requestFuture)
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

        queue.add(stringRequest)

        try {
            val response = requestFuture.get(10, TimeUnit.SECONDS)
            Log.d(TAG, "response: " + response)
            return response.contains("nightscout")
        }
        catch (e: TimeoutException) {
            // Unclear when we'd actually see this if at all, volley's TimeoutError gets wrapped in
            // ExecutionException below
            Log.d(TAG, "TimeoutException")
            return false
        }
        catch(e: ExecutionException) {
            // This wraps exceptions within the future
            // eg ExecutionException: com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host "foo.bar.baz": No address associated with hostnam
            // TODO: do something better on timeout, ie potentially still valid, third state
            Log.d(TAG, "ExecutionException: " + e.message)
            // invalid secret, valid domain, gives com.android.volley.AuthFailureError
            // unresolvable domain gives com.android.volley.NoConnectionError
            // resolvable domain, no response, com.android.volley.TimeoutError
            // 404 (eg not a NightScout app), com.android.volley.ClientError
            Log.d(TAG, "cause: " + e.cause.toString())
            return false
        }
        catch(e: Exception) {
            Log.d(TAG, "Unexpected Exception: " + e.javaClass)
            return false
        }
    }
}
