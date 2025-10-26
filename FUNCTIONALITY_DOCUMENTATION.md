# Planck - Complete Functionality Documentation

**Version:** 1.0.33  
**Package:** nl.mdworld.planck4  
**Target:** Android Automotive OS (AAOS) and Android phones/tablets  
**Last Updated:** October 26, 2025

---

## Overview

Planck is a comprehensive media player application designed for Android Automotive OS with support for:
- **Subsonic/Navidrome** music server streaming
- **Internet radio** streaming with metadata support
- **Offline playback** with intelligent caching
- **Android Auto/Automotive** integration
- **Instrument cluster** display support

---

## Application Architecture

### Core Components

1. **MainActivity** - Main entry point for phone/tablet/automotive display
2. **CarAppActivity** - Android Auto/Car App Service for in-vehicle integration
3. **MediaPlaybackService** - Foreground service handling media playback and notifications
4. **ClusterCarAppService** - Service for instrument cluster integration
5. **PlanckAppState** - Central state management for the entire application
6. **MediaCarSession** - Car app session manager

### Key Technologies

- **Compose UI** - Modern declarative UI framework
- **Ktor Client** - HTTP networking for Subsonic API
- **Media2/MediaSession** - Android media framework integration
- **Coil** - Image loading and caching
- **Kotlin Coroutines** - Asynchronous operations

---

## User Flows

### 1. Application Launch Flow

**Entry Point:** `MainActivity.onCreate()`

1. **Splash Screen** displays during initialization
2. **Notification Permission Request** (Android 13+)
   - If granted: Start MediaPlaybackService
   - If denied: Service starts but notifications won't show
3. **MediaPlaybackService** starts in foreground
4. **Network Monitoring** begins automatically
5. **Playlists Load** from Subsonic server
6. **Main UI** displays with bottom navigation

**Key Files:**
- `MainActivity.kt`
- `MediaPlaybackService.kt`
- `PlanckApp.kt`
- `PlanckAppState.kt`

---

### 2. Browsing Modes

Planck supports **two browsing modes** for music libraries:

#### A. Tags Mode (Metadata-based)
- Uses Subsonic's tag-based API endpoints
- Shows artists with album counts and cover art from metadata
- Faster browsing, relies on proper ID3 tags

#### B. Files Mode (Folder-based)
- Browses server's folder structure directly
- Better for libraries organized by folders
- Optional **folder count enrichment** (makes additional API calls to count albums/songs)

**Settings Location:** Settings Screen → Browsing Mode section

**API Endpoints Used:**
- Tags Mode: `getArtists`, `getArtist`, `getAlbum`
- Files Mode: `getIndexes`, `getMusicDirectory`

---

### 3. Music Library Navigation

#### 3.1 Playlists View (AppScreen.PLAYLISTS)

**Purpose:** Browse and select Subsonic playlists

**User Actions:**
- View all available playlists with cover art
- Tap playlist to navigate to songs
- Pull to refresh playlist list

**Data Loading:**
- Loads on app start via `LaunchedEffect(Unit)`
- Reloads when navigating back to playlists screen
- API: `SubsonicApi.getPlaylistsKtor()`

**Navigation:**
- Tap playlist → SONGS screen with playlist songs

**Key Files:**
- `views/playlists/PlaylistsCarScreen.kt`
- `views/playlists/PlaylistCard.kt`

#### 3.2 Artists View (AppScreen.ARTISTS)

**Purpose:** Browse music by artist

**User Actions:**
- View alphabetically sorted artists
- Search/filter artists with search field
- Tap artist to view their albums
- See album counts per artist (if enabled)

**Data Loading:**
- Loads when navigating to ARTISTS screen
- Different loading based on browsing mode
- Optional enrichment fetches album counts

**Navigation:**
- Tap artist → ALBUMS screen for that artist

**Key Files:**
- `views/library/ArtistsCarScreen.kt`
- `views/library/ArtistCard.kt`
- `views/library/SearchArtistField.kt`

#### 3.3 Albums View (AppScreen.ALBUMS)

**Purpose:** View albums for selected artist

**User Actions:**
- Browse albums by selected artist
- View album cover art, year, song count
- Tap album to view songs
- Pull to refresh (phone UI)

**Data Loading:**
- Triggered by `selectedArtistId` change
- Files mode: May probe subdirectories for multi-disc albums
- Tags mode: Uses artist ID to fetch albums

**Navigation:**
- Tap album → ALBUM_SONGS screen
- Back → Returns to ARTISTS screen

**Key Files:**
- `views/library/AlbumsCarScreen.kt`
- `views/library/AlbumCard.kt`

#### 3.4 Songs View (AppScreen.SONGS)

**Purpose:** Display songs from a playlist

**User Actions:**
- View song list with titles, artists, durations
- Tap song to play
- Pull to refresh playlist
- Swipe down to refresh
- Background cover art display

**Data Loading:**
- Loads when `selectedPlaylistId` changes
- Caches song lists locally for offline access
- Network availability aware

**Offline Support:**
- Cached playlists load when offline
- Network icon shows connectivity status

**Navigation:**
- Tap song → Begins playback
- Back → Returns to PLAYLISTS screen

