package nl.mdworld.planck4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.mdworld.planck4.networking.SubsonicApi
import nl.mdworld.planck4.networking.SubsonicPlaylistsResponse
import nl.mdworld.planck4.networking.ktorHttpClient
import nl.mdworld.planck4.ui.theme.PlanckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

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
}
