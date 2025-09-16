package nl.mdworld.planck4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import nl.mdworld.planck4.ui.theme.PlanckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Start the MediaPlaybackService to enable car hardware button support
        val mediaServiceIntent = Intent(this, MediaPlaybackService::class.java)
        startService(mediaServiceIntent)

        //val navController = rememberNavController()

        //val playlists = mutableListOf<Playlist>(
        //    Playlist(
        //        "Empty",
        //        "No Playlist"
        //    )
        //)
        //
        //// TODO what is the proper way to call an API on app start? Maybe this: https://github.com/KatieBarnett/Experiments/blob/main/jc-refresh/src/main/java/dev/katiebarnett/experiments/jcrefresh/MainViewModel.kt
        //// NOTE: this is hacky
        //GlobalScope.launch {
        //    try {
        //        val response: SubsonicPlaylistsResponse = SubsonicApi().getPlaylistsKtor()
        //        val playlistStrings: List<String> = response.sr.playlists.playlist.map {
        //            "${it.id} - ${it.name} (${it.coverArt})"
        //        }
        //        println(playlistStrings.joinToString(","))
        //        val newPlaylists: List<Playlist> =
        //            response.sr.playlists.playlist.map { Playlist(it.coverArt, it.name) }
        //        playlists.clear()
        //        playlists.addAll(newPlaylists)
        //    } catch (e: Exception) {
        //        println("Failed to call API:$e")
        //    } finally {
        //        ktorHttpClient.close()
        //    }
        //}

        setContent {
            PlanckTheme {
                PlanckApp()
                //Scaffold(
                //    bottomBar = {
                //        PlanckBottomAppBar()
                //    }
                //) { innerPadding ->
                //    // TODO how to remove the unused innerPadding param?
                //    println(innerPadding)
                //    // A surface container using the 'background' color from the theme
                //    Surface(
                //        modifier = Modifier.fillMaxSize(),
                //        color = MaterialTheme.colorScheme.background
                //    ) {
                //        PlaylistCardList(playlists)
                //    }
                //}
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "🎵 KEY DOWN - KeyCode: $keyCode, Event: $event")

        // Track key codes for debugging in Settings
        val keyName = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> "DPAD_CENTER (Rotary Confirm)"
            KeyEvent.KEYCODE_MEDIA_NEXT -> "MEDIA_NEXT"
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "MEDIA_PREVIOUS"
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "MEDIA_PLAY_PAUSE"
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            else -> KeyEvent.keyCodeToString(keyCode)
        }
        KeyCodeTracker.addKeyCode(keyCode.toString(), keyName.toString())

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                Log.d("MainActivity", "🎵 CAR ROTARY CONFIRM BUTTON PRESSED!")
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                Log.d("MainActivity", "🎵 CAR NEXT BUTTON PRESSED!")
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                Log.d("MainActivity", "🎵 CAR PREVIOUS BUTTON PRESSED!")
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                Log.d("MainActivity", "🎵 CAR PLAY/PAUSE BUTTON PRESSED!")
                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "🎵 KEY UP - KeyCode: $keyCode, Event: $event")
        KeyCodeTracker.addKeyCode(keyCode.toString(), event?.keyCode.toString())
        return super.onKeyUp(keyCode, event)
    }
}