**Persistence:**
- Last played playlist ID saved
- Auto-opens last playlist on launch (if available)

**Key Files:**
- `views/song/SongsCarScreen.kt`
- `views/song/SongCard.kt`
- `views/song/SongListItem.kt`
- `songcache/SongListCacheManager.kt`

#### 3.5 Album Songs View (AppScreen.ALBUM_SONGS)

**Purpose:** Display songs from a specific album

**User Actions:**
- View album tracks
- Play individual songs
- Navigate through multi-disc albums
- Pull to refresh

**Data Loading:**
- Triggered by `selectedAlbumId` change
- Handles multi-disc albums (folders within folders in Files mode)
- Caches album song lists

**Multi-Disc Support:**
- Automatically detects disc subdirectories
- Aggregates songs across all discs
- Maintains proper track ordering

**Navigation:**
- Back → Returns to ALBUMS screen for artist

**Key Files:**
- `views/library/AlbumSongsCarScreen.kt`
- `songcache/SongListCacheManager.kt`

---

### 4. Radio Streaming Flow

#### 4.1 Radio Screen (AppScreen.RADIO)

**Purpose:** Stream live internet radio with metadata

**User Actions:**
- Start/stop radio stream
- View current track and broadcast info
- See previous tracks with timestamps
- Skip to last played song during commercial breaks

**Radio URL Configuration:**
- Default: NPO Radio 2 (Dutch public radio)
- Customizable in Settings
- Can select from server's internet radio stations

**Metadata Features:**

1. **ICY Metadata** - Extracted from stream headers
2. **API Metadata** - For supported stations (NPO Radio 2)
   - Current song title, artist, cover art
   - Broadcast name and presenters
   - Historical track list with timestamps
3. **Fallback** - Generic metadata if unavailable

**Radio Skip Feature:**

The radio has a unique "skip" functionality:
- **Purpose:** Skip commercials/ads while keeping radio context
- **How it works:**
  1. Tap skip button while radio playing
  2. Radio pauses, last played song/album resumes
  3. App monitors radio metadata for track changes
  4. When new song detected on radio → auto-resume radio
  5. Tap skip again (now shows X icon) to cancel and resume radio immediately

**Key State Variables:**
- `isRadioPlaying` - Radio stream active
- `isRadioSkipping` - Skip mode active
- `isRadioTemporarilyPausedForSkip` - Internal skip state
- `radioMetadata` - List of metadata objects

**Metadata Manager:**
- Polls every 5 seconds for updates
- Supports multiple strategies (ICY, API-based)
- Station-specific presets (NPO Radio 2, Sky Radio)

**Key Files:**
- `views/radio/RadioScreen.kt`
- `views/radio/RadioMetadataManager.kt`
- `views/radio/RadioMetadataManagerFactory.kt`
- `util/radiometadata/IcyMetadataFetcher.kt`
- `util/radiometadata/strategies/ApiMetadataStrategy.kt`
- `util/radiometadata/presets/NPO2Preset.kt`

---

### 5. Playback Control System

#### 5.1 Song Playback

**Playback Flow:**

1. **User taps song** → `PlanckAppState.playStream(song)`
2. **Stop current playback** (if any)
3. **Set active song** and update UI
4. **Determine data source:**
   - Cached file (if available) → Play from cache
   - Network available → Stream from Subsonic
   - Offline + not cached → Show error message
5. **Create MediaPlayer** with audio attributes
6. **Prepare async** and start on ready
7. **Update MediaSession** with metadata
8. **Start progress tracking** (500ms intervals)
9. **Launch prefetch** for next songs
10. **Send metadata to service** for notification

**Auto-Advance:**
- On completion → `playNextSong()` automatically
- Loops to beginning when reaching end of playlist

**Persistence:**
- Last played song ID saved
- Last playlist/folder ID saved
- Used for radio skip feature restoration

#### 5.2 Playback Controls

**Available Controls:**

- **Play/Pause** - Toggle playback
  - Media buttons: KEYCODE_MEDIA_PLAY_PAUSE, KEYCODE_S
- **Next Song** - Skip to next track
  - Media buttons: KEYCODE_MEDIA_NEXT, KEYCODE_D
- **Previous Song** - Go to previous track
  - Media buttons: KEYCODE_MEDIA_PREVIOUS, KEYCODE_A
- **Seek** - Progress bar (phone UI only)

**Control Sources:**

1. **Bottom App Bar** - In-app controls
2. **Notification** - System notification controls
3. **Media Buttons** - Bluetooth/physical buttons
4. **MediaSession** - System media controls
5. **Android Auto** - Car display controls

**Key Code Handling:**
- Tracked via `KeyCodeTracker` for debugging
- Shows recent key codes in Settings (debug feature)

#### 5.3 Progress Tracking

**Progress Update System:**

- Updates every 500ms during playback
- Tracks: `currentPosition`, `duration`
- Displays: Linear progress bar, time remaining
- Updates MediaSession playback state

**Playback States:**
- `STATE_NONE` - No active playback
- `STATE_BUFFERING` - Loading/preparing
- `STATE_PLAYING` - Actively playing
- `STATE_PAUSED` - Paused
- `STATE_STOPPED` - Stopped
- `STATE_ERROR` - Playback error
- `STATE_SKIPPING_TO_NEXT` - Skip transition
- `STATE_SKIPPING_TO_PREVIOUS` - Previous transition

