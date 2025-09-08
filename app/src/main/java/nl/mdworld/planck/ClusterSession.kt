package nl.mdworld.planck

import android.content.Context
import android.content.SharedPreferences
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.car.app.CarContext

class ClusterSession : Session() {
    override fun onCreateScreen(intent: android.content.Intent): Screen {
        return ClusterScreen(carContext)
    }
}

class ClusterScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        // Always show "Planck is running" in the cluster home
        return MessageTemplate.Builder("Planck is running")
            .setTitle("Current Playlist")
            .build()
    }
}
