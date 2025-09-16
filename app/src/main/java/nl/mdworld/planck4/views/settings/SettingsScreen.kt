package nl.mdworld.planck4.views.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import nl.mdworld.planck4.BuildConfig
import nl.mdworld.planck4.KeyCodeTracker
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current

    // Load current values from settings
    var serverUrl by remember { mutableStateOf(SettingsManager.getServerUrl(context)) }
    var username by remember { mutableStateOf(SettingsManager.getUsername(context)) }
    var password by remember { mutableStateOf(SettingsManager.getPassword(context)) }
    var radioUrl by remember { mutableStateOf(SettingsManager.getRadioUrl(context)) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    // Track changes to enable save button
    LaunchedEffect(serverUrl, username, password, radioUrl) {
        hasUnsavedChanges = serverUrl != SettingsManager.getServerUrl(context) ||
                username != SettingsManager.getUsername(context) ||
                password != SettingsManager.getPassword(context) ||
                radioUrl != SettingsManager.getRadioUrl(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider()

        // Subsonic Settings Section
        SettingsSection(
            title = "Subsonic Settings"
        ) {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://your-server.com/rest/") }
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("your_username") }
            )

            var passwordVisibility by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("random_password") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Text(
                            text = if (passwordVisibility) "Hide" else "Show",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            OutlinedTextField(
                value = radioUrl,
                onValueChange = { radioUrl = it },
                label = { Text("Radio URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://your-radio.com/stream/") }
            )

            // TODO leave this toggle for now, until caching is implemented
            //var cacheEnabled by remember { mutableStateOf(true) }
            //SettingsSwitch(
            //    label = "Enable Caching",
            //    checked = cacheEnabled,
            //    onCheckedChange = { cacheEnabled = it }
            //)

            // Save button with reload functionality
            Button(
                onClick = {
                    // Save all settings
                    SettingsManager.saveServerUrl(context, serverUrl)
                    SettingsManager.saveUsername(context, username)
                    SettingsManager.savePassword(context, password)
                    SettingsManager.saveRadioUrl(context, radioUrl)
                    hasUnsavedChanges = false

                    // Trigger data reload with new settings
                    appState?.triggerReload()
                },
                enabled = hasUnsavedChanges,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }

        // Debug Section - Key Code Tracker
        SettingsSection(
            title = "Debug - Key Codes"
        ) {
            if (KeyCodeTracker.keyCodes.isEmpty()) {
                SettingsItem(
                    label = "No key codes pressed yet",
                    value = "Press car buttons to see them here"
                )
            } else {
                KeyCodeTracker.keyCodes.forEachIndexed { index, keyCode ->
                    SettingsItem(
                        label = "${index + 1}.",
                        value = keyCode
                    )
                }
            }

            // Test button to verify UI updates
            Button(
                onClick = {
                    KeyCodeTracker.addKeyCode("999", "TEST_BUTTON")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Test Key Code")
            }

            // Clear button
            Button(
                onClick = { KeyCodeTracker.clear() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Key Codes")
            }
        }

        // About Section
        SettingsSection(
            title = "About"
        ) {
            SettingsItem(
                label = "Version",
                value = BuildConfig.VERSION_NAME
            )

            SettingsItem(
                label = "Build",
                value = if (BuildConfig.DEBUG) "Debug" else "Release"
            )

            SettingsItem(
                label = "Padding",
                value = "Padding",
            )

            SettingsItem(
                label = "Padding",
                value = "Padding",
            )
        }
    }
}
