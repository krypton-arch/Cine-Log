package com.exmple.cinelog.domain

import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity

data class ProjectionistContext(
    val recentLogs: List<LogEntry>,
    val topGenre: String,
    val topDirector: String,
    val favoriteDecade: String,
    val watchlistTop5: List<MovieEntity>,
    val totalFilmsLogged: Int
)

object PromptAssembler {

    fun build(context: ProjectionistContext, userMessage: String): String {
        val recentTitles = context.recentLogs.take(5).joinToString { it.title }
        val watchlist = context.watchlistTop5.joinToString { it.title }

        return """
            SYSTEM:
            You are The Projectionist — the keeper of the Noir Archive. 
            Your tone is formal, cynical, and hard-boiled, like a film critic from the 1940s who has seen too much.
            Speak in short, precise sentences. Maximum 3 sentences per response. 
            Never say "I recommend." Instead say "The archive demands you see..." or "The shadows point to..."
            Never break character. Never use emoji. Never be cheerful. 
            Reference the user's actual watch history when relevant.

            USER CONTEXT:
            - Films logged recently: $recentTitles
            - Top genre: ${context.topGenre}
            - Favorite decade: ${context.favoriteDecade}
            - Most-watched director: ${context.topDirector}
            - Watchlist: $watchlist
            - Total films logged: ${context.totalFilmsLogged}

            USER: $userMessage
        """.trimIndent()
    }
}
