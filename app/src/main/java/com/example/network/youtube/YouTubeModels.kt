package com.example.network.youtube

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubePlaylistResponse(
    @Json(name = "items") val items: List<YouTubePlaylistDto> = emptyList(),
    @Json(name = "nextPageToken") val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistDto(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: YouTubePlaylistSnippetDto? = null,
    @Json(name = "contentDetails") val contentDetails: YouTubePlaylistContentDetailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistSnippetDto(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null,
    @Json(name = "channelTitle") val channelTitle: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistContentDetailsDto(
    @Json(name = "itemCount") val itemCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailsDto(
    @Json(name = "default") val default: YouTubeThumbnailItemDto? = null,
    @Json(name = "medium") val medium: YouTubeThumbnailItemDto? = null,
    @Json(name = "high") val high: YouTubeThumbnailItemDto? = null,
    @Json(name = "standard") val standard: YouTubeThumbnailItemDto? = null,
    @Json(name = "maxres") val maxres: YouTubeThumbnailItemDto? = null
) {
    fun bestUrl(): String? {
        return maxres?.url ?: standard?.url ?: high?.url ?: medium?.url ?: default?.url
    }
}

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailItemDto(
    @Json(name = "url") val url: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemListResponse(
    @Json(name = "items") val items: List<YouTubePlaylistItemDto> = emptyList(),
    @Json(name = "nextPageToken") val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: YouTubePlaylistItemSnippetDto? = null,
    @Json(name = "status") val status: YouTubePlaylistItemStatusDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemSnippetDto(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "position") val position: Int? = null,
    @Json(name = "resourceId") val resourceId: YouTubeResourceIdDto? = null,
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeResourceIdDto(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemStatusDto(
    @Json(name = "privacyStatus") val privacyStatus: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoListResponse(
    @Json(name = "items") val items: List<YouTubeVideoItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "contentDetails") val contentDetails: YouTubeVideoContentDetailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoContentDetailsDto(
    @Json(name = "duration") val duration: String? = null // ISO 8601 like PT15M33S
)