---

### 6. Offline & Caching System

#### 6.1 Song Cache (500MB)

**Purpose:** Cache streamed songs for offline playback

**How It Works:**

1. **Automatic Prefetch:**
   - When song starts playing → prefetch queue
   - Next song prioritized
   - Then remaining songs in order
   - 500ms delay between downloads

2. **Cache Storage:**
   - Location: `context.cacheDir/song_cache/`
   - Max size: 500MB
   - Filename: MD5 hash + song ID snippet

3. **Eviction Policy:**
   - LRU (Least Recently Used)
   - Oldest files deleted first when space needed
   - Touch cached files on access to update timestamp

4. **Offline Playback:**
   - Network loss detected → use cached files
   - Uncached songs → error message shown
   - Resume prefetch when network returns

**API:**
- `SongCacheManager.cacheSongIfNeeded()` - Download and cache
- `SongCacheManager.getCachedFile()` - Retrieve cached file
- `SongCacheManager.isCached()` - Check cache status
- `SongCacheManager.clear()` - Clear entire cache
- `SongCacheManager.sizeBytes()` - Get cache size

**Key Files:**
- `songcache/SongCacheManager.kt`

#### 6.2 Song List Cache

**Purpose:** Cache playlist/album song lists for offline browsing

**Storage:**
- Playlist lists: `song_lists/playlist_{id}.json`
- Album lists: `song_lists/album_{id}.json`
- JSON serialization of song objects

**Usage:**
- Fallback when network unavailable
- Faster initial display
- Reduced server load

**Key Files:**
- `songcache/SongListCacheManager.kt`

#### 6.3 Album Art Cache (100MB)

**Purpose:** Cache cover art images

**Implementation:**
- Managed by Coil ImageLoader
- Disk cache: 100MB limit
- Location: `context.cacheDir/album_art_cache/`

**Features:**
- Automatic HTTP caching
- Memory + disk caching
- Placeholder/error images
- Size configurable in `PlanckApplication`

**Management:**
- Clear cache from Settings
- View cache size in Settings
- Manual clear via Settings UI

**Key Files:**
- `imageloading/CoverArtCacheManager.kt`
- `PlanckApplication.kt` (Coil configuration)

#### 6.4 Network Monitoring

**Network Monitor:**
- Observes connectivity changes via `NetworkMonitor`
- Updates `PlanckAppState.isNetworkAvailable`
- Shows network status icon in UI

**Network Loss Handling:**
- Radio: Auto-stop + notification message
- Songs: Switch to cached playback
- Browsing: Load from cached lists
- Prefetch: Paused until network returns

**Network Restore:**
- Resume prefetch queue
- Clear offline message
- Allow streaming again

**Key Files:**
- `networking/NetworkMonitor.kt`

---

### 7. Settings & Configuration

#### 7.1 Settings Screen (AppScreen.SETTINGS)

**Subsonic Settings:**

- **Server URL** - REST API endpoint
  - Default: `https://example.com/rest/`
  - Format: Must end with `/rest/`
  
- **Username** - Subsonic authentication
  - Default: `demo_user`
  
- **Password** - Authentication password/token
  - Default: `demo_password`
  - Show/hide toggle for security
  
- **Radio URL** - Internet radio stream
  - Default: NPO Radio 2 stream
  - Can select from server's radio stations

**Library Settings:**

- **Browsing Mode** - Tags vs Files
  - Radio button selection
  - Default: FILES mode
  - Affects artist/album browsing

- **Folder Count Enrichment** - Toggle
  - When enabled: Makes extra API calls to count items
  - Shows accurate album/song counts in Files mode
  - Slower but more informative
  - Default: Disabled

**UI Settings:**

- **Overlay Opacity** - Background cover art darkness
  - Slider: 0.0 to 1.0
  - Default: 0.8 (80% dark overlay)
  - Auto-saves on change
  - Affects song/album song screens

**Cache Management:**

- **Album Art Cache**
  - View current size (calculated async)
  - Clear cache button
  - Shows size in MB

- **Song Cache**
  - View current size
  - Clear cache button
  - Max 500MB

**Internet Radio Stations:**

- **Load from Server** - Fetches available stations
  - Lists station names and URLs
  - Tap to set as radio URL
  - Refresh button to reload

**Debug Information:**

- **Key Codes Section**
  - Shows last 10 key presses
  - Key code number + name
  - Useful for troubleshooting remote controls

**About Section:**

- App version display
- Build information

**Save Behavior:**
- Most settings: Manual "Save Settings" button
- Overlay opacity: Auto-saves on change
- Cache clears: Immediate action

**Key Files:**
- `views/settings/SettingsScreen.kt`
- `views/settings/SettingsCarScreen.kt`
- `views/settings/SettingsSection.kt`
- `views/settings/AlbumArtCacheSection.kt`
- `views/settings/SongCacheSection.kt`
- `views/settings/DebugKeyCodesSection.kt`
- `SettingsManager.kt`

#### 7.2 Settings Persistence

