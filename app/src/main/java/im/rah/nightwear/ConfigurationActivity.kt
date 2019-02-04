package im.rah.nightwear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class ConfigurationActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        // Enables Always-on
        setAmbientEnabled()
    }
}
