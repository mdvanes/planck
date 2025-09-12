package nl.mdworld.planck4.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder

@Composable
fun CoverArt(
    coverArtId: String?,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier.size(size)) {
        if (coverArtId != null) {
            AsyncImage(
                model = SubsonicUrlBuilder.buildCoverArtUrl(context, coverArtId),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
        } else {
            Surface(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                color = MaterialTheme.colorScheme.surface
            ) {}
        }
    }
}
