package nl.mdworld.planck4.views.playlists

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.mdworld.planck4.CarDistractionOptimizer
import nl.mdworld.planck4.views.song.SongsCarScreen
import nl.mdworld.planck4.networking.subsonic.SubsonicApi

class PlaylistsCarScreen(carContext: CarContext) : Screen(carContext) {
    private val playlists = mutableListOf<Playlist>()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onGetTemplate(): Template {
        // Load playlists from the same API your main app uses
        if (playlists.isEmpty()) {
            loadPlaylists()
        }

        val isDriving = CarDistractionOptimizer.isDriving(carContext)
        val maxItems = CarDistractionOptimizer.getMaxListItems(carContext)

        val backAction = Action.Builder()
            .setTitle("Back")
            .setOnClickListener {
                screenManager.pop()
            }
            .build()

        val itemListBuilder = ItemList.Builder()

        // Show playlists (limit to maxItems when driving for safety)
        val playlistsToShow = if (isDriving) {
            playlists.take(maxItems)
        } else {
            playlists
        }

        playlistsToShow.forEach { playlist ->
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle(playlist.name)
                    .setOnClickListener {
                        // Navigate to songs in this playlist
                        screenManager.push(SongsCarScreen(carContext, playlist.id, playlist.name))
                    }
                    .build()
            )
        }

        // If no playlists loaded yet, show loading message
        if (playlists.isEmpty()) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Loading playlists...")
                    .build()
            )
        }

        val actionStripBuilder = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Refresh")
                    .setOnClickListener {
                        playlists.clear()
                        loadPlaylists()
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
                        // TODO: Implement search functionality
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

    private fun loadPlaylists() {
        scope.launch {
            try {
                val response = SubsonicApi().getPlaylistsKtor(carContext)
                val newPlaylists = response.sr.playlists.playlist.map {
                    Playlist(it.id, it.coverArt, it.name)
                }
                playlists.clear()
                playlists.addAll(newPlaylists)
                invalidate() // Refresh the screen with new data
            } catch (e: Exception) {
                println("CarScreen: Failed to load playlists: $e")
                // Add error item
                playlists.clear()
                playlists.add(Playlist("error", "", "Failed to load playlists"))
                invalidate()
            }
        }
    }
}