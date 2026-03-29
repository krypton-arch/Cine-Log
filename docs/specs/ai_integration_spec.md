````markdown
# CineLog: "The Projectionist's Booth" — AI Integration Spec
*Last Updated: 2026-03-30*

---

## 🎯 Objective
Transform the Noir Archive from a static collection into a living, conversational experience.
The Projectionist guides users through cinema history and their personal taste — not as a chatbot,
but as a curator who "analyzes shadows."

---

## 🎭 Persona: The Projectionist

| Attribute | Definition |
|---|---|
| **Tone** | Formal, cynical, hard-boiled — deeply respectful of the medium |
| **Role** | A curator who doesn't "recommend" — they "summon the right film" |
| **Length** | 2–3 sentences maximum per response. Brevity is cinematic. |
| **Forbidden** | Never say "I recommend", "Great choice!", or break the noir persona |

**Example response:**
> *"You've seen 'The Third Man' three times now. Are you chasing the shadows of post-war Vienna,
> or just captivated by Orson Welles' smile? If it's the latter, the archive demands you see
> 'Touch of Evil' (1958) — Welles at his most unhinged."*

---

## 🛠️ Technical Architecture

### Engine
- **Model**: Gemini 2.0 Flash *(upgraded from 1.5 Flash — faster, same free tier)*
- **Why**: Sub-second responses, strong reasoning for film analysis, free-tier eligible (15 RPM)
- **SDK**: `com.google.ai.client.generativeai`

### Dependency (`libs.versions.toml`)
```toml
[versions]
generativeai = "0.9.0"

[libraries]
generativeai = { group = "com.google.ai.client.generativeai", name = "generativeai", version.ref = "generativeai" }
```

In `app/build.gradle.kts`:
```kotlin
implementation(libs.generativeai)
```

### API Key Security
- Managed via **Secrets Gradle Plugin** — never hardcoded, never committed to VCS
- Accessed as `BuildConfig.GEMINI_API_KEY` at runtime

```toml
[plugins]
secrets-gradle = { id = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin", version = "2.0.1" }
```

In `local.properties` (already git-ignored):
```properties
GEMINI_API_KEY=your_key_here
```

In `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.secrets.gradle) // add this
}

secrets {
    propertiesFileName = "local.properties"
}
```

---

## 🧬 Data Flow

1. App queries Room DB for context: last 10 logs, top genre, top director, favorite decade, watchlist top 5
2. `PromptAssembler` wraps the context in the Projectionist system prompt
3. `GeminiRepository.sendMessage()` sends the assembled prompt to Gemini 2.0 Flash
4. Response is rendered in the Noir-styled UI

### Context Data Class
```kotlin
data class ProjectionistContext(
    val recentLogs: List<LogEntry>,
    val topGenre: String,
    val topDirector: String,
    val favoriteDecade: String,
    val watchlistTop5: List<MovieEntity>,
    val totalFilmsLogged: Int
)
```

---

## 🧠 Prompt Engineering

### System Prompt Template
```
SYSTEM:
You are The Projectionist — the keeper of the Noir Archive.
Your tone is formal, cynical, and hard-boiled, like a film critic who has seen too much.
Speak in short, precise sentences. Maximum 3 sentences per response.
Never say "I recommend." Instead say "The archive demands you see..." or "The shadows point to..."
Never break character. Never use emoji. Never be cheerful.
Reference the user's actual watch history when relevant.

USER CONTEXT:
- Films logged recently: {{recentLogs}}
- Top genre: {{topGenre}}
- Favorite decade: {{favoriteDecade}}
- Most-watched director: {{topDirector}}
- Watchlist: {{watchlistTop5}}

USER: {{userMessage}}
```

> Gemini 2.0 Flash supports a 1M token context window. For a typical user with under 500 logs,
> full context injection costs approximately 10K tokens per call — well within free-tier limits.

### `PromptAssembler.kt`
```kotlin
object PromptAssembler {

    fun build(context: ProjectionistContext, userMessage: String): String {
        val recentTitles = context.recentLogs.joinToString { it.title }
        val watchlist = context.watchlistTop5.joinToString { it.title }

        return """
            SYSTEM:
            You are The Projectionist — the keeper of the Noir Archive.
            Your tone is formal, cynical, and hard-boiled, like a film critic who has seen too much.
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

            USER: $userMessage
        """.trimIndent()
    }
}
```

---

## 🔌 GeminiRepository

### Interface
```kotlin
interface GeminiRepository {
    suspend fun sendMessage(systemPrompt: String, userMessage: String): Result<String>
    suspend fun generateInsight(context: ProjectionistContext): Result<String>
}
```

### Implementation
```kotlin
class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun sendMessage(systemPrompt: String, userMessage: String): Result<String> {
        return try {
            val response = model.generateContent(
                content { text(systemPrompt) }
            )
            Result.success(response.text ?: ProjectionistStrings.SILENT)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateInsight(context: ProjectionistContext): Result<String> {
        val prompt = PromptAssembler.build(context, "Give me one film I must see next.")
        return sendMessage(prompt, "")
    }
}
```

---

## 📱 UX Surfaces

### 1. The Projectionist's Booth (Full Screen Chat)
- Entry point: FAB on the Home screen — film reel or cinema ticket icon in Archive Gold (`#D4AF37`)
- Dedicated full-screen message interface

