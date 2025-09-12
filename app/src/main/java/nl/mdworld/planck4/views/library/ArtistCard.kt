package nl.mdworld.planck4.views.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mdworld.planck4.PlanckAppState

@Composable
fun ArtistCard(artist: Artist, appState: PlanckAppState? = null) {

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 64.dp)
            .height(100.dp)
            .clickable {
                println("Clicked on artist ${artist.name}")
                appState?.navigateToAlbums(artist.id, artist.name)
            }
    ) {


        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = artist.name,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (artist.albumCount > 0) {
                Text(
                    text = "${artist.albumCount} albums",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
