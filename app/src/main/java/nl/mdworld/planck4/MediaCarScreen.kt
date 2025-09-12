package nl.mdworld.planck4

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class MediaCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Playlists")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(PlaylistsCarScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Artists")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(ArtistsCarScreen(carContext))
                    }
                    .build()
            )

        // Only add settings when not driving (parked mode)
        if (CarDistractionOptimizer.isFeatureAvailable(carContext, requiresParking = true)) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Settings")
                    .setBrowsable(true)
                    .setOnClickListener {
                        screenManager.push(SettingsCarScreen(carContext))
                    }
                    .build()
            )
        }

        val refreshAction = Action.Builder()
            .setTitle("Refresh")
            .setOnClickListener {
                // Refresh data
                invalidate()
            }
            .build()

        val titleSuffix = CarDistractionOptimizer.getTitleSuffix(carContext, parkingOnlyFeature = false)

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle("Planck Media Player$titleSuffix")
                    .addEndHeaderAction(refreshAction)
                    .build()
            )
            .build()
    }
}
