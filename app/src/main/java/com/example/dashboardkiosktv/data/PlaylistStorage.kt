package com.example.dashboardkiosktv.data

import android.content.Context

class PlaylistStorage(context: Context) {

    private val prefs = context.getSharedPreferences(
        "dashboard_kiosk_settings",
        Context.MODE_PRIVATE
    )

    fun getPlaylistText(): String? {
        return prefs.getString(KEY_PLAYLIST_TEXT, null)
    }

    fun getPlaylistTextOrDefault(): String {
        return prefs.getString(KEY_PLAYLIST_TEXT, DEFAULT_PLAYLIST_TEXT)
            ?: DEFAULT_PLAYLIST_TEXT
    }

    fun isLoopEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOOP_PLAYLIST, true)
    }

    fun savePlaylist(text: String, loop: Boolean) {
        prefs.edit()
            .putString(KEY_PLAYLIST_TEXT, text)
            .putBoolean(KEY_LOOP_PLAYLIST, loop)
            .apply()
    }

    fun hasValidSavedPlaylist(): Boolean {
        val text = getPlaylistText()
        return !text.isNullOrBlank() && PlaylistParser.parse(text).isNotEmpty()
    }

    companion object {
        private const val KEY_PLAYLIST_TEXT = "playlist_text"
        private const val KEY_LOOP_PLAYLIST = "loop_playlist"

        private val DEFAULT_PLAYLIST_TEXT = """
            Example | https://example.com | 10
            Wikipedia | https://www.wikipedia.org | 20
        """.trimIndent()
    }
}