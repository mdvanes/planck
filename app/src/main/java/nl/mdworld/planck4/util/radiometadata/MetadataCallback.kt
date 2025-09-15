package nl.mdworld.planck4.util.radiometadata

/**
 * Callback interface for radio metadata updates.
 */
interface MetadataCallback {
    /**
     * Called when new radio metadata is available.
     *
     * @param metadata The updated radio metadata
     */
    fun onMetadataUpdate(metadata: RadioMetadata)

    /**
     * Called when metadata fetch fails.
     *
     * @param error The error that occurred
     */
    fun onMetadataError(error: String)
}
