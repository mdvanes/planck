package nl.mdworld.planck4

import androidx.car.app.Screen
import androidx.car.app.Session

class MediaCarSession : Session() {
    override fun onCreateScreen(intent: android.content.Intent): Screen {
        return MediaCarScreen(carContext)
    }
}
