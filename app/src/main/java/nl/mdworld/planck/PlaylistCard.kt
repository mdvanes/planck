package nl.mdworld.planck

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck.networking.SubsonicApi
import nl.mdworld.planck.networking.SubsonicPlaylistsResponse
import nl.mdworld.planck.networking.SubsonicTemp
import nl.mdworld.planck.networking.ktorHttpClient
import nl.mdworld.planck.ui.theme.PlanckTheme

data class Playlist(val id: String, val coverArt: String, val name: String)

// TODO fix this SuppressLint rule
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PlaylistCard(playlist: Playlist, appState: PlanckAppState? = null) {
    val context = LocalContext.current
    val playerName = R.string.subsonic_player_name
    val apiConfig =
        "?u=${SubsonicTemp.JUKEBOX_USERNAME}&t=${SubsonicTemp.JUKEBOX_API_TOKEN}&s=${SubsonicTemp.JUKEBOX_SALT}&v=1.16.0&c=${playerName}&f=json"

    //val coroutineScope = rememberCoroutineScope()
    //val (coverArtId, setCoverArtId) = remember { mutableStateOf<String?>(null) }
    //
    //coroutineScope.launch {
    //    val playlistResponse: SubsonicPlaylistResponse = SubsonicApi().getPlaylistKtor("28")
    //    println(playlistResponse)
    //}

    // Add padding around our message
    Row(modifier = Modifier
        .padding(all = 8.dp)
        .clickable {
            println("Clicked on playlist ${playlist.name}")
            setSelectedPlaylist(context, playlist.name)
            appState?.navigateToSongs(playlist.id, playlist.name)
        }
    ) {
        //Image(
        //    painter = painterResource(R.drawable.ic_launcher_background),
        //    contentDescription = "Contact profile picture",
        //    modifier = Modifier
        //        // Set image size to 40 dp
        //        .size(40.dp)
        //        // Clip image to be shaped as a circle
        //        .clip(CircleShape)
        //        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        //)
        AsyncImage(
            model = "${SubsonicTemp.JUKEBOX_BASE_URL}/getCoverArt${apiConfig}&id=${playlist.coverArt}",
            contentDescription = null,
            modifier = Modifier
                // Set image size to some dp
                .size(100.dp)
                // Clip image to be shaped as a circle
                .clip(CircleShape)
                //.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        // Add a horizontal space between the image and the column
        Spacer(modifier = Modifier.width(16.dp))

        // We keep track if the message is expanded or not in this variable
        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            label = "surfaceColor",
        )

        Column( modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = playlist.name,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp
            )
            // Add a vertical space between the author and message texts
            //Spacer(modifier = Modifier.height(4.dp))
            //Surface(
            //    shape = MaterialTheme.shapes.medium,
            //    shadowElevation = 1.dp,
            //    color = surfaceColor,
            //    modifier = Modifier
            //        .animateContentSize()
            //        .padding(1.dp)
            //) {
            //    Text(
            //        text = playlist.name,
            //        modifier = Modifier.padding(all = 4.dp),
            //        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
            //        style = MaterialTheme.typography.titleSmall
            //    )
            //}
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewPlaylistCard() {
    PlanckTheme {
        Surface {
            PlaylistCard(
                playlist = Playlist("1", "cover-art-url", "Playlist 1")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCardList(playlists: MutableList<Playlist>, appState: PlanckAppState? = null, modifier: Modifier = Modifier) {

    Box(modifier = modifier.padding(0.dp, 0.dp, 0.dp, 80.dp)) {

        //val playlists2 = mutableListOf<Playlist>(
        //    Playlist(
        //        "Empty",
        //        "No Playlist"
        //    )
        //)

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
                val newPlaylists = mutableListOf<Playlist>(
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
        PlaylistCardList(SampleData.playlistsSample)
    }
}
