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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.SettingsManager.BrowsingMode
import nl.mdworld.planck4.imageloading.CoverArtCacheManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load current values from settings
    var serverUrl by remember { mutableStateOf(SettingsManager.getServerUrl(context)) }
    var username by remember { mutableStateOf(SettingsManager.getUsername(context)) }
    var password by remember { mutableStateOf(SettingsManager.getPassword(context)) }
    var radioUrl by remember { mutableStateOf(SettingsManager.getRadioUrl(context)) }
    var overlayOpacity by remember { mutableStateOf(SettingsManager.getOverlayOpacity(context)) }
    var browsingMode by remember { mutableStateOf(SettingsManager.getBrowsingMode(context)) }
    var folderCountEnrich by remember { mutableStateOf(SettingsManager.getFolderCountEnrichmentEnabled(context)) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    // Album art cache state
    var cacheSizeText by remember { mutableStateOf("Calculating…") }
    fun refreshCacheSize() {
        scope.launch {
            val size = CoverArtCacheManager.sizeBytesAsync(context)
            cacheSizeText = CoverArtCacheManager.formatSize(size)
        }
    }
    LaunchedEffect(true) { refreshCacheSize() }

    // Track changes to enable save button (overlayOpacity auto-saves, so exclude)
    LaunchedEffect(serverUrl, username, password, radioUrl, browsingMode, folderCountEnrich) {
        hasUnsavedChanges = serverUrl != SettingsManager.getServerUrl(context) ||
                username != SettingsManager.getUsername(context) ||
                password != SettingsManager.getPassword(context) ||
                radioUrl != SettingsManager.getRadioUrl(context) ||
                browsingMode != SettingsManager.getBrowsingMode(context) ||
                folderCountEnrich != SettingsManager.getFolderCountEnrichmentEnabled(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
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

            // Extracted overlay opacity slider component
            OverlayOpacitySlider(
                value = overlayOpacity,
                onValueChange = { overlayOpacity = it }
            )

            // Browsing mode selection
            Text(
                text = "Library Browsing Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = browsingMode == BrowsingMode.FILES, onClick = { browsingMode = BrowsingMode.FILES })
                    Text("Folders", modifier = Modifier.padding(start = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = browsingMode == BrowsingMode.TAGS, onClick = { browsingMode = BrowsingMode.TAGS })
                    Text("Tags", modifier = Modifier.padding(start = 4.dp))
                }
            }
            // Folder count enrichment toggle (only meaningful in Folders mode)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Compute album & song counts in folder mode")
                    Text(
                        text = if (folderCountEnrich) "May slow down loading on large libraries" else "Faster loading (counts disabled)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = folderCountEnrich, enabled = browsingMode == BrowsingMode.FILES, onCheckedChange = { folderCountEnrich = it })
            }

            // Save button with reload functionality
            Button(
                onClick = {
                    // Save all explicit settings
                    SettingsManager.saveServerUrl(context, serverUrl)
                    SettingsManager.saveUsername(context, username)
                    SettingsManager.savePassword(context, password)
                    SettingsManager.saveRadioUrl(context, radioUrl)
                    SettingsManager.saveBrowsingMode(context, browsingMode)
                    SettingsManager.saveFolderCountEnrichmentEnabled(context, folderCountEnrich)
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

        // Album Art Cache Section
        AlbumArtCacheSection(
            cacheSizeText = cacheSizeText,
            onRefresh = { refreshCacheSize() },
            onClear = {
                scope.launch {
                    CoverArtCacheManager.clearAsync(context)
                    refreshCacheSize()
                }
            }
        )

        // About Section (extracted)
        AboutSection()

        // Debug Key Codes Section (extracted)
        DebugKeyCodesSection()
    }
}
