package nl.mdworld.planck4.views.components

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
import nl.mdworld.planck4.R
import nl.mdworld.planck4.SettingsManager

@Composable
fun CoverArt(
    coverArtId: String?,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerName = R.string.subsonic_player_name
    val apiConfig =
        "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"

    Box(modifier = modifier.size(size)) {
        if (coverArtId != null) {
            AsyncImage(
                model = "${SettingsManager.getJukeboxBaseUrl(context)}/getCoverArt${apiConfig}&id=${coverArtId}",
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            Surface(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.surface
            ) {}
        }
    }
}
