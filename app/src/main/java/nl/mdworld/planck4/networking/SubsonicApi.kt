package nl.mdworld.planck4.networking

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.mdworld.planck4.R
import nl.mdworld.planck4.SettingsManager


class SubsonicApi {

    suspend fun getPlaylistsKtor(context: Context): SubsonicPlaylistsResponse {
        val playerName = R.string.subsonic_player_name
        val apiConfig =
            "?u=${SubsonicTemp.JUKEBOX_USERNAME}&t=${SubsonicTemp.JUKEBOX_API_TOKEN}&s=${SubsonicTemp.JUKEBOX_SALT}&v=1.16.0&c=${playerName}&f=json"
        return ktorHttpClient.get("${SettingsManager.getJukeboxBaseUrl(context)}getPlaylists${apiConfig}").body()
    }

    suspend fun getPlaylistKtor(
        context: Context,
        id: String
    ): SubsonicPlaylistDetailResponse {
        val playerName = R.string.subsonic_player_name
        val apiConfig =
            "?u=${SubsonicTemp.JUKEBOX_USERNAME}&t=${SubsonicTemp.JUKEBOX_API_TOKEN}&s=${SubsonicTemp.JUKEBOX_SALT}&v=1.16.0&c=${playerName}&f=json"

        return ktorHttpClient.get("${SettingsManager.getJukeboxBaseUrl(context)}getPlaylist${apiConfig}&id=${id}").body()
    }

}
