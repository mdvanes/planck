package nl.mdworld.planck4.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicArtistsResponse(
    @SerialName("subsonic-response") val sr: SubsonicArtistsResponse2
)

@Serializable
data class SubsonicArtistsResponse2(
    @SerialName("artists") val artists: SubsonicArtistsResponseContainer
)

@Serializable
data class SubsonicArtistsResponseContainer(
    @SerialName("index") val index: List<SubsonicArtistIndex>
)

@Serializable
data class SubsonicArtistIndex(
    @SerialName("name") val name: String,
    @SerialName("artist") val artist: List<SubsonicArtistsEntity>
)

@Serializable
data class SubsonicArtistsEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("albumCount") val albumCount: Int = 0,
    @SerialName("coverArt") val coverArt: String? = null
)

@Serializable
data class SubsonicAlbumsResponse(
    @SerialName("subsonic-response") val sr: SubsonicAlbumsResponse2
)

@Serializable
data class SubsonicAlbumsResponse2(
    @SerialName("artist") val artist: SubsonicArtistDetail
)

@Serializable
data class SubsonicArtistDetail(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("albumCount") val albumCount: Int = 0,
    @SerialName("album") val album: List<SubsonicAlbumsEntity>
)

@Serializable
data class SubsonicAlbumsEntity(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("artist") val artist: String,
    @SerialName("artistId") val artistId: String,
    @SerialName("songCount") val songCount: Int = 0,
    @SerialName("duration") val duration: Int = 0,
    @SerialName("coverArt") val coverArt: String? = null,
    @SerialName("year") val year: Int? = null
)
