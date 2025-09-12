package nl.mdworld.planck4.views.song

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import nl.mdworld.planck4.R
import nl.mdworld.planck4.SettingsManager

@Composable
fun SongsHeaderCard(playlistTitle: String, coverArt: String?) {
    val context = LocalContext.current
    val playerName = context.getString(R.string.subsonic_player_name)
    val apiConfig =
        "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${
            SettingsManager.getSalt(
                context
            )
        }&v=1.16.0&c=${playerName}&f=json"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = if (coverArt != null)
                "${SettingsManager.getJukeboxBaseUrl(context)}/getCoverArt${apiConfig}&id=${coverArt}"
            else null,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(
                text = playlistTitle,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
    }
}