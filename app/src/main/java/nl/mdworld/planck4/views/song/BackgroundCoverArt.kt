package nl.mdworld.planck4.views.song

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder

@Composable
fun BackgroundCoverArt(
    coverArtId: String?,
    modifier: Modifier = Modifier,
    blurRadius: Int = 20,
    overlayAlphaTop: Float = 0.5f,
    overlayAlphaBottom: Float = 0.9f
) {
    val context = LocalContext.current

    if (coverArtId != null) {
        // Background cover art image
        AsyncImage(
            model = SubsonicUrlBuilder.buildCoverArtUrl(context, coverArtId),
            contentDescription = null,
            modifier = modifier
                .fillMaxSize()
                .blur(blurRadius.dp),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay to improve readability
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = overlayAlphaTop),
                            Color.Black.copy(alpha = overlayAlphaBottom)
                        )
                    )
                )
        )
    }
}
