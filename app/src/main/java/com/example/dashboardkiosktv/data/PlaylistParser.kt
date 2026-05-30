package com.example.dashboardkiosktv.data

object PlaylistParser {

    fun parse(text: String): List<DashboardPage> {
        return text
            .lines()
            .mapNotNull { line ->
                val trimmed = line.trim()

                if (trimmed.isBlank()) {
                    return@mapNotNull null
                }

                val parts = trimmed.split("|").map { it.trim() }

                if (parts.size < 3) {
                    return@mapNotNull null
                }

                val title = parts[0]
                val url = parts[1]
                val seconds = parts[2].toLongOrNull() ?: 30L

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    return@mapNotNull null
                }

                DashboardPage(
                    title = title.ifBlank { "Dashboard" },
                    url = url,
                    displaySeconds = seconds.coerceIn(5, 3600)
                )
            }
    }
}