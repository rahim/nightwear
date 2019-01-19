package im.rah.nightwear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*
import kotlin.concurrent.schedule

class MainHelloActivity : WearableActivity() {

    private lateinit var requestQueue : RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_hello)

        // Enables Always-on
        setAmbientEnabled()

        requestQueue = Volley.newRequestQueue(this)

        Timer().schedule(0, 1000 * 30) { runOnUiThread { refresh()} }
    }

    private fun refresh() {
        val textReading = findViewById<TextView>(R.id.textReading)
        val textReadingAge = findViewById<TextView>(R.id.textReadingAge)
        val url = "https://hugo-ns.herokuapp.com/api/v1/entries/current"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val bg = BloodGlucose.parse_tab_separated_current(response)
                textReading.text = bg.toString()
                textReadingAge.text = bg.readingAge().toMinutes().toString() + "m"
            },
            Response.ErrorListener { textReading.text = "X" })

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }

}
