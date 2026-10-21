package com.example.data.repository

import com.example.data.entity.ChapterEntity
import com.example.data.entity.CourseEntity
import com.example.network.youtube.YouTubeApiService
import com.example.network.youtube.YouTubeTimeUtils
import com.example.network.youtube.YouTubeUrlParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class ImportResult {
    data class Success(val course: CourseEntity, val chapters: List<ChapterEntity>) : ImportResult()
    data class Error(val message: String, val isKeyMissing: Boolean = false) : ImportResult()
}

class YouTubeRepository {

    private val apiService: YouTubeApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeApiService::class.java)
    }

    suspend fun importPlaylist(urlOrId: String, apiKey: String): ImportResult = withContext(Dispatchers.IO) {
        val playlistId = YouTubeUrlParser.extractPlaylistId(urlOrId)
            ?: return@withContext ImportResult.Error("Invalid YouTube playlist URL or ID format.")

        if (apiKey.isBlank()) {
            return@withContext ImportResult.Error(
                "YouTube Data API v3 Key is not configured. Please add your key in Settings or load a curated course.",
                isKeyMissing = true
            )
        }

        try {
            // 1. Fetch Playlist details
            val playlistResponse = apiService.getPlaylistDetails(playlistId = playlistId, apiKey = apiKey)
            val playlistItem = playlistResponse.items.firstOrNull()
                ?: return@withContext ImportResult.Error("Playlist not found or is private.")

            val courseTitle = playlistItem.snippet?.title ?: "Untitled Course"
            val courseDesc = playlistItem.snippet?.description
            val courseThumb = playlistItem.snippet?.thumbnails?.bestUrl()

            // 2. Fetch Playlist items with pagination
            val allVideoItems = mutableListOf<com.example.network.youtube.YouTubePlaylistItemDto>()
            var nextPageToken: String? = null

            do {
                val itemsResponse = apiService.getPlaylistItems(
                    playlistId = playlistId,
                    pageToken = nextPageToken,
                    apiKey = apiKey
                )
                allVideoItems.addAll(itemsResponse.items)
                nextPageToken = itemsResponse.nextPageToken
            } while (!nextPageToken.isNullOrEmpty() && allVideoItems.size < 200)

            if (allVideoItems.isEmpty()) {
                return@withContext ImportResult.Error("The playlist does not contain any accessible videos.")
            }

            // 3. Optional: Batch query video durations (up to 50 at a time)
            val videoIds = allVideoItems.mapNotNull { it.snippet?.resourceId?.videoId }.filter { it.isNotBlank() }
            val durationMap = mutableMapOf<String, Int>()

            for (chunk in videoIds.chunked(50)) {
                try {
                    val videoDetails = apiService.getVideoDetails(
                        videoIds = chunk.joinToString(","),
                        apiKey = apiKey
                    )
                    for (videoItem in videoDetails.items) {
                        val durationSeconds = YouTubeTimeUtils.parseIso8601Duration(videoItem.contentDetails?.duration)
                        if (durationSeconds != null) {
                            durationMap[videoItem.id] = durationSeconds
                        }
                    }
                } catch (_: Exception) {
                    // Ignore duration failure, proceed with items
                }
            }

            val courseEntity = CourseEntity(
                playlistId = playlistId,
                title = courseTitle,
                description = courseDesc,
                thumbnailUrl = courseThumb,
                totalKnownVideos = allVideoItems.size
            )

            val chapterEntities = allVideoItems.mapIndexed { index, item ->
                val vId = item.snippet?.resourceId?.videoId ?: ""
                val isPrivate = item.status?.privacyStatus?.equals("private", ignoreCase = true) == true
                val isUnavailable = vId.isBlank() || item.snippet?.title?.contains("Deleted video", ignoreCase = true) == true

                val availability = when {
                    isPrivate -> "PRIVATE"
                    isUnavailable -> "DELETED"
                    else -> "AVAILABLE"
                }

                ChapterEntity(
                    courseId = 0, // Assigned after course insertion
                    videoId = vId,
                    title = item.snippet?.title ?: "Chapter ${index + 1}",
                    thumbnailUrl = item.snippet?.thumbnails?.bestUrl(),
                    durationSeconds = durationMap[vId],
                    playlistOrder = item.snippet?.position ?: index,
                    availabilityStatus = availability
                )
            }

            ImportResult.Success(course = courseEntity, chapters = chapterEntities)

        } catch (e: HttpException) {
            val code = e.code()
            val msg = when (code) {
                400 -> "Bad request. Please verify the playlist URL and API key."
                403 -> "YouTube API access denied or quota limit exceeded (403). Check API key credentials in Google Cloud."
                404 -> "Playlist not found (404). Ensure the playlist is public or unlisted."
                else -> "YouTube API error: ${e.message()}"
            }
            ImportResult.Error(msg)
        } catch (e: IOException) {
            ImportResult.Error("Network error. Please check your internet connection: ${e.localizedMessage}")
        } catch (e: Exception) {
            ImportResult.Error("Failed to import playlist: ${e.localizedMessage}")
        }
    }

    /**
     * Curated starter courses with authentic educational YouTube videos
     * for instant testing without requiring an immediate API key.
     */
    fun getCuratedCourses(): List<Pair<CourseEntity, List<ChapterEntity>>> {
        // Course 1: Crash Course Computer Science
        val csCourse = CourseEntity(
            playlistId = "PL8dPuuaLjXtNlUrzyH5r6jN9ulIgZBpdo",
            title = "Computer Science Fundamentals",
            description = "A comprehensive overview of computer science, computing history, CPU design, memory, and algorithms by Crash Course.",
            thumbnailUrl = "https://i.ytimg.com/vi/tpIctyqH29Q/hqdefault.jpg",
            totalKnownVideos = 5
        )
        val csChapters = listOf(
            ChapterEntity(
                courseId = 0,
                videoId = "tpIctyqH29Q",
                title = "1. Early Computing: Crash Course Computer Science #1",
                thumbnailUrl = "https://i.ytimg.com/vi/tpIctyqH29Q/hqdefault.jpg",
                durationSeconds = 724,
                playlistOrder = 0
            ),
            ChapterEntity(
                courseId = 0,
                videoId = "LN0ucKNX0hc",
                title = "2. Electronic Computing: Crash Course Computer Science #2",
                thumbnailUrl = "https://i.ytimg.com/vi/LN0ucKNX0hc/hqdefault.jpg",
                durationSeconds = 644,
                playlistOrder = 1
            ),
            ChapterEntity(
                courseId = 0,
                videoId = "gI-qXk7XojA",
                title = "3. Boolean Logic & Logic Gates: Crash Course #3",
                thumbnailUrl = "https://i.ytimg.com/vi/gI-qXk7XojA/hqdefault.jpg",
                durationSeconds = 602,
                playlistOrder = 2
            ),
            ChapterEntity(
                courseId = 0,
                videoId = "1S2iRpea56I",
                title = "4. Representing Numbers and Letters with Binary: #4",
                thumbnailUrl = "https://i.ytimg.com/vi/1S2iRpea56I/hqdefault.jpg",
                durationSeconds = 648,
                playlistOrder = 3
            ),
            ChapterEntity(
                courseId = 0,
                videoId = "fpnE6UAfbtU",
                title = "5. How a CPU Works: Crash Course Computer Science #5",
                thumbnailUrl = "https://i.ytimg.com/vi/fpnE6UAfbtU/hqdefault.jpg",
                durationSeconds = 712,
                playlistOrder = 4
            )
        )

        // Course 2: Essence of Linear Algebra (3Blue1Brown)
        val mathCourse = CourseEntity(
            playlistId = "PLZHQObOWTQDPD3MizzM2xVFitgF8hE_ab",
            title = "Essence of Linear Algebra",
            description = "A geometric and intuitive understanding of vectors, matrices, determinants, and linear transformations by 3Blue1Brown.",
            thumbnailUrl = "https://i.ytimg.com/vi/fNk_zzaMoSs/hqdefault.jpg",
            totalKnownVideos = 4
        )
        val mathChapters = listOf(
            ChapterEntity(
                courseId = 0,
                videoId = "fNk_zzaMoSs",
                title = "Vectors, what even are they? | Chapter 1",
                thumbnailUrl = "https://i.ytimg.com/vi/fNk_zzaMoSs/hqdefault.jpg",
                durationSeconds = 590,
                playlistOrder = 0
            ),
            ChapterEntity(
                courseId = 0,
                videoId = "k7RM-ot2NWY",
                title = "Linear combinations, span, and basis | Chapter 2",
                thumbnailUrl = "https://i.ytimg.com/vi/k7RM-ot2NWY/hqdefault.jpg",
                durationSeconds = 598,
                playlistOrder = 1
            ),
            ChapterEntity(
                courseId = 0,
                videoId = "kYB8IZa5AuE",
                title = "Linear transformations and matrices | Chapter 3",
                thumbnailUrl = "https://i.ytimg.com/vi/kYB8IZa5AuE/hqdefault.jpg",
                durationSeconds = 652,
                playlistOrder = 2
            ),
            ChapterEntity(
                courseId = 0,
                videoId = "Ip3X9LOh2dk",
                title = "Matrix multiplication as composition | Chapter 4",
                thumbnailUrl = "https://i.ytimg.com/vi/Ip3X9LOh2dk/hqdefault.jpg",
                durationSeconds = 603,
                playlistOrder = 3
            )
        )

        return listOf(csCourse to csChapters, mathCourse to mathChapters)
    }
}
