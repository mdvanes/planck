package nl.mdworld.planck4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import nl.mdworld.planck4.ui.theme.PlanckTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

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
