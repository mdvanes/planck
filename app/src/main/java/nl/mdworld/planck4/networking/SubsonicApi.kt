package nl.mdworld.planck4.networking

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.utils.SubsonicUrlBuilder

class SubsonicApi {

    private fun areCredentialsValid(context: Context): Boolean {
        val apiToken = SettingsManager.getApiToken(context)
        val salt = SettingsManager.getSalt(context)
        val username = SettingsManager.getUsername(context)
        return !(apiToken == SettingsManager.DEFAULT_API_TOKEN || salt == SettingsManager.DEFAULT_SALT || salt.isEmpty() || username.isEmpty())
    }

    suspend fun getPlaylistsKtor(context: Context): SubsonicPlaylistsResponse {
        if (!areCredentialsValid(context)) {
            return createDummyPlaylistsResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildPlaylistsUrl(context))
            .body()
    }

    suspend fun getPlaylistKtor(
        context: Context,
        id: String
    ): SubsonicPlaylistDetailResponse {
        if (!areCredentialsValid(context)) {
            return createDummyPlaylistResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildPlaylistUrl(context, id))
            .body()
    }

    suspend fun getArtistsKtor(context: Context): SubsonicArtistsResponse {
        if (!areCredentialsValid(context)) {
            return createDummyArtistsResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildArtistsUrl(context))
            .body()
    }

    suspend fun getArtistKtor(context: Context, id: String): SubsonicAlbumsResponse {
        if (!areCredentialsValid(context)) {
            return createDummyAlbumsResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildArtistUrl(context, id))
            .body()
    }

    suspend fun getAlbumKtor(context: Context, id: String): SubsonicPlaylistDetailResponse {
        if (!areCredentialsValid(context)) {
            return createDummyPlaylistResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildAlbumUrl(context, id))
            .body()
    }

    private fun createDummyPlaylistsResponse(): SubsonicPlaylistsResponse {
        val dummyPlaylists = arrayListOf(
            SubsonicPlaylistsEntity(
                id = "fake-1",
                coverArt = "dummy-cover-1",
                name = "🎵 Fake Hits of the 80s"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-2",
                coverArt = "dummy-cover-2",
                name = "🎭 Imaginary Broadway Classics"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-3",
                coverArt = "dummy-cover-3",
                name = "🤖 Robot Dance Party Mix"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-4",
                coverArt = "dummy-cover-4",
                name = "🌙 Lunar Lullabies Collection"
            ),
            SubsonicPlaylistsEntity(
                id = "fake-5",
                coverArt = "dummy-cover-5",
                name = "🦄 Unicorn Pop Anthems"
            )
        )

        return SubsonicPlaylistsResponse(
            sr = SubsonicPlaylistsResponse2(
                playlists = SubsonicPlaylistsResponsePlaylist(
                    playlist = dummyPlaylists
                )
            )
        )
    }

    private fun createDummyPlaylistResponse(): SubsonicPlaylistDetailResponse {
        val dummySongs = arrayListOf(
            SubsonicSong(
                id = "fake-1",
                coverArt = "dummy-cover-1",
                title = "Dummy Song One",
                artist = "John Doe",
                duration = 245,
                track = 2
            ),
            SubsonicSong(
                id = "fake-2",
                coverArt = "dummy-cover-2",
                title = "Imaginary Track Two",
                artist = "Jane Smith",
                duration = 425,
                track = 1
            )
        )

        return SubsonicPlaylistDetailResponse(
            sr = SubsonicPlaylistDetailResponse2(
                playlist = SubsonicPlaylistDetail(
                    id = "fake-1",
                    name = "🎵 Fake Hits of the 80s",
                    coverArt = "dummy-cover-1",
                    songCount = dummySongs.size,
                    duration = 3600,
                    songs = dummySongs
                )
            )
        )
    }

    private fun createDummyArtistsResponse(): SubsonicArtistsResponse {
        val dummyArtists = arrayListOf(
            SubsonicArtistsEntity(
                id = "fake-artist-1",
                name = "🎸 The Fake Beatles",
                albumCount = 13,
                coverArt = "artist-cover-1"
            ),
            SubsonicArtistsEntity(
                id = "fake-artist-2",
                name = "🎤 Imaginary Adele",
                albumCount = 4,
                coverArt = "artist-cover-2"
            ),
            SubsonicArtistsEntity(
                id = "fake-artist-3",
                name = "🎹 Pretend Pink Floyd",
                albumCount = 15,
                coverArt = "artist-cover-3"
            ),
            SubsonicArtistsEntity(
                id = "fake-artist-4",
                name = "🥁 Mock Metallica",
                albumCount = 10,
                coverArt = "artist-cover-4"
            )
        )

        return SubsonicArtistsResponse(
            sr = SubsonicArtistsResponse2(
                artists = SubsonicArtistsResponseContainer(
                    index = listOf(
                        SubsonicArtistIndex(
                            name = "F",
                            artist = dummyArtists.filter { it.name.contains("Fake") }
                        ),
                        SubsonicArtistIndex(
                            name = "I-P",
                            artist = dummyArtists.filter { it.name.contains("Imaginary") || it.name.contains("Pretend") || it.name.contains("Mock") }
                        )
                    )
                )
            )
        )
    }

    private fun createDummyAlbumsResponse(): SubsonicAlbumsResponse {
        val dummyAlbums = arrayListOf(
            SubsonicAlbumsEntity(
                id = "fake-album-1",
                name = "🎵 Greatest Fake Hits",
                artist = "The Fake Beatles",
                artistId = "fake-artist-1",
                songCount = 12,
                duration = 2880,
                coverArt = "album-cover-1",
                year = 1969
            ),
            SubsonicAlbumsEntity(
                id = "fake-album-2",
                name = "🌈 Imaginary Rainbow",
                artist = "The Fake Beatles",
                artistId = "fake-artist-1",
                songCount = 14,
                duration = 3360,
                coverArt = "album-cover-2",
                year = 1973
            ),
            SubsonicAlbumsEntity(
                id = "fake-album-3",
                name = "🚀 Space Oddity (Fake Version)",
                artist = "The Fake Beatles",
                artistId = "fake-artist-1",
                songCount = 11,
                duration = 2640,
                coverArt = "album-cover-3",
                year = 1967
            )
        )

        return SubsonicAlbumsResponse(
            sr = SubsonicAlbumsResponse2(
                artist = SubsonicArtistDetail(
                    id = "fake-artist-1",
                    name = "The Fake Beatles",
                    albumCount = dummyAlbums.size,
                    album = dummyAlbums
                )
            )
        )
    }
}
