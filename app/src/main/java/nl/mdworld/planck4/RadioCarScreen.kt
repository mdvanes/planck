package nl.mdworld.planck4

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class RadioCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val isDriving = CarDistractionOptimizer.isDriving(carContext)
        val maxItems = CarDistractionOptimizer.getMaxListItems(carContext)

        val backAction = Action.Builder()
            .setTitle("Back")
            .setOnClickListener {
                screenManager.pop()
            }
            .build()

        val itemListBuilder = ItemList.Builder()
            // Core radio stations - always available for quick access while driving
            .addItem(
                Row.Builder()
                    .setTitle("Radio 2")
                    .setOnClickListener {
                        // Handle radio station selection
                        // This could integrate with your existing radio functionality
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("NPO Radio 1")
                    .setOnClickListener {
                        // Handle radio station selection
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("3FM")
                    .setOnClickListener {
                        // Handle radio station selection
                    }
                    .build()
            )

        // Add more stations only when constraints allow (not driving or more items allowed)
        if (!isDriving || maxItems > 4) {
            itemListBuilder
                .addItem(
                    Row.Builder()
                        .setTitle("Radio Veronica")
                        .setOnClickListener {
                            // Handle radio station selection
                        }
                        .build()
                )
                .addItem(
                    Row.Builder()
                        .setTitle("Sky Radio")
                        .setOnClickListener {
                            // Handle radio station selection
                        }
                        .build()
                )
        }

        val actionStripBuilder = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Favorites")
                    .setOnClickListener {
                        // Show favorite stations - always available
                    }
                    .build()
            )

        // Add more complex actions only when parked
        if (CarDistractionOptimizer.isFeatureAvailable(carContext, requiresParking = true)) {
            actionStripBuilder
                .addAction(
                    Action.Builder()
                        .setTitle("Browse")
                        .setOnClickListener {
                            // Browse all stations - only when parked
                        }
                        .build()
                )
                .addAction(
                    Action.Builder()
                        .setTitle("Search")
                        .setOnClickListener {
                            // Search stations - only when parked
                        }
                        .build()
                )
        }

        val titleSuffix = CarDistractionOptimizer.getTitleSuffix(carContext, parkingOnlyFeature = false)

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle("Radio Stations$titleSuffix")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .setActionStrip(actionStripBuilder.build())
            .build()
    }
}
