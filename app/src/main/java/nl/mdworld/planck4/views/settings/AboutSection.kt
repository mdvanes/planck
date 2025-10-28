package nl.mdworld.planck4.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.mdworld.planck4.BuildConfig

/**
 * Displays application version/build info inside a SettingsSection.
 */
@Composable
fun AboutSection(
    isDebugEnabled: Boolean,
    onDebugEnabledChange: (Boolean) -> Unit
) {
    SettingsSection(title = "About") {
        SettingsItem(
            label = "Version",
            value = BuildConfig.VERSION_NAME
        )
        SettingsItem(
            label = "Build",
            value = if (BuildConfig.DEBUG) "Debug" else "Release"
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Debug Mode")
            }
            Switch(checked = isDebugEnabled, onCheckedChange = onDebugEnabledChange)
        }
    }
}

