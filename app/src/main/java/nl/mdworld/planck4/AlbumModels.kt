package nl.mdworld.planck4

data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int = 0,
    val coverArt: String? = null
)

data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val artistId: String,
    val songCount: Int = 0,
    val duration: Int = 0,
    val coverArt: String? = null,
    val year: Int? = null
)
