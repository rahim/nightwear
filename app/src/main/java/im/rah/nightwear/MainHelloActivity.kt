package im.rah.nightwear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.TextView
import java.util.*
import kotlin.concurrent.schedule

class MainHelloActivity : WearableActivity() {

    private lateinit var bloodGlucoseService: BloodGlucoseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_hello)

        // Enables Always-on
        setAmbientEnabled()

        bloodGlucoseService = BloodGlucoseService(this)

        Timer().schedule(0, 1000) { runOnUiThread { refresh()} }
    }

    private fun refresh() {
        val textReading = findViewById<TextView>(R.id.textReading)
        val textReadingAge = findViewById<TextView>(R.id.textReadingAge)

        val bg:BloodGlucose? = bloodGlucoseService.latestBg
        if (bg == null) {
            textReading.text = "X"
            textReadingAge.text = ""
        }
        else {
            textReading.text = bg.toString()
            textReadingAge.text = bg.readingAge().toMinutes().toString() + "m"
        }
    }

}
