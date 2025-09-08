package nl.mdworld.planck.networking

import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.mdworld.planck.R


class SubsonicApi {

    suspend fun getPlaylistsKtor(): SubsonicPlaylistsResponse {
        val playerName = R.string.subsonic_player_name
        val apiConfig =
            "?u=${SubsonicTemp.JUKEBOX_USERNAME}&t=${SubsonicTemp.JUKEBOX_API_TOKEN}&s=${SubsonicTemp.JUKEBOX_SALT}&v=1.16.0&c=${playerName}&f=json"
        return ktorHttpClient.get("${SubsonicTemp.JUKEBOX_BASE_URL}getPlaylists${apiConfig}").body()
    }

    suspend fun getPlaylistKtor(
        id: String
    ): SubsonicPlaylistDetailResponse {
        val playerName = R.string.subsonic_player_name
        val apiConfig =
            "?u=${SubsonicTemp.JUKEBOX_USERNAME}&t=${SubsonicTemp.JUKEBOX_API_TOKEN}&s=${SubsonicTemp.JUKEBOX_SALT}&v=1.16.0&c=${playerName}&f=json"
        return ktorHttpClient.get("${SubsonicTemp.JUKEBOX_BASE_URL}getPlaylist${apiConfig}&id=${id}").body()
    }

}
