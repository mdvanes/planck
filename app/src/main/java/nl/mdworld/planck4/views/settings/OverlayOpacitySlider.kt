package nl.mdworld.planck4.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import nl.mdworld.planck4.SettingsManager

/**
 * Slider control for the background overlay opacity (0f..1f) in 5% increments.
 * Persists changes immediately and reports the quantized value via [onValueChange].
 */
@Composable
fun OverlayOpacitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(
            text = "Background Overlay Opacity: ${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = value,
            onValueChange = { newValue ->
                val clamped = newValue.coerceIn(0f, 1f)
                val quantized = (clamped * 20f).roundToInt() / 20f  // 0.00, 0.05, 0.10, ... 1.00
                onValueChange(quantized)
                SettingsManager.saveOverlayOpacity(context, quantized)
            },
            valueRange = 0f..1f,
            steps = 19 // 20 intervals -> 5% increments
        )
    }
}

