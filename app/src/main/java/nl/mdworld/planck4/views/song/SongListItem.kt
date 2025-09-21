package nl.mdworld.planck4.views.song

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SongListItem(
    song: Song,
    index: Int,
    isCurrentlyPlaying: Boolean = false,
    onClick: (Song) -> Unit = {}
) {
    val durationText = if (song.duration != null) {
        val minutes = song.duration / 60
        val seconds = song.duration % 60
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    } else {
        ""
    }

    val backgroundColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    val textColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val artistTextColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(song) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCurrentlyPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Currently Playing",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Text(
                text = "${index + 1}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 24.sp,
                modifier = Modifier.width(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp,
                fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (song.artist != null) {
                Text(
                    text = song.artist,
                    color = artistTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (durationText.isNotEmpty()) {
            Text(
                text = durationText,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp
            )
        }
    }
}