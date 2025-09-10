package nl.mdworld.planck4.views.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.SampleData
import nl.mdworld.planck4.networking.ktorHttpClient
import nl.mdworld.planck4.ui.theme.PlanckTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCardList(modifier: Modifier = Modifier, playlists: MutableList<Playlist>, appState: PlanckAppState? = null) {

    Box(modifier = modifier.padding(0.dp, 0.dp, 0.dp, 80.dp)) {

        // https://dev.to/andreym/how-to-do-a-material-3-pull-refresh-15b0
        val isRefreshing by remember {
            mutableStateOf(false)
        }
        val state = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
            println("Triggered by PullRefresh")

            try {
                //val response: SubsonicPlaylistsResponse = SubsonicApi().getPlaylistsKtor()
                //val playlistStrings: List<String> = response.sr.playlists.playlist.map {
                //    "${it.id} - ${it.name} (${it.coverArt})"
                //}
                //println(playlistStrings.joinToString(","))
                //val newPlaylists: List<Playlist> =
                //    response.sr.playlists.playlist.map { Playlist(it.coverArt, it.name) }
                //playlists2.clear()
                // TODO even this temporary setter does not work!
                val newPlaylists = mutableListOf(
                    Playlist(
                        "empty1",
                        "Empty1",
                        "No Playlist1"
                    )
                )
                //playlists2.addAll(newPlaylists)
                println(newPlaylists)
                playlists.clear()
                playlists.addAll(newPlaylists)
            } catch (e: Exception) {
                println("Failed to call API:$e")
            } finally {
                ktorHttpClient.close()
            }
        })

        LazyColumn(modifier = Modifier.pullRefresh(state)) {
            items(playlists) { playlist ->
                PlaylistCard(playlist, appState)
            }
        }
        PullRefreshIndicator(refreshing = isRefreshing, state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
    }
}

@Preview
@Composable
fun PreviewPlaylistCardList() {
    PlanckTheme {
        PlaylistCardList(playlists = SampleData.playlistsSample)
    }
}
