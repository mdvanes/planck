package nl.mdworld.planck4.views.settings

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import nl.mdworld.planck4.BuildConfig
import nl.mdworld.planck4.CarDistractionOptimizer
import nl.mdworld.planck4.SettingsManager

class SettingsCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        // If driving state changed and we're now driving, go back to main screen
        if (!CarDistractionOptimizer.isFeatureAvailable(carContext, requiresParking = true)) {
            screenManager.pop()
        }

        val backAction = Action.Builder()
            .setTitle("Back")
            .setOnClickListener {
                screenManager.pop()
            }
            .build()

        // Get current settings values to display
        val serverUrl = SettingsManager.getServerUrl(carContext)
        val username = SettingsManager.getUsername(carContext)

        val itemListBuilder = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Server Settings")
                    .addText("Current server: ${if (serverUrl.isNotEmpty()) serverUrl else "Not configured"}")
                    .setOnClickListener {
                        // In a real implementation, you'd show a text input dialog
                        // For now, just show current value
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Account Settings")
                    .addText("Current user: ${if (username.isNotEmpty()) username else "Not configured"}")
                    .setOnClickListener {
                        // In a real implementation, you'd show account configuration
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Connection Status")
                    .addText("Tap to test connection")
                    .setOnClickListener {
                        // TODO: Test API connection
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("About")
                    .addText("App version: ${BuildConfig.VERSION_NAME}")
                    .setOnClickListener {
                        // Show more app information
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Privacy Policy")
                    .setOnClickListener {
                        // Show privacy policy
                    }
                    .build()
            )

        val titleSuffix = CarDistractionOptimizer.getTitleSuffix(carContext, parkingOnlyFeature = true)

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle("Settings$titleSuffix")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .build()
    }
}