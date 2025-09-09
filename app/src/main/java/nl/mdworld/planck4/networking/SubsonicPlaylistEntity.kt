package nl.mdworld.planck4.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicPlaylistResponse(
    @SerialName("subsonic-response") val sr: SubsonicPlaylistResponse2
)

@Serializable
data class SubsonicPlaylistResponse2(
    @SerialName("playlists") val playlists: SubsonicPlaylistResponsePlaylist
)

@Serializable
data class SubsonicPlaylistResponsePlaylist(
    @SerialName("playlist") val playlist: ArrayList<SubsonicPlaylistEntity>
)

@Serializable
data class SubsonicPlaylistEntity(
    @SerialName("id")
    val id: String,
    @SerialName("coverArt")
    val coverArt: String,
    @SerialName("name")
    val name: String
)