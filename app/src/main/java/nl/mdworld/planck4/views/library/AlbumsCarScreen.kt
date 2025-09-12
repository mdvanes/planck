package nl.mdworld.planck4.views.library

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
import nl.mdworld.planck4.networking.subsonic.SubsonicApi

class AlbumsCarScreen(
    carContext: CarContext,
    private val artistId: String,
    private val artistName: String
) : Screen(carContext) {

    private val albums = mutableListOf<Album>()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onGetTemplate(): Template {
        // Load albums from the same API your main app uses
        if (albums.isEmpty()) {
            loadAlbums()
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

        // Show albums (limit to maxItems when driving for safety)
        val albumsToShow = if (isDriving) {
            albums.take(maxItems)
        } else {
            albums
        }

        albumsToShow.forEach { album ->
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle(album.name)
                    .addText("${album.songCount} songs • ${album.year ?: "Unknown year"}")
                    .setOnClickListener {
                        // Navigate to songs in this album
                        screenManager.push(AlbumSongsCarScreen(carContext, album.id, album.name))
                    }
                    .build()
            )
        }

        // If no albums loaded yet, show loading message
        if (albums.isEmpty()) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Loading albums...")
                    .build()
            )
        }

        val actionStripBuilder = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Refresh")
                    .setOnClickListener {
                        albums.clear()
                        loadAlbums()
                        invalidate()
                    }
                    .build()
            )

        val titleSuffix = CarDistractionOptimizer.getTitleSuffix(carContext, parkingOnlyFeature = false)

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle("$artistName Albums$titleSuffix")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .setActionStrip(actionStripBuilder.build())
            .build()
    }

    private fun loadAlbums() {
        scope.launch {
            try {
                val response = SubsonicApi().getArtistKtor(carContext, artistId)
                val newAlbums = response.sr.artist.album.map { albumEntity ->
                    Album(
                        id = albumEntity.id,
                        name = albumEntity.name,
                        artist = albumEntity.artist,
                        artistId = albumEntity.artistId,
                        songCount = albumEntity.songCount,
                        duration = albumEntity.duration,
                        coverArt = albumEntity.coverArt,
                        year = albumEntity.year
                    )
                }
                albums.clear()
                albums.addAll(newAlbums)
                invalidate() // Refresh the screen with new data
            } catch (e: Exception) {
                println("CarScreen: Failed to load albums: $e")
                // Add error item
                albums.clear()
                albums.add(Album("error", "Failed to load albums", "", "", 0, 0, "", null))
                invalidate()
            }
        }
    }
}
