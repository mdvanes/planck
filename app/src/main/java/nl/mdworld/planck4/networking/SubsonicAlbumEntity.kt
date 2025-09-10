package nl.mdworld.planck4.networking

data class SubsonicArtistsResponse(
    val sr: SubsonicArtistsResponse2
)

data class SubsonicArtistsResponse2(
    val artists: SubsonicArtistsResponseContainer
)

data class SubsonicArtistsResponseContainer(
    val index: List<SubsonicArtistIndex>
)

data class SubsonicArtistIndex(
    val name: String,
    val artist: List<SubsonicArtistsEntity>
)

data class SubsonicArtistsEntity(
    val id: String,
    val name: String,
    val albumCount: Int = 0,
    val coverArt: String? = null
)

data class SubsonicAlbumsResponse(
    val sr: SubsonicAlbumsResponse2
)

data class SubsonicAlbumsResponse2(
    val artist: SubsonicArtistDetail
)

data class SubsonicArtistDetail(
    val id: String,
    val name: String,
    val albumCount: Int = 0,
    val album: List<SubsonicAlbumsEntity>
)

data class SubsonicAlbumsEntity(
    val id: String,
    val name: String,
    val artist: String,
    val artistId: String,
    val songCount: Int = 0,
    val duration: Int = 0,
    val coverArt: String? = null,
    val year: Int? = null
)
