package im.rah.nightwear

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView

class ConfigurationActivity : WearableActivity() {

    companion object {
        const val TAG:String = "ConfigurationActivity"
        private val TLDS = arrayOf("herokuapp.com", "azurewebsites.net")
        private const val DEFAULT_URL = "https://domain.herokuapp.com"
    }

    private val scheme = "https://"

    private lateinit var urlTextView:TextView
    private lateinit var domainEditText:EditText
    private lateinit var tldSpinner:Spinner

    lateinit var prefs:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = applicationContext.getSharedPreferences("nightwear", Context.MODE_PRIVATE)

        setContentView(R.layout.activity_configuration)

        tldSpinner = findViewById(R.id.tld)
        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, TLDS).also { adapter ->
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

        loadUrlFromPrefs()

        // Enables Always-on
        setAmbientEnabled()
    }

    private fun loadUrlFromPrefs() {
        val url = prefs.getString("nightscout-url", DEFAULT_URL)
        val domain = domainFromUrl(url)
        domainEditText.setText(domain)

        val tld = tldFromUrl(url)
        val adapter = tldSpinner.adapter as ArrayAdapter<String>
        val tldPosition = adapter.getPosition(tld)
        tldSpinner.setSelection(tldPosition)
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
        for (tld in TLDS) if (url.endsWith(tld)) return tld
        return ""
    }

    private fun refreshUrlText() {
        val url = url()
        if (urlTextView.text != url) {
            urlTextView.text = url()
            val edit = prefs.edit()
            edit.putString("nightscout-url", url())
            edit.apply()
        }
    }

    private fun url() : String {
        return scheme + domainEditText.text + "." + tldSpinner.selectedItem
    }
}
