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

class PlaylistsCarScreen(carContext: CarContext) : Screen(carContext) {

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
            .addItem(
                Row.Builder()
                    .setTitle("My Music")
                    .setOnClickListener {
                        // Handle playlist selection - always available
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Favorites")
                    .setOnClickListener {
                        // Handle playlist selection - always available
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Recently Played")
                    .setOnClickListener {
                        // Handle playlist selection - always available
                    }
                    .build()
            )

        // Add more playlists only when not driving or within driving limits
        if (!isDriving || maxItems > 3) {
            itemListBuilder
                .addItem(
                    Row.Builder()
                        .setTitle("Rock Classics")
                        .setOnClickListener {
                            // Handle playlist selection
                        }
                        .build()
                )
                .addItem(
                    Row.Builder()
                        .setTitle("Jazz Collection")
                        .setOnClickListener {
                            // Handle playlist selection
                        }
                        .build()
                )
        }

        val actionStripBuilder = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Refresh")
                    .setOnClickListener {
                        // Refresh playlists
                        invalidate()
                    }
                    .build()
            )

        // Add search action only when parked
        if (CarDistractionOptimizer.isFeatureAvailable(carContext, requiresParking = true)) {
            actionStripBuilder.addAction(
                Action.Builder()
                    .setTitle("Search")
                    .setOnClickListener {
                        // Open search - only available when parked
                    }
                    .build()
            )
        }

        val titleSuffix = CarDistractionOptimizer.getTitleSuffix(carContext, parkingOnlyFeature = false)

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle("Playlists$titleSuffix")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .setActionStrip(actionStripBuilder.build())
            .build()
    }
}
