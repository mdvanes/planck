package nl.mdworld.planck4.networking.subsonic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data classes for file structure based browsing using getIndexes and getMusicDirectory

@Serializable
data class SubsonicIndexesResponse(
    @SerialName("subsonic-response") val sr: SubsonicIndexesResponse2
)

@Serializable
data class SubsonicIndexesResponse2(
    @SerialName("indexes") val indexes: SubsonicIndexesContainer
)

@Serializable
data class SubsonicIndexesContainer(
    @SerialName("index") val index: List<SubsonicFileArtistIndex> = emptyList(),
    @SerialName("lastModified") val lastModified: Long? = null,
    @SerialName("ignoredArticles") val ignoredArticles: String? = null
)

@Serializable
data class SubsonicFileArtistIndex(
    @SerialName("name") val name: String,
    @SerialName("artist") val artist: List<SubsonicFileArtist> = emptyList()
)

@Serializable
data class SubsonicFileArtist(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String
)

@Serializable
data class SubsonicMusicDirectoryResponse(
    @SerialName("subsonic-response") val sr: SubsonicMusicDirectoryResponse2
)

@Serializable
data class SubsonicMusicDirectoryResponse2(
    @SerialName("directory") val directory: SubsonicDirectory
)

@Serializable
data class SubsonicDirectory(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("child") val child: List<SubsonicChild> = emptyList()
)

@Serializable
data class SubsonicChild(
    @SerialName("id") val id: String,
    @SerialName("parent") val parent: String? = null,
    @SerialName("isDir") val isDir: Boolean = false,
    @SerialName("title") val title: String,
    @SerialName("album") val album: String? = null,
    @SerialName("artist") val artist: String? = null,
    @SerialName("coverArt") val coverArt: String? = null,
    @SerialName("duration") val duration: Int? = null,
    @SerialName("track") val track: Int? = null
)

