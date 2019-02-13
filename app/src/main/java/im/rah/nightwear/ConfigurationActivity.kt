package im.rah.nightwear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class ConfigurationActivity : WearableActivity() {

    companion object {
        val TLDS = arrayOf("herokuapp.com", "azurewebsites.net")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        val tldTextView = findViewById<AutoCompleteTextView>(R.id.tld)
        tldTextView.threshold = 1
        ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, TLDS).also { adapter ->
            tldTextView.setAdapter(adapter)
        }

        // Enables Always-on
        setAmbientEnabled()
    }
}
