package nl.mdworld.planck4.networking.subsonic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicPlaylistsResponse(
    @SerialName("subsonic-response") val sr: SubsonicPlaylistsResponse2
)

@Serializable
data class SubsonicPlaylistsResponse2(
    @SerialName("playlists") val playlists: SubsonicPlaylistsResponsePlaylist
)

@Serializable
data class SubsonicPlaylistsResponsePlaylist(
    @SerialName("playlist") val playlist: ArrayList<SubsonicPlaylistsEntity>
)

@Serializable
data class SubsonicPlaylistsEntity(
    @SerialName("id")
    val id: String,
    @SerialName("coverArt")
    val coverArt: String,
    @SerialName("name")
    val name: String
)