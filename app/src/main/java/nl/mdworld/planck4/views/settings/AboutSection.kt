package nl.mdworld.planck4.views.settings

import androidx.compose.runtime.Composable
import nl.mdworld.planck4.BuildConfig

/**
 * Displays application version/build info inside a SettingsSection.
 */
@Composable
fun AboutSection() {
    SettingsSection(title = "About") {
        SettingsItem(
            label = "Version",
            value = BuildConfig.VERSION_NAME
        )
        SettingsItem(
            label = "Build",
            value = if (BuildConfig.DEBUG) "Debug" else "Release"
        )
    }
}

