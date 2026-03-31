package com.exmple.cinelog.ui.screens.watchlist

import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.local.entity.Priority
import com.exmple.cinelog.data.local.entity.WatchlistEntry
import com.exmple.cinelog.data.remote.MovieApiService
import com.exmple.cinelog.data.remote.RemoteMovie
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<WatchlistRepository>()
    private val apiService = mockk<MovieApiService>()

    @Test
    fun `init collects watchlist items from repository`() = runTest {
        val movie = MovieEntity(
            movieId = 42,
            title = "The Red Shoes",
            posterPath = "/poster.jpg",
            releaseYear = "1948",
            genres = "Drama",
            runtime = 135,
            director = "Powell and Pressburger",
            overview = "A dancer is torn between art and love."
        )
        val watchlistItem = WatchlistItemWithMovie(
            watchlistEntry = WatchlistEntry(
                id = 1,
                movieId = 42,
                priority = Priority.CASUAL,
                addedDate = 123L,
                notes = null
            ),
            movie = movie
        )

        every { repository.getAllWatchlistItems() } returns flowOf(listOf(watchlistItem))

        val viewModel = WatchlistViewModel(repository, apiService)
        advanceUntilIdle()

        assertEquals(listOf(watchlistItem), viewModel.watchlist.value)
    }

    @Test
    fun `addToWatchlist maps remote movie into movie entity`() = runTest {
        every { repository.getAllWatchlistItems() } returns flowOf(emptyList())
        coEvery { repository.addToWatchlist(any(), any(), any()) } returns Unit

        val viewModel = WatchlistViewModel(repository, apiService)
        val remoteMovie = RemoteMovie(
            id = 7,
            title = "Possession",
            poster_path = "/possession.jpg",
            release_date = "1981-05-27",
            overview = "A marriage unravels into hysteria and horror.",
            vote_average = 7.3
        )

        viewModel.addToWatchlist(remoteMovie)
        advanceUntilIdle()

        coVerify {
            repository.addToWatchlist(
                match { movie ->
                    movie.movieId == 7 &&
                        movie.title == "Possession" &&
                        movie.posterPath == "/possession.jpg" &&
                        movie.releaseYear == "1981" &&
                        movie.genres.isEmpty() &&
                        movie.runtime == 0 &&
                        movie.director == null &&
                        movie.overview == "A marriage unravels into hysteria and horror."
                },
                Priority.CASUAL,
                null
            )
        }
    }
}
