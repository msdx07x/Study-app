# Study OS — Focused YouTube Study Management for Android

Study OS is a native, offline-friendly study management Android application built with Kotlin, Jetpack Compose, Material 3, and Room. It transforms YouTube educational playlists into structured courses with chapter progress tracking, embedded playback, persistent lesson notes, and scheduled repeat study reminders.

---

## 🌟 Key Features

### 1. YouTube Playlist Import & Organization
- **Smart URL Parser**: Import playlists from standard URLs (`https://www.youtube.com/playlist?list=...`), video watch links with a playlist parameter, or raw playlist IDs.
- **YouTube Data API v3 Integration**: Live fetching of playlist metadata, thumbnails, and complete chapter lists using paginated queries.
- **Curated Starter Courses**: Instant access to educational starter courses (Crash Course Computer Science, 3Blue1Brown Linear Algebra) with real YouTube video IDs—allowing full testing even before an API key is configured.
- **Duplicate Prevention**: Prevents redundant imports while preserving chapter progress, notes, and alarms.

### 2. Official YouTube Embedded Player & Resume Logic
- **Compliant Player**: Implements the official YouTube IFrame Player API inside an Android `WebView` loaded with `https://www.youtube.com` base origin, honoring YouTube branding, controls, and policies.
- **Playback Position Checkpoints**: Saves playback position (in seconds) locally to Room periodically, on pause, and on navigation away.
- **Instant Resume**: Automatically seeks to the exact saved timestamp when reopening a lesson.
- **Automatic Chapter Completion**: Captures the official YouTube Player `ENDED` event to mark the chapter complete automatically.
- **Manual Completion Controls**: Toggle completion on or off at any time.

### 3. Dedicated Lesson Notes (Locally Persistent)
- Accessible directly below the video player in every lesson.
- Real-time debounced auto-save directly to the local Room database (`chapters.notes`).
- Explicit "Save Note" action with feedback snackbar.
- Retains all notes across app restarts and device reboots without an internet connection or account.

### 4. Smart Study Reminders & Reboot Recovery
- **Custom Scheduling**: Choose when the first reminder should fire (in 1 hour, 3 hours, 6 hours, tomorrow, etc.).
- **Repeating Reminders**: Schedule repeat reminders (every 3 hours, 6 hours, daily) that continuously prompt the student until the chapter is marked complete.
- **Auto-Cancellation**: Once a chapter is completed or its parent course is deleted, associated alarms and pending notifications are immediately cancelled.
- **Reboot Resilience**: A `BOOT_COMPLETED` receiver restores pending alarms from the Room database upon device restart.
- **Deep Linking**: Tapping a reminder notification opens the exact course and chapter directly in the lesson player.

### 5. Statistics & Clean Architecture
- **Real Metrics**: Course count, completed chapters, pending chapters, and recent study activity computed directly from database queries.
- **MVVM Architecture**: Clean separation between `AppDatabase`, `CourseRepository`, `UserPreferencesRepository`, `YouTubeRepository`, and `MainViewModel`.
- **Dark Theme Palette**: Engineered with dark charcoal (`#090D16`), deep navy surfaces (`#101726`), and electric blue accents (`#38BDF8`).

---

## 🛠️ How to Configure a YouTube Data API v3 Key

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select a project.
3. Navigate to **APIs & Services > Library** and enable **YouTube Data API v3**.
4. Navigate to **APIs & Services > Credentials** and click **Create Credentials > API Key**.
5. Copy the generated API key.
6. In **Study OS**, open the **Settings** tab (or the import dialog), paste your key into the **YouTube Data API v3** field, and tap **Save Key**.
   *(Alternatively, use the curated starter courses to test immediately without any key).*

---

## 🏗️ Build & Testing Commands

To build the debug APK:
```bash
gradle assembleDebug
```

To run local unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```
