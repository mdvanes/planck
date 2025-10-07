package nl.mdworld.planck4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import nl.mdworld.planck4.ui.theme.PlanckTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    // Launcher for notification permission (API 33+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Regardless of grant result, attempt to start service (if denied, notification won't show)
        startMediaService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Request notification permission first (Android 13+) then start foreground media service
        maybeRequestNotificationPermissionAndStartService()

        setContent {
            PlanckTheme {
                PlanckApp()
            }
        }
    }

    private fun maybeRequestNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                startMediaService()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startMediaService()
        }
    }

    private fun startMediaService() {
        val mediaServiceIntent = Intent(this, MediaPlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use startForegroundService for API 26+ to ensure timely foreground promotion
            startForegroundService(mediaServiceIntent)
        } else {
            startService(mediaServiceIntent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "🎵 KEY DOWN - KeyCode: $keyCode, Event: $event")
        val appState = mainViewModel.appState.value

        if(keyCode == KeyEvent.KEYCODE_S || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            handlePlayPause(this, appState)
        }

        if(keyCode == KeyEvent.KEYCODE_A || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            println("prev")
            appState?.playPreviousSong()
        }

        if(keyCode == KeyEvent.KEYCODE_D || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            println("next")
            appState?.playNextSong()
        }

        KeyCodeTracker.addKeyCode(keyCode.toString(), KeyEvent.keyCodeToString(keyCode))

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        // Ensure all audio is stopped when activity finishes
        AppAudioManager.cleanupAllState()
        super.onDestroy()
    }
}
