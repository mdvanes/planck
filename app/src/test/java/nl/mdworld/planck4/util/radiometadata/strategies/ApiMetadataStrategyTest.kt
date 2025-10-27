package nl.mdworld.planck4.util.radiometadata.strategies

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import nl.mdworld.planck4.util.radiometadata.RadioMetadata
import nl.mdworld.planck4.util.radiometadata.SongInfo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ApiMetadataStrategy].
 * Focuses on interaction with [ApiMetadataUtil] for NPO2 and Sky presets.
 * (Deliberately excludes ICY related tests as requested.)
 */
class ApiMetadataStrategyTest {

    @Before
    fun setUp() {
        mockkObject(ApiMetadataUtil)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `fetchMetadata returns tracks for npo2 preset`() = runBlocking {
        val expected = listOf(
            RadioMetadata(song = SongInfo(artist = "Artist A", title = "Title A")),
            RadioMetadata(song = SongInfo(artist = "Artist B", title = "Title B"))
        )
        coEvery { ApiMetadataUtil.getRadioMetaData("npo2") } returns expected

        val strategy = ApiMetadataStrategy("npo2")
        val result = strategy.fetchMetadata("unused-stream-url")

        assertEquals("Should return the same list from ApiMetadataUtil", expected, result)
        coVerify(exactly = 1) { ApiMetadataUtil.getRadioMetaData("npo2") }
    }

    @Test
    fun `fetchMetadata returns tracks for sky preset`() = runBlocking {
        val expected = listOf(
            RadioMetadata(song = SongInfo(artist = "Sky Artist", title = "Sky Title"))
        )
        coEvery { ApiMetadataUtil.getRadioMetaData("sky") } returns expected

        val strategy = ApiMetadataStrategy("sky")
        val result = strategy.fetchMetadata("unused")

        assertEquals(expected, result)
        coVerify(exactly = 1) { ApiMetadataUtil.getRadioMetaData("sky") }
    }

    @Test
    fun `fetchMetadata returns empty list when ApiMetadataUtil throws`() = runBlocking {
        coEvery { ApiMetadataUtil.getRadioMetaData("npo2") } throws RuntimeException("boom")

        val strategy = ApiMetadataStrategy("npo2")
        val result = strategy.fetchMetadata("unused")

        assertTrue("Result should be empty when exception occurs", result.isEmpty())
        coVerify(exactly = 1) { ApiMetadataUtil.getRadioMetaData("npo2") }
    }
}

