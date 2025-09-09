package nl.mdworld.planck4.networking

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.mdworld.planck4.R
import nl.mdworld.planck4.SettingsManager


class SubsonicApi {

    suspend fun getPlaylistsKtor(context: Context): SubsonicPlaylistsResponse {
        // Hidden feature: if API token is default, return dummy playlists
        val apiToken = SettingsManager.getApiToken(context)
        if (apiToken == SettingsManager.DEFAULT_API_TOKEN) {
            return createDummyPlaylistsResponse()
        }

        val playerName = R.string.subsonic_player_name
        val apiConfig =
            "?u=${SettingsManager.getUsername(context)}&t=${apiToken}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"
        return ktorHttpClient.get("${SettingsManager.getJukeboxBaseUrl(context)}getPlaylists${apiConfig}").body()
    }

    suspend fun getPlaylistKtor(
        context: Context,
        id: String
    ): SubsonicPlaylistDetailResponse {
        val playerName = R.string.subsonic_player_name
        val apiConfig =
            "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"

        return ktorHttpClient.get("${SettingsManager.getJukeboxBaseUrl(context)}getPlaylist${apiConfig}&id=${id}").body()
    }

    private fun createDummyPlaylistsResponse(): SubsonicPlaylistsResponse {
        val dummyPlaylists = arrayListOf(
            SubsonicPlaylistsEntity(
                id = "fake-1",
                coverArt = "dummy-cover-1",
                name = "🎵 Fake Hits of the 80s"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-2",
                coverArt = "dummy-cover-2",
                name = "🎭 Imaginary Broadway Classics"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-3",
                coverArt = "dummy-cover-3",
                name = "🤖 Robot Dance Party Mix"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-4",
                coverArt = "dummy-cover-4",
                name = "🌙 Lunar Lullabies Collection"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-5",
                coverArt = "dummy-cover-5",
                name = "🦄 Unicorn Pop Anthems"
            )
        )

        return SubsonicPlaylistsResponse(
            sr = SubsonicPlaylistsResponse2(
                playlists = SubsonicPlaylistsResponsePlaylist(
                    playlist = dummyPlaylists
                )
            )
        )
    }

}

