package im.rah.nightwear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView



class ConfigurationActivity : WearableActivity() {

    companion object {
        val TLDS = arrayOf("herokuapp.com", "azurewebsites.net")
    }

    val scheme = "https://"

    lateinit var urlTextView:TextView
    lateinit var domainEditText:EditText
    lateinit var tldSpinner:Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                refreshUrlText()
            }

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })
        refreshUrlText()

        // Enables Always-on
        setAmbientEnabled()
    }

    fun refreshUrlText() {
        urlTextView.text = url()
    }

    fun url() : String {
        return scheme + domainEditText.text + "." + tldSpinner.selectedItem
    }
}
