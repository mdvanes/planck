package nl.mdworld.planck

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import nl.mdworld.planck.networking.SubsonicApi
import nl.mdworld.planck.networking.SubsonicPlaylistsResponse
import nl.mdworld.planck.networking.ktorHttpClient

// Example: https://github.com/android/compose-samples/blob/main/Jetcaster/app/src/main/java/com/example/jetcaster/ui/JetcasterAppState.kt

@Composable
fun PlanckApp(
    appState: PlanckAppState = rememberPlanckAppState()
) {

    LaunchedEffect(Unit) {
        try {
            val response: SubsonicPlaylistsResponse = SubsonicApi().getPlaylistsKtor()
            val playlistStrings: List<String> = response.sr.playlists.playlist.map {
                "${it.id} - ${it.name} (${it.coverArt})"
            }
            println(playlistStrings.joinToString(","))
            val newPlaylists: List<Playlist> =
                response.sr.playlists.playlist.map { Playlist(it.coverArt, it.name) }
            appState.playlists.clear()
            appState.playlists.addAll(newPlaylists)
        } catch (e: Exception) {
            println("PlanckApp: Failed to call API:$e")
        } finally {
            ktorHttpClient.close()
        }
    }

    Scaffold(
        bottomBar = {
            PlanckBottomAppBar()
        }
    ) { innerPadding ->
        // TODO how to remove the unused innerPadding param?
        println(innerPadding)
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlaylistCardList(appState.playlists)
        }
    }
}