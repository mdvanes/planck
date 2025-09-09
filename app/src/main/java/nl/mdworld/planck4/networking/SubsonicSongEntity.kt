package nl.mdworld.planck4.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicPlaylistDetailResponse(
    @SerialName("subsonic-response") val sr: SubsonicPlaylistDetailResponse2
)

@Serializable
data class SubsonicPlaylistDetailResponse2(
    @SerialName("playlist") val playlist: SubsonicPlaylistDetail
)

@Serializable
data class SubsonicPlaylistDetail(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("coverArt") val coverArt: String?,
    @SerialName("songCount") val songCount: Int,
    @SerialName("duration") val duration: Int,
    @SerialName("entry") val songs: List<SubsonicSong>? = null
)

@Serializable
data class SubsonicSong(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("artist") val artist: String? = null,
    @SerialName("album") val album: String? = null,
    @SerialName("duration") val duration: Int? = null,
    @SerialName("track") val track: Int? = null,
    @SerialName("coverArt") val coverArt: String? = null
)
