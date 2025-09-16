package nl.mdworld.planck4

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp

/**
 * Focus parking view for Android Auto rotary controller navigation
 * This invisible view provides a place for focus to "park" when no other elements should be focused
 */
@Composable
fun FocusParkingView(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester()
) {
    Box(
        modifier = modifier
            .size(0.dp) // Make it invisible
            .focusRequester(focusRequester)
            .focusable()
    )
}
