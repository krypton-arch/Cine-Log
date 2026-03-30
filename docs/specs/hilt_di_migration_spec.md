```markdown
# CineLog: Hilt DI & Unit Testing Architecture Spec
*Last Updated: 2026-03-30*

---

## 🎯 Objectives
- **Standardize DI**: Replace all manual `ViewModelProvider.Factory` implementations with Dagger-Hilt.
- **Isolate Logic**: Decouple the `GamificationManager` from concrete repositories for reliable unit testing.
- **Zero Boilerplate**: Automate the dependency graph for all 4 primary screens.

---

## ⚙️ Hilt Infrastructure

### 1. Dependencies (`libs.versions.toml`)

> ✅ KSP is already configured (`2.2.10-2.0.2`) and active for Room compilation on line 65.
> Hilt piggybacks on the same `ksp(...)` declaration — no new plugin setup required.

Add only the Hilt-specific entries to your existing catalog:

```toml
[versions]
# Do NOT add a new ksp version — it already exists
hilt = "2.56"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### 2. `app/build.gradle.kts`

Add Hilt alongside the existing Room KSP setup:

```kotlin
plugins {
    id("com.google.devtools.ksp") // ✅ already on line 6 — do not duplicate
    alias(libs.plugins.hilt)      // 👈 add this
}

dependencies {
    // Room — already present
    ksp(libs.androidx.room.compiler)        // ✅ already on line 65

    // Hilt — add these
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)                 // 👈 same pattern as Room
    implementation(libs.hilt.navigation.compose)

    // Testing
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}
```

### 3. Top-Level `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.hilt) apply false
}
```

---

## 🚀 Application Entry Point

Create `CineLogApplication.kt` if it doesn't exist:

```kotlin
@HiltAndroidApp
class CineLogApplication : Application()
```

Register in `AndroidManifest.xml`:

```xml
<application
    android:name=".CineLogApplication"
    ... />
```

---

## 📦 Hilt Modules

Place all modules in a `di/` package: `com.yourpackage.cinelog.di`

### `DatabaseModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideLogDao(db: AppDatabase): LogDao = db.logDao()

    @Provides
    @Singleton
    fun provideMovieDao(db: AppDatabase): MovieDao = db.movieDao()

    @Provides
    @Singleton
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    @Singleton
    fun provideBadgeDao(db: AppDatabase): BadgeDao = db.badgeDao()

    @Provides
    @Singleton
    fun provideChallengeDao(db: AppDatabase): ChallengeDao = db.challengeDao()
}
```

### `RepositoryModule.kt`

> ⚠️ All repositories **must** be `@Singleton`. Without it, Hilt creates a new instance per
> injection site, breaking the single source of truth guarantee and causing divergent DB states.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLogRepository(logDao: LogDao): LogRepository =
        LogRepositoryImpl(logDao)

    @Provides
    @Singleton
    fun provideMovieRepository(
        movieDao: MovieDao,
        @ApplicationContext context: Context
    ): MovieRepository = MovieRepositoryImpl(movieDao, context)

    @Provides
    @Singleton
    fun provideGamificationRepository(
        badgeDao: BadgeDao,
        challengeDao: ChallengeDao,
        userProfileDao: UserProfileDao
    ): GamificationRepository =
        GamificationRepositoryImpl(badgeDao, challengeDao, userProfileDao)

    @Provides
    @Singleton
    fun provideGamificationManager(
        gamificationRepository: GamificationRepository,
        logRepository: LogRepository
    ): GamificationManager =
        GamificationManager(gamificationRepository, logRepository)
}
```

### `AiModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGeminiRepository(): GeminiRepository =
        GeminiRepositoryImpl(apiKey = BuildConfig.GEMINI_API_KEY)
}
```

---

## 🗺️ Migration Path (Step-by-Step)

### Step 1 — Update Gradle Files
Apply changes to `libs.versions.toml` and `app/build.gradle.kts` as defined above.
Sync the project and confirm no KSP version conflicts.

### Step 2 — Annotate Application & Activity
```kotlin
// CineLogApplication.kt
@HiltAndroidApp
class CineLogApplication : Application()

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() { ... }
```

### Step 3 — Refactor ViewModels

**Before:**
```kotlin
class HomeViewModel(
    private val logRepository: LogRepository,
    private val gamificationManager: GamificationManager
) : ViewModel() {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(applicationContext())
                HomeViewModel(
                    LogRepositoryImpl(db.logDao()),
                    GamificationManager(...)
                )
            }
        }
    }
}
```

**After:**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val gamificationManager: GamificationManager
) : ViewModel()
```

### Step 4 — Update Compose Screen Calls

**Before:**
```kotlin
val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
```

**After:**
```kotlin
val viewModel: HomeViewModel = hiltViewModel()
```

Apply this change to all 4 primary screens.

### Step 5 — Create `di/` Package Structure

```
app/src/main/java/com/yourpackage/cinelog/
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── AiModule.kt
├── data/
├── ui/
└── ...
```

### Step 6 — Verify Singleton Chain

Before running, confirm every node in the `GamificationManager` dependency chain is `@Singleton`:

```
GamificationManager        (@Singleton ✅)
├── GamificationRepository (@Singleton ✅)
│   ├── BadgeDao           (@Singleton ✅)
│   ├── ChallengeDao       (@Singleton ✅)
│   └── UserProfileDao     (@Singleton ✅)
└── LogRepository          (@Singleton ✅)
    └── LogDao             (@Singleton ✅)
```

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
```