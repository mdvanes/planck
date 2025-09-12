package nl.mdworld.planck4.views.song

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck4.ui.theme.PlanckTheme
import nl.mdworld.planck4.views.components.CoverArt

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Int?,
    val coverArt: String?
)

@Composable
fun SongCard(song: Song) {
    Row(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        CoverArt(coverArtId = song.coverArt)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.height(80.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = song.title,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp
            )

            if (song.artist != null) {
                Text(
                    text = song.artist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp
                )
            }

            if (song.album != null) {
                Text(
                    text = song.album,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewSongCard() {
    PlanckTheme {
        Surface {
            SongCard(
                song = Song(
                    id = "1",
                    title = "Example Song",
                    artist = "Example Artist",
                    album = "Example Album",
                    duration = 240,
                    coverArt = "cover-art-id"
                )
            )
        }
    }
}


