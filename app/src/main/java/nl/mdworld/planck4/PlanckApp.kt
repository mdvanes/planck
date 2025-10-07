package nl.mdworld.planck4

import android.util.Log
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import nl.mdworld.planck4.networking.subsonic.SubsonicApi
import nl.mdworld.planck4.networking.subsonic.SubsonicPlaylistsResponse
import nl.mdworld.planck4.views.library.Album
import nl.mdworld.planck4.views.library.AlbumCardList
import nl.mdworld.planck4.views.library.Artist
import nl.mdworld.planck4.views.library.ArtistCardList
import nl.mdworld.planck4.views.playlists.Playlist
import nl.mdworld.planck4.views.playlists.PlaylistCardList
import nl.mdworld.planck4.views.radio.RadioScreen
import nl.mdworld.planck4.views.settings.SettingsScreen
import nl.mdworld.planck4.views.song.Song
import nl.mdworld.planck4.views.song.SongCardList

@Composable
fun PlanckApp(
    appState: PlanckAppState = rememberPlanckAppState()
) {
    val context = LocalContext.current
    val focusParkingRequester = remember { FocusRequester() }
    val mainViewModel: MainViewModel = viewModel()

    // Keep MainViewModel in sync with PlanckAppState
    LaunchedEffect(appState) {
        mainViewModel.setAppState(appState)
    }

    // Load playlists on app start
    LaunchedEffect(Unit) {
        try {
            val response: SubsonicPlaylistsResponse = SubsonicApi().getPlaylistsKtor(context)
            val playlistStrings: List<String> = response.sr.playlists.playlist.map {
                "${it.id} - ${it.name} (${it.coverArt})"
            }
            println(playlistStrings.joinToString(","))
            val newPlaylists: List<Playlist> =
                response.sr.playlists.playlist.map { Playlist(it.id, it.coverArt, it.name) }
            appState.playlists.clear()
            appState.playlists.addAll(newPlaylists)
        } catch (e: Exception) {
            println("PlanckApp: Failed to call API:$e")
        }
    }

    // Load songs when navigating to song view
    LaunchedEffect(appState.selectedPlaylistId, appState.currentScreen) {
        if (appState.selectedPlaylistId != null && appState.currentScreen == AppScreen.SONGS) {
            appState.songs.clear()
            try {
                val response = SubsonicApi().getPlaylistKtor(context, appState.selectedPlaylistId!!)
                val songs = response.sr.playlist.songs?.map { song ->
                    Song(
                        id = song.id,
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        duration = song.duration,
                        coverArt = song.coverArt
                    )
                } ?: emptyList()

                appState.songs.addAll(songs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Failed to load playlist songs: $e")
            }
        }
    }

    // Load artists when navigating to playlists or artists view
    LaunchedEffect(appState.currentScreen) {
        if (appState.currentScreen == AppScreen.PLAYLISTS) {
            appState.playlists.clear()
            try {
                val response: SubsonicPlaylistsResponse = SubsonicApi().getPlaylistsKtor(context)
                val playlistStrings: List<String> = response.sr.playlists.playlist.map {
                    "${it.id} - ${it.name} (${it.coverArt})"
                }
                println(playlistStrings.joinToString(","))
                val newPlaylists: List<Playlist> =
                    response.sr.playlists.playlist.map { Playlist(it.id, it.coverArt, it.name) }
                appState.playlists.clear()
                appState.playlists.addAll(newPlaylists)
            } catch (e: Exception) {
                println("PlanckApp: Failed to call API:$e")
            }
        }

        if (appState.currentScreen == AppScreen.ARTISTS) {
            appState.artists.clear()
            try {
                // File-structure browsing using getIndexes replaces getArtists
                val response = SubsonicApi().getIndexesKtor(context)
                val artists = response.sr.indexes.index.flatMap { idx ->
                    idx.artist.map { folderArtist ->
                        // albumCount unknown in file browsing without extra calls; set 0
                        Artist(
                            id = folderArtist.id,
                            name = folderArtist.name,
                            albumCount = 0,
                            coverArt = null
                        )
                    }
                }
                appState.artists.addAll(artists)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Failed to load artists (indexes): $e")
            }
        }
    }

    // Load albums when navigating to albums view (using file browsing: getMusicDirectory on artist folder)
    LaunchedEffect(appState.selectedArtistId, appState.currentScreen) {
        if (appState.selectedArtistId != null && appState.currentScreen == AppScreen.ALBUMS) {
            appState.albums.clear()
            try {
                val response = SubsonicApi().getMusicDirectoryKtor(context, appState.selectedArtistId!!)
                val albums: List<Album> = response.sr.directory.child
                    .filter { it.isDir }
                    .map { childDir ->
                        Album(
                            id = childDir.id,
                            name = childDir.title,
                            artist = appState.selectedArtistName ?: "",
                            artistId = appState.selectedArtistId!!,
                            songCount = 0, // Unknown without deeper traversal
                            duration = 0,
                            coverArt = childDir.coverArt,
                            year = null
                        )
                    }
                appState.albums.addAll(albums)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Failed to load albums (musicDirectory): $e")
            }
        }
    }

    // Load album songs when navigating to album songs view (using file browsing: getMusicDirectory on album folder)
    LaunchedEffect(appState.selectedAlbumId, appState.currentScreen) {
        if (appState.selectedAlbumId != null && appState.currentScreen == AppScreen.ALBUM_SONGS) {
            appState.songs.clear()
            try {
                val response = SubsonicApi().getMusicDirectoryKtor(context, appState.selectedAlbumId!!)
                val songs = response.sr.directory.child
                    .filter { !it.isDir }
                    .map { child ->
                        Song(
                            id = child.id,
                            title = child.title,
                            artist = child.artist,
                            album = child.album ?: appState.selectedAlbumName,
                            duration = child.duration,
                            coverArt = child.coverArt
                        )
                    }
                appState.songs.addAll(songs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Failed to load album songs (musicDirectory): $e")
            }
        }
    }

    // Add Focus Parking View for rotary controller support
    FocusParkingView(focusRequester = focusParkingRequester)

    RotaryControllerHandler(
        onRotaryConfirmClick = {
            // Handle rotary confirm button - you can add your next song logic here
            Log.d("PlanckApp", "🎵 ROTARY CONFIRM CLICKED - Ready to implement next song!")
            // Example: appState.playNextSong() - you can add this later
        }
    ) {
        Scaffold(
            bottomBar = {
                PlanckBottomAppBar(
                    currentScreen = appState.currentScreen,
                    onNavigateBack = { appState.navigateToPlaylists() },
                    activeSong = appState.activeSong,
                    onNavigateToSettings = { appState.navigateToSettings() },
                    appState = appState
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr),
                        bottom = 0.dp // Remove excessive bottom padding
                    ),
                color = MaterialTheme.colorScheme.background
            ) {
                when (appState.currentScreen) {
                    AppScreen.PLAYLISTS -> {
                        PlaylistCardList(
                            modifier = Modifier,
                            playlists = appState.playlists,
                            appState = appState
                        )
                    }

                    AppScreen.SONGS -> {
                        SongCardList(
                            songs = appState.songs.toList(),
                            playlistTitle = appState.selectedPlaylistName ?: "Playlist",
                            currentlyPlayingSong = appState.activeSong,
                            onSongClick = { song -> appState.playStream(song) }
                        )
                    }

                    AppScreen.ARTISTS -> {
                        ArtistCardList(appState.artists, appState)
                    }

                    AppScreen.ALBUMS -> {
                        AlbumCardList(appState.albums, appState)
                    }

                    AppScreen.ALBUM_SONGS -> {
                        SongCardList(
                            songs = appState.songs.toList(),
                            playlistTitle = appState.selectedAlbumName ?: "Album",
                            currentlyPlayingSong = appState.activeSong,
                            onSongClick = { song -> appState.playStream(song) }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            onNavigateBack = { appState.navigateToPlaylists() },
                            appState = appState
                        )
                    }

                    AppScreen.RADIO -> {
                        RadioScreen(
                            appState = appState
                        )
                    }
                }
            }
        }
    }
}
