package nl.mdworld.planck4.networking.subsonic

object SubsonicDummyResponses {

    fun createDummyPlaylistsResponse(): SubsonicPlaylistsResponse {
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

    fun createDummyPlaylistResponse(): SubsonicPlaylistDetailResponse {
        val dummySongs = arrayListOf(
            SubsonicSong(
                id = "fake-1",
                coverArt = "dummy-cover-1",
                title = "Dummy Song One Very Long Title For Testing Long Titles And Even Longer And So Long It Should Never Push The Buttons Offscreen And Even Longer And So Long It Should Never Push The Buttons Offscreen",
                artist = "John Doe The Second With A Very Long Name As Well That Should Be Tested And Even Longer And So Long It Should Never Push The Buttons Offscreen",
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

    fun createDummyArtistsResponse(): SubsonicArtistsResponse {
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

    fun createDummyAlbumsResponse(): SubsonicAlbumsResponse {
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

    fun createDummyAlbumResponse(): SubsonicAlbumDetailResponse {
        val dummySongs = arrayListOf(
            SubsonicSong(
                id = "fake-song-1",
                title = "Fake Song One",
                artist = "The Fake Beatles",
                album = "Greatest Fake Hits",
                duration = 240,
                track = 1,
                coverArt = "album-cover-1"
            ),
            SubsonicSong(
                id = "fake-song-2",
                title = "Imaginary Track Two",
                artist = "The Fake Beatles",
                album = "Greatest Fake Hits",
                duration = 300,
                track = 2,
                coverArt = "album-cover-1"
            ),
            SubsonicSong(
                id = "fake-song-3",
                title = "Pretend Melody Three",
                artist = "The Fake Beatles",
                album = "Greatest Fake Hits",
                duration = 180,
                track = 3,
                coverArt = "album-cover-1"
            )
        )

        return SubsonicAlbumDetailResponse(
            sr = SubsonicAlbumDetailResponse2(
                album = SubsonicAlbumDetail(
                    id = "fake-album-1",
                    name = "🎵 Greatest Fake Hits",
                    artist = "The Fake Beatles",
                    artistId = "fake-artist-1",
                    coverArt = "album-cover-1",
                    songCount = dummySongs.size,
                    duration = 3600,
                    year = 1969,
                    songs = dummySongs
                )
            )
        )
    }

    fun createDummyIndexesResponse(): SubsonicIndexesResponse {
        val dummyArtists = listOf(
            SubsonicFileArtist(id = "artist-folder-1", name = "🎻 Folder Artist One"),
            SubsonicFileArtist(id = "artist-folder-2", name = "🎷 Folder Artist Two")
        )
        return SubsonicIndexesResponse(
            sr = SubsonicIndexesResponse2(
                indexes = SubsonicIndexesContainer(
                    index = listOf(
                        SubsonicFileArtistIndex(name = "A", artist = dummyArtists)
                    )
                )
            )
        )
    }

    fun createDummyMusicDirectoryResponse(id: String): SubsonicMusicDirectoryResponse {
        // If id represents an artist folder, return album directories; else return songs
        val children = if (id.startsWith("artist-folder")) {
            listOf(
                SubsonicChild(
                    id = "album-folder-1",
                    parent = id,
                    isDir = true,
                    title = "🗂 Album Folder One"
                ),
                SubsonicChild(
                    id = "album-folder-2",
                    parent = id,
                    isDir = true,
                    title = "🗂 Album Folder Two"
                )
            )
        } else {
            listOf(
                SubsonicChild(
                    id = "song-file-1",
                    parent = id,
                    isDir = false,
                    title = "Dummy Song In Folder",
                    artist = "Folder Artist",
                    album = "Album Folder One",
                    duration = 200,
                    track = 1,
                    coverArt = null
                ),
                SubsonicChild(
                    id = "song-file-2",
                    parent = id,
                    isDir = false,
                    title = "Another Dummy Track",
                    artist = "Folder Artist",
                    album = "Album Folder One",
                    duration = 240,
                    track = 2,
                    coverArt = null
                )
            )
        }
        return SubsonicMusicDirectoryResponse(
            sr = SubsonicMusicDirectoryResponse2(
                directory = SubsonicDirectory(
                    id = id,
                    name = id,
                    child = children
                )
            )
        )
    }

    // New dummy internet radio stations response
    fun createDummyInternetRadioStationsResponse(): SubsonicInternetRadioStationsResponse {
        val stations = listOf(
            SubsonicRadioStation(id = "dummy-station-1", name = "\uD83D\uDCE2 Demo News Radio", streamUrl = "https://example.com/news.mp3", homepageUrl = "https://example.com", bitrate = 128),
            SubsonicRadioStation(id = "dummy-station-2", name = "\uD83C\uDFB5 Demo Music FM", streamUrl = "https://example.com/music.mp3", homepageUrl = "https://music.example.com", bitrate = 192),
            SubsonicRadioStation(id = "dummy-station-3", name = "\uD83D\uDD34 Live Talk Station", streamUrl = "https://example.com/talk.mp3", homepageUrl = null, bitrate = 64)
        )
        return SubsonicInternetRadioStationsResponse(
            sr = SubsonicInternetRadioStationsResponse2(
                internetRadioStations = SubsonicInternetRadioStationsContainer(station = stations)
            )
        )
    }
}
