package im.rah.wearhello

import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class MainHelloActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_hello)

        // Enables Always-on
        setAmbientEnabled()
    }
}
