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
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread

class ConfigurationActivity : WearableActivity() {

    companion object {
        const val TAG:String = "ConfigurationActivity"
        private val COMMON_TLDS = arrayOf("herokuapp.com", "azurewebsites.net")
        private const val DEFAULT_URL = "https://domain.herokuapp.com"
    }

    private val scheme = "https://"

    private lateinit var urlTextView:TextView
    private lateinit var domainEditText:EditText
    private lateinit var tldSpinner:Spinner
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
        confirmButton.setOnClickListener {
            thread {
                if (urlValid()) {
                    persistUrl()
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

        unitToggleButton = findViewById(R.id.unit_toggle_button)
        unitToggleButton.setOnClickListener { persistUnit() }

        timeFormatToggleButton = findViewById(R.id.time_format_toggle_button)
        timeFormatToggleButton.setOnClickListener { persistTimeFormat() }

        loadUrlFromPrefs()
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

    private fun persistUrl() {
        Log.d(TAG, "persistUrl")
        val url = url()
        if (prefs.getString("nighscoutBaseUrl", "") != url) {
            Log.d(TAG, "updating nighscoutBaseUrl pref")
            val edit = prefs.edit()
            edit.putString("nightscoutBaseUrl", url())
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

    private fun urlValid() : Boolean {
        return try {
            URL(url() + "/api/v1/status.json").readText().contains("nightscout")
        } catch (e : IOException) {
            false
        }
    }
}
