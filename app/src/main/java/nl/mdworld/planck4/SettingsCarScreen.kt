package nl.mdworld.planck4

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

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

        val titleSuffix = CarDistractionOptimizer.getTitleSuffix(carContext, parkingOnlyFeature = true)

        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle("Server Settings")
                            .setOnClickListener {
                                // Handle server settings
                                // In parking mode only - can show detailed configuration
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("Audio Settings")
                            .setOnClickListener {
                                // Handle audio settings
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("Display Settings")
                            .setOnClickListener {
                                // Handle display settings
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("Account Settings")
                            .setOnClickListener {
                                // Handle account settings
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("About")
                            .setOnClickListener {
                                // Show about information
                            }
                            .build()
                    )
                    .build()
            )
            .setHeader(
                Header.Builder()
                    .setTitle("Settings$titleSuffix")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .build()
    }
}
