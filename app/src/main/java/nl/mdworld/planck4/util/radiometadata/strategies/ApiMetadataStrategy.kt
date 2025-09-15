package nl.mdworld.planck4.util.radiometadata.strategies

import nl.mdworld.planck4.util.radiometadata.RadioMetadata

/**
 * Strategy for fetching metadata using the radio-metadata API (NPO2, Sky, etc.)
 */
class ApiMetadataStrategy(private val presetName: String) : MetadataStrategy {
    override suspend fun fetchMetadata(streamUrl: String): RadioMetadata? {
        return try {
            println("ApiMetadataStrategy: Fetching metadata for preset $presetName")
            val tracks = ApiMetadataUtil.getRadioMetaData(presetName)
            val firstTrack = tracks.firstOrNull()
            println("ApiMetadataStrategy: Fetched ${tracks.size} tracks $firstTrack")
            firstTrack
        } catch (e: Exception) {
            println("ApiMetadataStrategy: Error fetching metadata for $presetName: ${e.message}")
            null
        }
    }
}