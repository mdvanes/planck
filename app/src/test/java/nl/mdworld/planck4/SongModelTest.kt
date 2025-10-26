package nl.mdworld.planck4

import nl.mdworld.planck4.views.song.Song
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Song data class and playback-related business logic
 *
 * Tests cover:
 * - Song model validation
 * - Song data integrity
 * - Special song types (radio streams)
 * - Song comparison and equality
 */
class SongModelTest {

    @Test
    fun `song creation with all fields`() {
        val song = Song(
            id = "song123",
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            duration = 180,
            coverArt = "cover123"
        )

        assertEquals("song123", song.id)
        assertEquals("Test Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals("Test Album", song.album)
        assertEquals(180, song.duration)
        assertEquals("cover123", song.coverArt)
    }

    @Test
    fun `song creation with null optional fields`() {
        val song = Song(
            id = "song456",
            title = "Minimal Song",
            artist = null,
            album = null,
            duration = null,
            coverArt = null
        )

        assertEquals("song456", song.id)
        assertEquals("Minimal Song", song.title)
        assertNull(song.artist)
        assertNull(song.album)
        assertNull(song.duration)
        assertNull(song.coverArt)
    }

    @Test
    fun `radio stream song has special id`() {
        val radioSong = Song(
            id = "radio-stream",
            title = "NPO Radio 2",
            artist = "Live Radio",
            album = "Live Stream",
            duration = 0,
            coverArt = null
        )

        assertEquals("radio-stream", radioSong.id)
        assertEquals(0, radioSong.duration)
    }

    @Test
    fun `song equality based on data class`() {
        val song1 = Song("1", "Title", "Artist", "Album", 100, "cover")
        val song2 = Song("1", "Title", "Artist", "Album", 100, "cover")
        val song3 = Song("2", "Title", "Artist", "Album", 100, "cover")

        assertEquals(song1, song2)
        assertNotEquals(song1, song3)
    }

    @Test
    fun `song with empty strings is valid`() {
        val song = Song(
            id = "",
            title = "",
            artist = "",
            album = "",
            duration = 0,
            coverArt = ""
        )

        assertNotNull(song)
        assertEquals("", song.id)
        assertEquals("", song.title)
    }

    @Test
    fun `song duration can be zero or positive`() {
        val song1 = Song("1", "Short", null, null, 0, null)
        val song2 = Song("2", "Long", null, null, 3600, null)

        assertEquals(0, song1.duration)
        assertEquals(3600, song2.duration)
    }

    @Test
    fun `song can have artist without album`() {
        val song = Song(
            id = "single",
            title = "Single Track",
            artist = "Solo Artist",
            album = null,
            duration = 200,
            coverArt = null
        )

        assertNotNull(song.artist)
        assertNull(song.album)
    }

    @Test
    fun `song can have album without artist`() {
        val song = Song(
            id = "compilation",
            title = "Various Track",
            artist = null,
            album = "Compilation Album",
            duration = 180,
            coverArt = null
        )

        assertNull(song.artist)
        assertNotNull(song.album)
    }

    @Test
    fun `song toString contains all fields`() {
        val song = Song("id1", "Title1", "Artist1", "Album1", 123, "cover1")
        val string = song.toString()

        assertTrue(string.contains("id1"))
        assertTrue(string.contains("Title1"))
        assertTrue(string.contains("Artist1"))
        assertTrue(string.contains("Album1"))
    }

    @Test
    fun `multiple songs can exist in a list`() {
        val songs = listOf(
            Song("1", "Song 1", "Artist A", "Album A", 180, null),
            Song("2", "Song 2", "Artist B", "Album B", 200, null),
            Song("3", "Song 3", "Artist C", "Album C", 220, null)
        )

        assertEquals(3, songs.size)
        assertEquals("Song 1", songs[0].title)
        assertEquals("Song 2", songs[1].title)
        assertEquals("Song 3", songs[2].title)
    }

    @Test
    fun `songs can be filtered by properties`() {
        val songs = listOf(
            Song("1", "Rock Song", "Rock Band", "Rock Album", 180, null),
            Song("2", "Jazz Song", "Jazz Band", "Jazz Album", 200, null),
            Song("3", "Rock Anthem", "Rock Band", "Rock Album 2", 220, null)
        )

        val rockSongs = songs.filter { it.artist == "Rock Band" }

        assertEquals(2, rockSongs.size)
        assertTrue(rockSongs.all { it.artist == "Rock Band" })
    }

    @Test
    fun `song copy creates new instance`() {
        val original = Song("1", "Original", "Artist", "Album", 100, "cover")
        val copy = original.copy(title = "Modified")

        assertEquals("Original", original.title)
        assertEquals("Modified", copy.title)
        assertEquals(original.id, copy.id)
        assertEquals(original.artist, copy.artist)
    }
}

