package com.example.network.youtube

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("youtube/v3/playlists")
    suspend fun getPlaylistDetails(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") playlistId: String,
        @Query("key") apiKey: String
    ): YouTubePlaylistResponse

    @GET("youtube/v3/playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet,status",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubePlaylistItemListResponse

    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails",
        @Query("id") videoIds: String, // Comma separated
        @Query("key") apiKey: String
    ): YouTubeVideoListResponse
}
