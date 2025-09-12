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
import nl.mdworld.planck4.views.song.Song

class SongsCarScreen(
    carContext: CarContext,
    private val playlistId: String,
    private val playlistName: String
) : Screen(carContext) {

    private val songs = mutableListOf<Song>()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onGetTemplate(): Template {
        // Load songs from the same API your main app uses
        if (songs.isEmpty()) {
            loadSongs()
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

        // Show songs (limit to maxItems when driving for safety)
        val songsToShow = if (isDriving) {
            songs.take(maxItems)
        } else {
            songs
        }

        songsToShow.forEach { song ->
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle(song.title)
                    .addText(song.artist ?: "Unknown Artist")
                    .setOnClickListener {
                        // TODO: Implement song playback
                        // This would integrate with your existing media playback logic
                    }
                    .build()
            )
        }

        // If no songs loaded yet, show loading message
        if (songs.isEmpty()) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Loading songs...")
                    .build()
            )
        }

        val actionStripBuilder = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Refresh")
                    .setOnClickListener {
                        songs.clear()
                        loadSongs()
                        invalidate()
                    }
                    .build()
            )

        val titleSuffix = CarDistractionOptimizer.getTitleSuffix(carContext, parkingOnlyFeature = false)

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle("$playlistName$titleSuffix")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .setActionStrip(actionStripBuilder.build())
            .build()
    }

    private fun loadSongs() {
        scope.launch {
            try {
                val response = SubsonicApi().getPlaylistKtor(carContext, playlistId)
                val newSongs = response.sr.playlist.songs?.map { song ->
                    Song(
                        id = song.id,
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        duration = song.duration,
                        coverArt = song.coverArt
                    )
                } ?: emptyList()

                songs.clear()
                songs.addAll(newSongs)
                invalidate() // Refresh the screen with new data
            } catch (e: Exception) {
                println("CarScreen: Failed to load songs: $e")
                // Add error item
                songs.clear()
                songs.add(Song("error", "Failed to load songs", "", "", 0, ""))
                invalidate()
            }
        }
    }
}
