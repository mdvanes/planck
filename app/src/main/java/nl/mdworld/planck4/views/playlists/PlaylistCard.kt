package nl.mdworld.planck4.views.playlists

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.setSelectedPlaylist
import nl.mdworld.planck4.ui.theme.PlanckTheme
import nl.mdworld.planck4.imageloading.CoverArtRequests

@Composable
fun PlaylistCard(playlist: Playlist, appState: PlanckAppState? = null) {
    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(all = 8.dp)
        .clickable {
            println("Clicked on playlist ${playlist.name}")
            setSelectedPlaylist(context, playlist.name)
            appState?.navigateToSongs(playlist.id, playlist.name)
        }
    ) {
        AsyncImage(
            model = CoverArtRequests.buildCoverArtRequest(context, playlist.coverArt),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = playlist.name,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp
            )
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
