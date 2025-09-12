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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.mdworld.planck4.networking.subsonic.SubsonicApi
import nl.mdworld.planck4.views.library.Artist

class ArtistsCarScreen(carContext: CarContext) : Screen(carContext) {
    private val artists = mutableListOf<Artist>()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onGetTemplate(): Template {
        // Load artists from the same API your main app uses
        if (artists.isEmpty()) {
            loadArtists()
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

        // Show artists (limit to maxItems when driving for safety)
        val artistsToShow = if (isDriving) {
            artists.take(maxItems)
        } else {
            artists
        }

        artistsToShow.forEach { artist ->
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle(artist.name)
                    .addText("${artist.albumCount} albums")
                    .setOnClickListener {
                        // Navigate to albums for this artist
                        screenManager.push(AlbumsCarScreen(carContext, artist.id, artist.name))
                    }
                    .build()
            )
        }

        // If no artists loaded yet, show loading message
        if (artists.isEmpty()) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Loading artists...")
                    .build()
            )
        }

        val actionStripBuilder = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Refresh")
                    .setOnClickListener {
                        artists.clear()
                        loadArtists()
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
                    .setTitle("Artists$titleSuffix")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .setActionStrip(actionStripBuilder.build())
            .build()
    }

    private fun loadArtists() {
        scope.launch {
            try {
                val response = SubsonicApi().getArtistsKtor(carContext)
                val newArtists = response.sr.artists.index.flatMap { index ->
                    index.artist.map { artistEntity ->
                        Artist(
                            id = artistEntity.id,
                            name = artistEntity.name,
                            albumCount = artistEntity.albumCount,
                            coverArt = artistEntity.coverArt
                        )
                    }
                }
                artists.clear()
                artists.addAll(newArtists)
                invalidate() // Refresh the screen with new data
            } catch (e: Exception) {
                println("CarScreen: Failed to load artists: $e")
                // Add error item
                artists.clear()
                artists.add(Artist("error", "Failed to load artists", 0, ""))
                invalidate()
            }
        }
    }
}
