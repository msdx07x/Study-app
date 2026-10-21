package com.example.network.youtube

import android.net.Uri
import java.util.regex.Pattern

object YouTubeUrlParser {
    private val PLAYLIST_PARAM_REGEX = Pattern.compile("[?&]list=([a-zA-Z0-9_-]+)")
    private val DIRECT_ID_REGEX = Pattern.compile("^[a-zA-Z0-9_-]{12,}$")

    fun extractPlaylistId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // Try extracting via Uri if valid scheme
        try {
            val uri = Uri.parse(trimmed)
            val listParam = uri.getQueryParameter("list")
            if (!listParam.isNullOrBlank()) {
                return listParam
            }
        } catch (_: Exception) {
            // fallback to regex
        }

        // Regex check
        val matcher = PLAYLIST_PARAM_REGEX.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }

        // If user pasted just the playlist ID directly
        if (DIRECT_ID_REGEX.matcher(trimmed).matches()) {
            return trimmed
        }

        return null
    }
}

object YouTubeTimeUtils {
    // Regex for ISO 8601 duration: PT(#H)?(#M)?(#S)?
    private val DURATION_REGEX = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")

    fun parseIso8601Duration(iso: String?): Int? {
        if (iso.isNullOrBlank()) return null
        val matcher = DURATION_REGEX.matcher(iso)
        if (matcher.matches()) {
            val hours = matcher.group(1)?.toIntOrNull() ?: 0
            val minutes = matcher.group(2)?.toIntOrNull() ?: 0
            val seconds = matcher.group(3)?.toIntOrNull() ?: 0
            return (hours * 3600) + (minutes * 60) + seconds
        }
        return null
    }

    fun formatSeconds(totalSeconds: Int?): String {
        if (totalSeconds == null || totalSeconds < 0) return "--:--"
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}