**SharedPreferences Keys:**
- `server_url` - Subsonic server URL
- `username` - Username
- `password` - Password/token
- `radio_url` - Radio stream URL
- `overlay_opacity` - Background overlay value
- `browsing_mode` - TAGS or FILES
- `folder_count_enrich` - Boolean flag
- `last_song_id` - Last played song
- `last_playlist_id` - Last playlist context
- `last_folder_id` - Last album/folder context

**Preferences File:** `planck_prefs` (MODE_PRIVATE)

---

### 8. Android Auto/Automotive Integration

#### 8.1 Car App Service

**CarAppActivity:**
- Extends `CarAppService`
- Entry point for Android Auto
- Creates `MediaCarSession`
- Allows all hosts (development)

**MediaCarSession:**
- Manages car app lifecycle
- Creates `MediaCarScreen` as root

**Manifest Configuration:**
```xml
<service android:name=".CarAppActivity"
    android:exported="true"
    android:permission="androidx.car.app.ACCESS_SURFACE">
    <intent-filter>
        <action android:name="androidx.car.app.CarAppService" />
        <category android:name="androidx.car.app.category.MEDIA" />
    </intent-filter>
    <meta-data android:name="distractionOptimized" android:value="true"/>
</service>
```

#### 8.2 Car Screens

**MediaCarScreen:**
- Main navigation hub for car mode
- Grid layout with navigation buttons:
  - Playlists
  - Artists
  - Radio
  - Settings
- Lifecycle aware
- Focus management for rotary controls

**Screen Implementations:**
- `PlaylistsCarScreen` - Browse playlists
- `SongsCarScreen` - View playlist songs
- `ArtistsCarScreen` - Browse artists
- `AlbumsCarScreen` - View artist albums
- `AlbumSongsCarScreen` - View album tracks
- `RadioCarScreen` - Radio controls and metadata
- `SettingsCarScreen` - Configuration in car

**Car-Specific Features:**
- Simplified layouts for safety
- Larger touch targets
- Limited text input
- Distraction-optimized templates
- Template-based UI (not Compose in car screens)

**Key Files:**
- `CarAppActivity.kt`
- `MediaCarSession.kt`
- `MediaCarScreen.kt`
- `RadioCarScreen.kt`
- `views/library/ArtistsCarScreen.kt`
- `views/library/AlbumsCarScreen.kt`
- `views/playlists/PlaylistsCarScreen.kt`
- `views/song/SongsCarScreen.kt`

#### 8.3 Instrument Cluster Integration

**ClusterCarAppService:**
- Separate service for cluster display
- Shows "Planck is running" message
- Minimal information display

**ClusterScreen:**
- MessageTemplate-based UI
- Refreshable display
- Static content (no interactive controls)

**Manifest Configuration:**
```xml
<service android:name=".ClusterCarAppService"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.car.app.CarAppService" />
    </intent-filter>
</service>
```

**Key Files:**
- `ClusterCarAppService.kt`
- `ClusterSession.kt`

#### 8.4 Rotary Controller Support

**RotaryControllerHandler:**
- Handles rotary input events
- Scroll support
- Focus management
- Navigation between items

**Focus Management:**
- `FocusParkingView` - Focus restoration
- `FocusRequester` - Compose focus control
- Rotary-friendly layouts

**Key Files:**
- `RotaryControllerHandler.kt`
- `FocusParkingView.kt`

---

### 9. Media Session & Notifications

#### 9.1 MediaPlaybackService

**Service Type:** Foreground MediaBrowserService

**Responsibilities:**
1. Media session management
2. Persistent notification display
3. Media button handling
4. External control support (Auto, Android TV, etc.)

**Lifecycle:**
- Started by MainActivity on launch
- Runs in foreground when playing
- Notification shows playback state
- Stops when app destroyed

#### 9.2 Media Session

**Session Configuration:**
- Flags: Handles media buttons + transport controls
- Actions: Play, Pause, Stop, Next, Previous
- Callback: Routes controls to app logic

**Metadata Updates:**
- Song title, artist, album
- Duration
- Cover art (loaded async via Coil)

**Playback State:**
- State, position, playback speed
- Available actions
- Error states

**Session Callbacks:**
```kotlin
override fun onPlay()
override fun onPause()
override fun onStop()
override fun onSkipToNext()
override fun onSkipToPrevious()
```

**Note:** Current implementation has duplicate MediaSession:
- One in PlanckAppState (direct control)
- One in MediaPlaybackService (system integration)
- TODO: Consolidate to service only (see notes.md)

#### 9.3 Notification System

**Notification Channel:**
- ID: `planck_playback`
- Importance: LOW
- Name: "Playback"

**Notification Content:**
- Title: Song title or "Planck"
- Text: Artist/album or playback state
- Large Icon: Album art (512px, loaded async)
- Small Icon: App launcher icon

**Notification States:**
- Playing: Ongoing notification
- Paused: Dismissible notification
- Stopped: Hidden or shows "Ready"
- Buffering: Shows loading state