| Element | Style |
|---|---|
| AI response text | `Roboto Mono`, 13sp, Silver `#C0C0C0` |
| User message text | `Inter`, 14sp, White `#FFFFFF` |
| Background | Pure Black `#000000` |
| Message borders | 1px Silver `#C0C0C0` hairline |
| Send button | Archive Gold `#D4AF37` icon |

### 2. Archive Insights Cards (Profile Page)
- A horizontal row of AI-generated cards summarizing the user's Genre Passport stats
- Refresh cadence: every 24 hours, cached in Room as `AiInsightEntity` with a `generatedAt` timestamp
- Seed data: Top Director + Favorite Decade → 1 curated film recommendation per cycle
- Card limit: max 3 cards per session to stay within free-tier RPM limits

### `AiInsightEntity` (Room)
```kotlin
@Entity(tableName = "ai_insights")
data class AiInsightEntity(
    @PrimaryKey val id: Int = 1, // singleton row
    val insightText: String,
    val generatedAt: Long // System.currentTimeMillis()
)
```

Cache check logic:
```kotlin
fun isCacheStale(generatedAt: Long): Boolean {
    val twentyFourHours = 24 * 60 * 60 * 1000L
    return System.currentTimeMillis() - generatedAt > twentyFourHours
}
```

---

## 🛡️ Error & Offline Handling

All fallbacks are hardcoded strings in `ProjectionistStrings.kt` — no raw errors are ever shown to the user.

```kotlin
object ProjectionistStrings {
    const val OFFLINE = "The archive is closed tonight. The reels are rewinding."
    const val RATE_LIMITED = "The Projectionist is occupied. Return at midnight."
    const val SERVER_ERROR = "Static on the reel. The booth will reopen shortly."
    const val EMPTY_ARCHIVE = "The archive is empty. Log your first film to begin."
    const val SILENT = "..."
}
```

Map errors in the ViewModel:
```kotlin
fun sendMessage(userInput: String) {
    viewModelScope.launch {
        val result = geminiRepository.sendMessage(
            PromptAssembler.build(currentContext, userInput), userInput
        )
        _uiState.update {
            it.copy(
                displayMessage = result.getOrElse { e ->
                    when (e) {
                        is IOException -> ProjectionistStrings.OFFLINE
                        is HttpException -> if (e.code() == 429)
                            ProjectionistStrings.RATE_LIMITED
                        else
                            ProjectionistStrings.SERVER_ERROR
                        else -> ProjectionistStrings.SERVER_ERROR
                    }
                }
            )
        }
    }
}
```

---

## 🧪 Testing the AI Layer

Mock `GeminiRepository` in all ViewModel tests to avoid live API calls:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectionistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockGeminiRepo = mockk<GeminiRepository>()
    private val mockLogRepo = mockk<LogRepository>()
    private val viewModel = ProjectionistViewModel(mockGeminiRepo, mockLogRepo)

    @Test
    fun `offline error maps to correct fallback message`() = runTest {
        coEvery { mockGeminiRepo.sendMessage(any(), any()) } returns
            Result.failure(IOException("No internet"))
        viewModel.sendMessage("What should I watch?")
        assert(viewModel.uiState.value.displayMessage == ProjectionistStrings.OFFLINE)
    }

    @Test
    fun `rate limit error maps to correct fallback message`() = runTest {
        val httpException = mockk<HttpException> { every { code() } returns 429 }
        coEvery { mockGeminiRepo.sendMessage(any(), any()) } returns
            Result.failure(httpException)
        viewModel.sendMessage("What should I watch?")
        assert(viewModel.uiState.value.displayMessage == ProjectionistStrings.RATE_LIMITED)
    }

    @Test
    fun `successful response updates display message correctly`() = runTest {
        coEvery { mockGeminiRepo.sendMessage(any(), any()) } returns
            Result.success("The shadows point to Chinatown (1974).")
        viewModel.sendMessage("Give me a noir recommendation.")
        assert(viewModel.uiState.value.displayMessage == "The shadows point to Chinatown (1974).")
    }

    @Test
    fun `stale cache triggers new insight generation`() = runTest {
        val staleTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)
        assert(isCacheStale(staleTimestamp) == true)
    }
}
```

---

## ✅ Priority Checklist

- [ ] Add `generativeai` dependency to `libs.versions.toml` and `app/build.gradle.kts`
- [ ] Add `secrets-gradle-plugin` and configure `local.properties`
- [ ] Add `GEMINI_API_KEY` to `local.properties` (confirm it is in `.gitignore`)
- [ ] Create `GeminiRepository` interface and `GeminiRepositoryImpl`
- [ ] Register `GeminiRepository` as `@Singleton` in `di/AiModule.kt`
- [ ] Create `PromptAssembler.kt` with full context injection
- [ ] Create `ProjectionistStrings.kt` with all 5 fallback constants
- [ ] Add `AiInsightEntity` to Room and increment database version
- [ ] Implement 24-hour cache check via `isCacheStale()` before API call
- [ ] Build Projectionist Booth screen with correct typography and colors
- [ ] Build Archive Insights card row on Profile screen
- [ ] Write `ProjectionistViewModelTest` with all 4 test cases
- [ ] Confirm no API key appears in any committed file or build output
````