package nl.mdworld.planck4

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import nl.mdworld.planck4.networking.SubsonicApi
import nl.mdworld.planck4.networking.SubsonicPlaylistsResponse

@Composable
fun PlanckApp(
    appState: PlanckAppState = rememberPlanckAppState()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val response: SubsonicPlaylistsResponse = SubsonicApi().getPlaylistsKtor(context)
            val playlistStrings: List<String> = response.sr.playlists.playlist.map {
                "${it.id} - ${it.name} (${it.coverArt})"
            }
            println(playlistStrings.joinToString(","))
            val newPlaylists: List<Playlist> =
                response.sr.playlists.playlist.map { Playlist(it.id, it.coverArt, it.name) }
            appState.playlists.clear()
            appState.playlists.addAll(newPlaylists)
        } catch (e: Exception) {
            println("PlanckApp: Failed to call API:$e")
        }
    }

    // Load songs when navigating to song view
    LaunchedEffect(appState.selectedPlaylistId, appState.currentScreen) {
        if (appState.selectedPlaylistId != null && appState.currentScreen == AppScreen.SONGS) {
            appState.songs.clear()
            try {
                val response = SubsonicApi().getPlaylistKtor(context, appState.selectedPlaylistId!!)
                val songs = response.sr.playlist.songs?.map { song ->
                    Song(
                        id = song.id,
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        duration = song.duration,
                        coverArt = song.coverArt
                    )
                } ?: emptyList()

                appState.songs.addAll(songs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Failed to load playlist songs: $e")
            }
        }
    }

    Scaffold(
        bottomBar = {
            PlanckBottomAppBar(
                currentScreen = appState.currentScreen,
                onNavigateBack = { appState.navigateToPlaylists() },
                activeSong = appState.activeSong,
                onNavigateToSettings = { appState.navigateToSettings() }
            )
        }
    ) { innerPadding ->
        // TODO how to remove the unused innerPadding param?
        println(innerPadding)
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (appState.currentScreen) {
                AppScreen.PLAYLISTS -> {
                    PlaylistCardList(appState.playlists, appState)
                }
                AppScreen.SONGS -> {
                    SongCardList(
                        songs = appState.songs.toList(),
                        playlistTitle = appState.selectedPlaylistName ?: "Playlist",
                        onSongClick = { song -> appState.activeSong = song }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        onNavigateBack = { appState.navigateToPlaylists() }
                    )
                }
            }
        }
    }
}
