package nl.mdworld.planck

import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.car.app.CarContext
import androidx.car.app.model.Header

class ClusterSession : Session() {
    override fun onCreateScreen(intent: android.content.Intent): Screen {
        val screen = ClusterScreen(carContext)
        ClusterScreen.currentScreen = screen
        return screen
    }
}

class ClusterScreen(carContext: CarContext) : Screen(carContext) {
    companion object {
        var currentScreen: ClusterScreen? = null

        fun refreshClusterDisplay() {
            currentScreen?.invalidate()
        }
    }

    override fun onGetTemplate(): Template {
        // Always show "Planck is running" in the cluster home
        return MessageTemplate.Builder("Planck is running")
            .setHeader(Header.Builder().setTitle("Current Playlist").build())
            .build()
}}

