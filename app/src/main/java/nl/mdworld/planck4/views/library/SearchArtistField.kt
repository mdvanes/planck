package nl.mdworld.planck4.views.library

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun SearchArtistField(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Focus requester to grab focus when the field becomes visible
    val focusRequester = remember { FocusRequester() }
    // Ensure we only auto-focus once (avoid stealing focus on recompositions)
    var hasAutoFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasAutoFocused) {
            focusRequester.requestFocus()
            keyboardController?.show()
            hasAutoFocused = true
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onSearch(newText)
        },
        placeholder = {
            Text("Search artists...")
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    text = ""
                    onSearch("")
                    // Keep focus after clearing so user can immediately type again
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        )
    )
}
