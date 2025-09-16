package nl.mdworld.planck4

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Rotary controller handler for Android Auto
 * This component captures rotary controller events and logs them
 */
@Composable
fun RotaryControllerHandler(
    modifier: Modifier = Modifier,
    onRotaryConfirmClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    // Auto-focus this component when it appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                Log.d("RotaryController", "🎵 KEY EVENT: ${keyEvent.key} - ${keyEvent.type}")

                when {
                    keyEvent.key == Key.DirectionCenter && keyEvent.type == KeyEventType.KeyDown -> {
                        Log.d("RotaryController", "🎵 CAR ROTARY CONFIRM BUTTON CLICKED!")
                        onRotaryConfirmClick()
                        true
                    }
                    keyEvent.key == Key.DirectionUp && keyEvent.type == KeyEventType.KeyDown -> {
                        Log.d("RotaryController", "🎵 CAR ROTARY UP!")
                        true
                    }
                    keyEvent.key == Key.DirectionDown && keyEvent.type == KeyEventType.KeyDown -> {
                        Log.d("RotaryController", "🎵 CAR ROTARY DOWN!")
                        true
                    }
                    keyEvent.key == Key.DirectionLeft && keyEvent.type == KeyEventType.KeyDown -> {
                        Log.d("RotaryController", "🎵 CAR ROTARY LEFT!")
                        true
                    }
                    keyEvent.key == Key.DirectionRight && keyEvent.type == KeyEventType.KeyDown -> {
                        Log.d("RotaryController", "🎵 CAR ROTARY RIGHT!")
                        true
                    }
                    else -> false
                }
            }
    ) {
        content()
    }
}
