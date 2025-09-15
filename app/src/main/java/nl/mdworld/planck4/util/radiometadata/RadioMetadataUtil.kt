package nl.mdworld.planck4.util.radiometadata

/**
 * Data class for parsed radio metadata - matches the TypeScript RadioMetadata interface
 */
data class RadioMetadata(
    val time: TimeInfo? = null,
    val broadcast: BroadcastInfo? = null,
    val song: SongInfo
)

data class TimeInfo(
    val start: String? = null,
    val end: String? = null
)

data class BroadcastInfo(
    val title: String? = null,
    val presenters: String? = null,
    val imageUrl: String? = null
)

data class SongInfo(
    val artist: String? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    val listenUrl: String? = null
)

/**
 * Data class for URL configuration in radio schemas
 */
data class UrlConfig(
    val name: String,
    val url: String,
    val headers: Map<String, String>? = null
)

/**
 * Type alias for pick paths (array of strings and integers)
 */
typealias PickPath = List<Any>

/**
 * Data class for radio station schema paths
 */
data class SchemaPaths(
    val tracks: PickPath,
    val time: TimeInfo? = null,
    val broadcast: BroadcastInfo? = null,
    val song: SongInfo
) {
    data class TimeInfo(
        val start: PickPath? = null,
        val end: PickPath? = null
    )

    data class BroadcastInfo(
        val title: PickPath? = null,
        val presenters: PickPath? = null,
        val imageUrl: PickPath? = null
    )

    data class SongInfo(
        val artist: PickPath? = null,
        val title: PickPath,
        val imageUrl: PickPath? = null,
        val listenUrl: PickPath? = null
    )
}

/**
 * Data class for radio station schema - matches the TypeScript RadioSchema interface
 */
data class RadioSchema(
    val name: String,
    val urls: List<UrlConfig>,
    val paths: SchemaPaths
)


