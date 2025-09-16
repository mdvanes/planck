package nl.mdworld.planck4

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import nl.mdworld.planck4.views.library.ArtistsCarScreen
import nl.mdworld.planck4.views.playlists.PlaylistsCarScreen
import nl.mdworld.planck4.views.settings.SettingsCarScreen

class MediaCarScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    init {
        // Add this screen as a lifecycle observer
        lifecycle.addObserver(this)
        Log.d("MediaCarScreen", "MediaCarScreen initialized - ready to detect rotary button clicks")
    }

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Playlists")
                    .setBrowsable(true)
                    .setOnClickListener {
                        Log.d("MediaCarScreen", "🎵 CAR ROTARY CONFIRM BUTTON CLICKED - Playlists selected!")
                        screenManager.push(PlaylistsCarScreen(carContext))
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Artists")
                    .setBrowsable(true)
                    .setOnClickListener {
                        Log.d("MediaCarScreen", "🎵 CAR ROTARY CONFIRM BUTTON CLICKED - Artists selected!")
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
                        Log.d("MediaCarScreen", "🎵 CAR ROTARY CONFIRM BUTTON CLICKED - Settings selected!")
                        screenManager.push(SettingsCarScreen(carContext))
                    }
                    .build()
            )
        }

        val refreshAction = Action.Builder()
            .setTitle("Refresh")
            .setOnClickListener {
                Log.d("MediaCarScreen", "🎵 CAR ROTARY CONFIRM BUTTON CLICKED - Refresh action!")
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

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d("MediaCarScreen", "MediaCarScreen destroyed")
    }
}