**Foreground Service:**
- Required for Android 8+
- Type: `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- Permission: `FOREGROUND_SERVICE`
- Promotes/demotes based on playback state

**Actions (Planned - see notes.md):**
- Play/Pause button
- Next track button
- Previous track button
- Close button

**Key Files:**
- `MediaPlaybackService.kt`

---

### 10. Audio Management

#### 10.1 AppAudioManager

**Purpose:** Centralized MediaPlayer lifecycle management

**Functionality:**
- Tracks all active MediaPlayer instances
- Ensures only one player active at a time
- Cleanup on app background/destroy
- Prevents audio leaks

**API:**
```kotlin
AppAudioManager.register(mediaPlayer) // Register new player
AppAudioManager.cleanupAllState() // Stop and release all
```

**Usage Pattern:**
1. Create MediaPlayer
2. Register with AppAudioManager
3. Manager handles cleanup automatically

**Process Lifecycle Integration:**
- `PlanckApplication` observes lifecycle
- Cleanup on `ON_STOP` (app backgrounded)
- Prevents audio playing when app hidden

**Key Files:**
- `AppAudioManager.kt`
- `PlanckApplication.kt`

#### 10.2 Audio Focus

**Implementation:** Via AudioAttributes

**Audio Attributes:**
```kotlin
AudioAttributes.Builder()
    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
    .build()
```

**Focus Handling:**
- Automatic with MediaPlayer + AudioAttributes
- System manages focus transitions
- Ducking for notifications/navigation
- Pause for phone calls

---

### 11. Data Models

#### 11.1 Song Model

```kotlin
data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Int?,  // seconds
    val coverArt: String?  // ID or URL
)
```

**Special Songs:**
- Radio: `id = "radio-stream"`, `duration = 0`

#### 11.2 Playlist Model

```kotlin
data class Playlist(
    val id: String,
    val coverArt: String?,
    val name: String
)
```

#### 11.3 Artist Model

```kotlin
data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val coverArt: String?
)
```

#### 11.4 Album Model

```kotlin
data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val artistId: String,
    val songCount: Int,
    val duration: Int,  // seconds
    val coverArt: String?,
    val year: Int?
)
```

#### 11.5 Radio Metadata

```kotlin
data class RadioMetadata(
    val song: RadioSong?,
    val broadcast: RadioBroadcast?,
    val time: RadioTime?
)

data class RadioSong(
    val title: String?,
    val artist: String?,
    val imageUrl: String?
)

data class RadioBroadcast(
    val title: String?,
    val presenters: String?,
    val imageUrl: String?
)

