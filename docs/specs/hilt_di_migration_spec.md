
---

## 🧪 Unit Testing Strategy (`GamificationManager`)

### Test Dispatcher Rule

Create this once in `test/` and reuse across all test classes:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}
```

### `GamificationManagerTest.kt`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class GamificationManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockLogRepo = mockk<LogRepository>()
    private val mockGamificationRepo = mockk<GamificationRepository>()
    private val manager = GamificationManager(mockGamificationRepo, mockLogRepo)

    // ── Badge Logic ──────────────────────────────────────────────────────────

    @Test
    fun `when 10 films pre-1980 logged, Old Soul badge should unlock`() = runTest {
        coEvery { mockLogRepo.getLogsBeforeYear(1980) } returns List(10) { mockk() }
        coEvery { mockGamificationRepo.unlockBadge(any()) } just Runs
        manager.evaluateBadges()
        coVerify { mockGamificationRepo.unlockBadge("old_soul") }
    }

    @Test
    fun `badge evaluation is idempotent — unlocked badge is not re-triggered`() = runTest {
        coEvery { mockGamificationRepo.isBadgeUnlocked("old_soul") } returns true
        manager.evaluateBadges()
        coVerify(exactly = 0) { mockGamificationRepo.unlockBadge("old_soul") }
    }

    // ── Streak Logic ─────────────────────────────────────────────────────────

    @Test
    fun `streak resets to 1 when a day is missed`() = runTest {
        coEvery { mockLogRepo.getLastTwoLogDates() } returns listOf(
            LocalDate.now(),
            LocalDate.now().minusDays(2) // gap of 2 days = streak break
        )
        val streak = manager.calculateStreak()
        assert(streak == 1)
    }

    @Test
    fun `streak increments correctly on consecutive daily logs`() = runTest {
        coEvery { mockLogRepo.getLastTwoLogDates() } returns listOf(
            LocalDate.now(),
            LocalDate.now().minusDays(1) // consecutive = valid streak
        )
        val streak = manager.calculateStreak()
        assert(streak > 1)
    }

    // ── XP & Leveling ────────────────────────────────────────────────────────

    @Test
    fun `XP milestone triggers at every 5 levels`() = runTest {
        coEvery { mockGamificationRepo.getUserXp() } returns 500 // = level 5
        val milestone = manager.checkMilestone()
        assert(milestone == true)
    }

    @Test
    fun `XP milestone does not trigger at non-milestone level`() = runTest {
        coEvery { mockGamificationRepo.getUserXp() } returns 300 // = level 3
        val milestone = manager.checkMilestone()
        assert(milestone == false)
    }

    // ── Genre & Challenge Logic ───────────────────────────────────────────────

    @Test
    fun `genre aggregation returns empty map for new user with no logs`() = runTest {
        coEvery { mockLogRepo.getAllLogs() } returns emptyList()
        val genres = manager.aggregateGenres()
        assert(genres.isEmpty())
    }

    @Test
    fun `challenge progress increments correctly on genre match`() = runTest {
        coEvery { mockLogRepo.getLogsByGenre("Indie") } returns List(3) { mockk() }
        coEvery { mockGamificationRepo.updateChallengeProgress(any(), any()) } just Runs
        manager.evaluateChallenges()
        coVerify { mockGamificationRepo.updateChallengeProgress("indie_challenge", 3) }
    }
}
```

---

## ✅ Priority Checklist

- [ ] Add Hilt entries to `libs.versions.toml` (version, libraries, plugin)
- [ ] Apply `alias(libs.plugins.hilt)` in app-level `build.gradle.kts`
- [ ] Apply `alias(libs.plugins.hilt) apply false` in top-level `build.gradle.kts`
- [ ] Create `CineLogApplication` with `@HiltAndroidApp`
- [ ] Register `CineLogApplication` in `AndroidManifest.xml`
- [ ] Add `@AndroidEntryPoint` to `MainActivity`
- [ ] Annotate all ViewModels with `@HiltViewModel` + `@Inject constructor`
- [ ] Replace all `viewModel(factory = ...)` calls with `hiltViewModel()`
- [ ] Create `di/DatabaseModule.kt` with all DAO providers as `@Singleton`
- [ ] Create `di/RepositoryModule.kt` with all repositories as `@Singleton`
- [ ] Create `di/AiModule.kt` for `GeminiRepository`
- [ ] Verify full singleton chain for `GamificationManager`
- [ ] Create `MainDispatcherRule.kt` in `test/` directory
- [ ] Write all 8 `GamificationManagerTest` cases with `runTest` + `coEvery`
- [ ] Confirm clean build with no KSP/Hilt annotation processing errors