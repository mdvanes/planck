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
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.imageloading.CoverArtRequests

/**
 * Displays a blurred, darkened cover art as a background.
 * Provide either [coverArtUrl] (takes precedence) or [coverArtId] (converted via SubsonicUrlBuilder).
 * The overlay opacity is loaded from settings unless explicitly overridden.
 */
@Composable
fun BackgroundCoverArt(
    modifier: Modifier = Modifier,
    coverArtId: String? = null,
    coverArtUrl: String? = null,
    blurRadius: Int = 20,
    overlayAlpha: Float? = null
) {
    val context = LocalContext.current

    // Determine final model URL: explicit URL wins; else build from id; else null (no background)
    val model: Any? = when {
        coverArtUrl != null -> coverArtUrl
        coverArtId != null -> CoverArtRequests.buildCoverArtRequest(context, coverArtId)
        else -> null
    }

    val finalAlpha = (overlayAlpha ?: SettingsManager.getOverlayOpacity(context)).coerceIn(0f, 1f)

    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = finalAlpha),
                            Color.Black.copy(alpha = finalAlpha)
                        )
                    )
                )
        )
    }
}
