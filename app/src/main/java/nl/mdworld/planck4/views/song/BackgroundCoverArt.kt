package nl.mdworld.planck4.views.song

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder

/**
 * Displays a blurred, darkened cover art as a background.
 * Provide either [coverArtUrl] (takes precedence) or [coverArtId] (converted via SubsonicUrlBuilder).
 */
@Composable
fun BackgroundCoverArt(
    modifier: Modifier = Modifier,
    coverArtId: String? = null,
    coverArtUrl: String? = null,
    blurRadius: Int = 20,
    overlayAlphaTop: Float = 0.8f,
    overlayAlphaBottom: Float = 0.8f
) {
    val context = LocalContext.current

    // Determine final model URL: explicit URL wins; else build from id; else null (no background)
    val model: String? = when {
        coverArtUrl != null -> coverArtUrl
        coverArtId != null -> SubsonicUrlBuilder.buildCoverArtUrl(context, coverArtId)
        else -> null
    }

    if (model != null) {
        // Background cover art image
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = modifier
                .fillMaxSize(),
                //.blur(blurRadius.dp),
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
