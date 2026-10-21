package com.example.player

enum class YTPlayerState(val code: Int) {
    UNSTARTED(-1),
    ENDED(0),
    PLAYING(1),
    PAUSED(2),
    BUFFERING(3),
    VIDEO_CUED(5);

    companion object {
        fun fromCode(code: Int): YTPlayerState {
            return entries.firstOrNull { it.code == code } ?: UNSTARTED
        }
    }
}