data class RadioTime(
    val start: String?,  // ISO format
    val end: String?
)
```

---

### 12. Subsonic API Integration

#### 12.1 API Client

**Implementation:** Ktor HttpClient

**Configuration:**
- Content negotiation: JSON
- Logging: All requests/responses
- Engine: Android-optimized (CIO)

**Authentication:**
- Uses username + password credentials
- Demo mode when credentials are default values
- Returns dummy data in demo mode

#### 12.2 Supported Endpoints

**Browsing:**
- `getPlaylists` - List all playlists
- `getPlaylist` - Get playlist with songs
- `getArtists` - List artists (tag mode)
- `getArtist` - Get artist albums (tag mode)
- `getAlbum` - Get album with songs (tag mode)
- `getIndexes` - List folder index (files mode)
- `getMusicDirectory` - Get folder contents (files mode)
- `getInternetRadioStations` - List radio stations

**Streaming:**
- `stream` - Stream song by ID
- `getCoverArt` - Get cover art image

#### 12.3 URL Builder

**SubsonicUrlBuilder:**
- Builds REST API URLs
- Appends authentication parameters
- Handles URL encoding
- Configurable server URL

**Example URLs:**
```
{server}/rest/getPlaylists?u={user}&p={pass}&v=1.16.1&c=Planck&f=json
{server}/rest/stream?id={songId}&u={user}&p={pass}&v=1.16.1&c=Planck
{server}/rest/getCoverArt?id={coverArtId}&u={user}&p={pass}&size=512
```

#### 12.4 Response Entities

**Naming Convention:** `Subsonic*Response`, `Subsonic*Entity`

**Examples:**
- `SubsonicPlaylistsResponse`
- `SubsonicSongEntity`
- `SubsonicAlbumEntity`
- `SubsonicArtistEntity`

**Structure:** Nested response wrapper
```kotlin
data class SubsonicPlaylistsResponse(
    @SerialName("subsonic-response")
    val sr: SubsonicPlaylistsWrapper
)
```

#### 12.5 Dummy Responses

**SubsonicDummyResponses:**
- Provides fake data for demo mode
- Used when credentials not configured
- Allows app testing without server

**Key Files:**
- `networking/subsonic/SubsonicApi.kt`
- `networking/subsonic/SubsonicUrlBuilder.kt`
- `networking/subsonic/SubsonicDummyResponses.kt`
- `networking/subsonic/*Entity.kt`

---

### 13. UI Components

#### 13.1 Bottom App Bar

**PlanckBottomAppBar:**
- Persistent across all screens
- Shows current song info
- Playback controls
- Progress indicator
- Navigation buttons

**Components:**
- Back button (context-aware)
- Song title display
- Play/Pause button
- Next/Previous buttons
- Linear progress bar

**Behavior:**
- Auto-hides when no song active (configurable)
- Updates in real-time with playback
- Touch targets sized for automotive use

#### 13.2 Navigation System

**Bottom Navigation Bar:**
- Playlists
- Artists
- Radio
- Settings

**NavigationButton Component:**
- Icon + label
- Selected state highlighting
- Automotive-sized touch targets

#### 13.3 Card Components

**PlaylistCard:**
- Cover art thumbnail
- Playlist name
- Tap to navigate

**ArtistCard:**
- Artist name
- Album count
- Cover art (if available)
- Search/filter support

**AlbumCard:**
- Album art
- Album name
- Artist name
- Year, song count, duration
- Multi-line text support

**SongCard / SongListItem:**
- Song title
- Artist - Album
- Duration (formatted MM:SS)
- Playing indicator
- Cover art
- Tap to play

#### 13.4 Background Cover Art

**BackgroundCoverArt Component:**
- Full-screen album art background
- Configurable overlay opacity
- Blur effect
- Fallback to solid color
- Used in SONGS and ALBUM_SONGS screens

**Implementation:**
- Coil AsyncImage
- ContentScale.Crop
- Dark overlay (alpha from settings)

#### 13.5 Specialized Components

**MessageCard:**
- Info/error messages
- Transient notifications
- Network status alerts

**CoverArt:**
- Reusable album art display
- Placeholder images
- Error handling
- Size variants

**SettingsSwitch:**
- Toggle switches for settings
- Label + description
- Immediate or deferred save

**OverlayOpacitySlider:**
- Dedicated slider for background opacity
- Live preview
- Auto-save on change

---

### 14. State Management

#### 14.1 PlanckAppState

**Singleton Pattern:** Accessed via `PlanckAppStateHolder`

**State Variables:**

**Navigation:**
- `currentScreen: AppScreen`
- `selectedPlaylistId: String?`
- `selectedArtistId: String?`
- `selectedAlbumId: String?`

**Data:**
- `playlists: SnapshotStateList<Playlist>`
- `songs: SnapshotStateList<Song>`
- `artists: SnapshotStateList<Artist>`
- `albums: SnapshotStateList<Album>`

**Playback:**
- `activeSong: Song?`
- `isPlaying: Boolean`
- `currentPosition: Int`
- `duration: Int`
- `currentSongIndex: Int`

**Radio:**
- `isRadioPlaying: Boolean`
- `radioMetadata: List<RadioMetadata>`
- `isRadioSkipping: Boolean`

**System:**
- `isNetworkAvailable: Boolean`
- `transientMessage: String?`
- `isSongsRefreshing: Boolean`

**Methods:**

**Navigation:**
- `navigateToSongs(playlistId, name)`
- `navigateToPlaylists()`
- `navigateToArtists()`
- `navigateToAlbums(artistId, name)`
- `navigateToAlbumSongs(albumId, name)`
- `navigateToRadio()`
- `navigateToSettings()`

**Playback:**
- `playStream(song)`
- `stopPlayback()`
- `pausePlayback()`
- `resumePlayback()`
- `playNextSong()`
- `playPreviousSong()`

**Radio:**
- `startRadio()`
- `stopRadio()`
- `skipRadioToLastContext()`

**Utilities:**
- `triggerReload()`
- `openLastPlaylistIfAvailable()`
- `consumeTransientMessage()`

#### 14.2 MainViewModel

**Purpose:** Bridge between Activity and Compose state

**Holds Reference:** To PlanckAppState

**Usage:**
- Activity accesses for hardware button handling
- Lifecycle-aware state preservation

---

### 15. Testing & Debugging

#### 15.1 Debug Features

**Key Code Tracker:**
- Records last 10 key presses
- Shows key code + name
- Displayed in Settings screen
- Helps debug remote control issues

**Network Status:**
- Visual indicator in UI
- Console logging of network changes
- Offline mode testing

**API Logging:**
- Ktor client logs all requests
- Response bodies logged
- Error details captured

#### 15.2 Demo Mode

**Triggers:**
- Default username/password not changed
- Provides sample data for testing

**Dummy Data:**
- Sample playlists
- Sample artists/albums
- Sample songs
- Allows UI testing without server

#### 15.3 Sample Data

**SampleData.kt:**
- Mock data for previews
- Compose preview support
- UI development without backend

---

### 16. Special Features

#### 16.1 Pull-to-Refresh

**Screens with PTR:**
- Playlists (phone UI)
- Songs (phone UI)
- Album songs (phone UI)

**Implementation:**
- `eu.bambooapps:compose-material3-pullrefresh`
- Visual indicator
- Triggers data reload
- Network-aware

#### 16.2 Splash Screen

**Implementation:**
- `androidx.core:core-splashscreen`
- Theme: `Theme.Planck.SplashScreen`
- Auto-dismisses when content ready
- Smooth transition to main UI

#### 16.3 Persistence

**Auto-Resume Features:**
- Last playlist remembered
- Last song tracked
- Radio skip context preserved
- Playback position restored (planned)

**Settings Persistence:**
- All settings saved to SharedPreferences
- Survives app restarts
- Backed up via Android Auto Backup

#### 16.4 Multi-Disc Album Support

**Detection:**
- Folders within album folders
- Each subfolder is a disc
- Aggregates songs from all discs

**Handling:**
- Automatic in Files browsing mode
- Maintains track order
- Counts songs across all discs

---

### 17. Error Handling

#### 17.1 Network Errors

**Strategies:**
- Fallback to cached data
- User-friendly error messages
- Retry on network restore
- Graceful degradation

#### 17.2 Playback Errors

**MediaPlayer Errors:**
- OnErrorListener captures
- State reset to ERROR
- User notification
- Cleanup and release

**Offline Errors:**
- "Song not cached" message
- Prevents playback attempt
- Suggests caching songs

#### 17.3 API Errors

**Handling:**
- Try-catch on all API calls
- Dummy data in demo mode
- Empty lists on failure
- Console logging for debugging

---

### 18. Performance Optimizations

#### 18.1 Lazy Loading

**LaunchedEffect Usage:**
- Data loads only when screen visible
- Cancels on screen exit
- Prevents unnecessary API calls

**Lazy Lists:**
- LazyColumn for long lists
- Only renders visible items
- Smooth scrolling

#### 18.2 Caching Strategy

**Three-Tier Caching:**
1. **Memory** - Compose state lists
2. **Disk** - Song cache + song list cache
3. **Server** - Subsonic API (fallback)

**Benefits:**
- Offline support
- Reduced server load
- Faster app startup
- Lower data usage

#### 18.3 Image Loading

**Coil Optimization:**
- Disk cache (100MB)
- Memory cache (auto-managed)
- Downsampling to 512px
- Placeholder/error images
- AsyncImage composable

#### 18.4 Prefetch Queue

**Smart Prefetching:**
- Next song prioritized
- Remaining songs in order
- 500ms delay between downloads
- Pauses when offline
- Background coroutine scope

---

### 19. Permissions

#### 19.1 Required Permissions

**Manifest Permissions:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

#### 19.2 Runtime Permissions

**POST_NOTIFICATIONS (Android 13+):**
- Requested on app launch
- Required for foreground service notification
- Handled gracefully if denied

**Permission Flow:**
1. Check if granted
2. Request if needed
3. Start service regardless
4. Notification won't show if denied

---

### 20. Build & Distribution

#### 20.1 Build Configuration

**Gradle (Kotlin DSL):**
- Min SDK: 30 (Android 11)
- Target SDK: 36
- Compile SDK: 36
- Version Code: 33
- Version Name: 1.0.33

**Build Types:**
- Debug: Debuggable, no obfuscation
- Release: Minify disabled (for now)

#### 20.2 Signing & Publishing

**Keystore:** `play-store/keystore`

**Build Process:**
1. Bump versionCode and versionName
2. Build → Generate Signed Bundle/APK
3. Upload to Google Play Console
4. Closed test for Automotive track

**Important:** Internal tests NOT shown on car

**Key Files:**
- `app/build.gradle.kts`
- `play-store/keystore`
- `play-store/key_output.zip`

---

### 21. Known Issues & TODOs

#### From notes.md:

1. ✅ **MediaStyle notification** - Add transport actions (play/pause/next/prev)
2. ✅ **Album art in notification** - Using setLargeIcon
3. ❌ **Consolidate MediaSession** - Currently duplicated in PlanckAppState and Service
4. ❌ **Move playback to service** - Service should be single source of truth

#### Other Areas:

- **Seek functionality** - Only in phone UI, not in car mode
- **Queue management** - No reordering/queue view
- **Search** - Only artist search, no global search
- **Favorites** - No starring/favoriting songs
- **Download management** - No manual download selection
- **Playback speed** - No speed adjustment
- **Equalizer** - No EQ support
- **Gapless playback** - Not implemented
- **Crossfade** - Not implemented
- **Lyrics** - No lyrics display
- **Podcast support** - No podcast features

---

### 22. Dependencies

#### Key Libraries:

**Compose:**
- `androidx.compose:compose-bom:2024.09.01`
- `androidx.compose.material3`
- `androidx.compose.material:material-icons-extended`

**Navigation:**
- `androidx.navigation:navigation-compose:2.9.5`

**Networking:**
- `io.ktor:ktor-client-core:3.3.1`
- `io.ktor:ktor-client-android:3.3.1`
- `io.ktor:ktor-serialization-kotlinx-json:3.3.1`

**Serialization:**
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0`

**Car/Auto:**
- `androidx.car.app:app:1.7.0`

**Media:**
- `androidx.media:media:1.7.1`
- `androidx.media2:media2-session:1.3.0`

**Image Loading:**
- `io.coil-kt:coil-compose:2.7.0`

**UI:**
- `eu.bambooapps:compose-material3-pullrefresh:1.1.1`
- `androidx.core:core-splashscreen:1.0.1`

**Lifecycle:**
- `androidx.lifecycle:lifecycle-process:2.9.4`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.9.4`

---

### 23. File Structure Reference

```
app/src/main/java/nl/mdworld/planck4/
├── MainActivity.kt - Main activity entry point
├── PlanckApp.kt - Root Compose UI
├── PlanckAppState.kt - Central state management
├── PlanckApplication.kt - Application class
├── MainViewModel.kt - ViewModel bridge
├── SettingsManager.kt - Preferences management
├── CarAppActivity.kt - Android Auto service
├── MediaPlaybackService.kt - Foreground media service
├── MediaCarSession.kt - Car app session
├── MediaCarScreen.kt - Car navigation hub
├── ClusterCarAppService.kt - Cluster service
├── ClusterSession.kt - Cluster screen
├── PlanckBottomAppBar.kt - Bottom controls
├── AppAudioManager.kt - Audio lifecycle manager
├── KeyCodeTracker.kt - Debug key tracking
├── RotaryControllerHandler.kt - Rotary input
├── FocusParkingView.kt - Focus management
├── MessageCard.kt - UI message component
├── SampleData.kt - Mock data for previews
│
├── views/
│   ├── playlists/
│   │   ├── PlaylistsCarScreen.kt
│   │   ├── PlaylistCard.kt
│   │   └── PlaylistModels.kt
│   ├── song/
│   │   ├── SongsCarScreen.kt
│   │   ├── SongCard.kt
│   │   ├── SongListItem.kt
│   │   ├── BackgroundCoverArt.kt
│   │   └── SongsHeaderCard.kt
│   ├── library/
│   │   ├── ArtistsCarScreen.kt
│   │   ├── ArtistCard.kt
│   │   ├── AlbumsCarScreen.kt
│   │   ├── AlbumCard.kt
│   │   ├── AlbumSongsCarScreen.kt
│   │   ├── SearchArtistField.kt
│   │   └── AlbumModels.kt
│   ├── radio/
│   │   ├── RadioScreen.kt
│   │   ├── RadioCarScreen.kt
│   │   ├── RadioMetadataManager.kt
│   │   └── RadioMetadataManagerFactory.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   ├── SettingsCarScreen.kt
│   │   ├── SettingsSection.kt
│   │   ├── SettingsItem.kt
│   │   ├── SettingsSwitch.kt
│   │   ├── OverlayOpacitySlider.kt
│   │   ├── AlbumArtCacheSection.kt
│   │   ├── SongCacheSection.kt
│   │   ├── DebugKeyCodesSection.kt
│   │   └── AboutSection.kt
│   └── components/
│       ├── CoverArt.kt
│       └── NavigationButton.kt
│
├── networking/
│   ├── NetworkMonitor.kt
│   ├── config.kt
│   ├── subsonic/
│   │   ├── SubsonicApi.kt
│   │   ├── SubsonicUrlBuilder.kt
│   │   ├── SubsonicDummyResponses.kt
│   │   ├── Subsonic*Entity.kt (multiple files)
│   │   └── SubsonicRadioStationsEntities.kt
│   └── hass/
│       ├── DomoticzStatusApi.kt
│       └── DomoticzStatusEntity.kt
│
├── songcache/
│   ├── SongCacheManager.kt
│   └── SongListCacheManager.kt
│
├── imageloading/
│   ├── CoverArtCacheManager.kt
│   └── CoverArtRequests.kt
│
├── util/
│   └── radiometadata/
│       ├── IcyMetadataFetcher.kt
│       ├── RadioMetadataUtil.kt
│       ├── MetadataCallback.kt
│       ├── strategies/
│       │   ├── MetadataStrategy.kt
│       │   ├── IcyMetadataStrategy.kt
│       │   ├── ApiMetadataStrategy.kt
│       │   └── ApiMetadataUtil.kt
│       └── presets/
│           ├── NPO2Preset.kt
│           └── SkyPreset.kt
│
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

---

### 24. Compatibility

**Supported Platforms:**
- Android Automotive OS (primary target)
- Android phones (testing/debugging)
- Android tablets (testing/debugging)
- Android Auto (via Car App API)

**Android Versions:**
- Minimum: Android 11 (API 30)
- Target: Android 15+ (API 36)

**Screen Orientations:**
- Landscape (automotive primary)
- Portrait (phone support)

**Hardware Requirements:**
- Internet connectivity (WiFi or cellular)
- Audio output
- Touch or rotary input

---

### 25. Security Considerations

**Credentials Storage:**
- SharedPreferences (MODE_PRIVATE)
- NOT encrypted (TODO: Use EncryptedSharedPreferences)
- Plain text password storage

**Network Security:**
- HTTPS recommended for Subsonic server
- No certificate pinning
- Standard Android network security config

**Permissions:**
- Minimal required permissions
- Runtime permission for notifications
- No sensitive data access

---

## Conclusion

This documentation captures the complete functionality of Planck v1.0.33 as of October 26, 2025. It includes:

- ✅ All user flows and navigation paths
- ✅ Complete playback system (songs + radio)
- ✅ Offline caching strategy (500MB songs, 100MB art, song lists)
- ✅ Android Auto/Automotive integration
- ✅ Settings and configuration options
- ✅ API integration details
- ✅ State management architecture
- ✅ UI components and screens
- ✅ Special features (radio skip, multi-disc, etc.)
- ✅ Error handling and edge cases
- ✅ Build and deployment process

This document serves as:
1. **User manual** - Understanding app capabilities
2. **Developer reference** - Maintaining and extending features
3. **AI assistant context** - Ensuring no functionality is lost during modifications

**For AI Assistants:** When modifying this codebase, refer to this document to understand:
- Existing functionality that must be preserved
- User flows that should not break
- Integration points between components
- Expected behavior for edge cases

