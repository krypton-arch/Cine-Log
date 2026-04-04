# CineLog — The Noir Archive

> *"Cinema is a mirror that can focus the shadows of the past into the light of the present."*

CineLog is a premium Android film diary for people who treat watching movies like an act of preservation. No algorithms. No social pressure. Just you, your archive, and a projector humming in the dark.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-DI-orange?style=flat)
![Room](https://img.shields.io/badge/Room-Local%20DB-green?style=flat)
![Gemini](https://img.shields.io/badge/Gemini%202.5%20Flash-AI-blue?style=flat&logo=google)
![TMDB](https://img.shields.io/badge/TMDB-API-01B4E4?style=flat)
![API](https://img.shields.io/badge/Min%20SDK-API%2024-brightgreen?style=flat)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat)

---

## Screenshots

<p align="center">
  <img src="screenshots/home.jpg" width="30%" />
  <img src="screenshots/booth.jpg" width="30%" />
  <img src="screenshots/insights.jpg" width="30%" />
</p>

<details>
<summary>See the full archive →</summary>
<br>
<p align="center">
  <img src="screenshots/profile.jpg" width="22%" />
  <img src="screenshots/diary.jpg" width="22%" />
  <img src="screenshots/badges.jpg" width="22%" />
  <img src="screenshots/challenges.jpg" width="22%" />
  <img src="screenshots/library.jpg" width="22%" />
</p>
</details>

---

## Why I Built This

I wanted a film journal that felt like it belonged on a mahogany desk next to a reel-to-reel projector — not a growth-hacked social feed. CineLog is where I went deep on Android architecture (Hilt, Room, Compose) and asked a harder question: what does AI integration actually feel like when it knows your archive, your taste, your favorite decade, and the last ten films you loved?

The answer is the Projectionist's Booth. It's not a chatbot. It's a cinema personality.

---

## What's Inside

### 🎞 The Diary

Log films with star ratings, written reviews, and full TMDB metadata pulled on search. A swipeable month-view calendar built on `HorizontalPager` lets you navigate your cinematic history the way you'd flip through a physical journal — forward and back, month by month. Days with logged films are highlighted; tap one to see exactly what you watched and what you thought.

Review text is fully expandable with a SHOW MORE / SHOW LESS toggle so long entries don't bury the rest of the card. Logs can be edited or deleted inline from the day sheet. All data lives in Room — no cloud sync required, no account needed.

Month filtering uses an exclusive upper bound (`plusMonths(1).atDay(1).atStartOfDay()`) ensuring a film logged at 11:59 PM on the last day of the month is never miscounted.

### 🎬 The Library

TMDB-backed full-text search with poster art, release year, runtime, genre tags, director, and overview. Every film you find can be added to your personal watchlist with a single tap. The watchlist tracks status and is one of the primary inputs that drives Projectionist context.

The detail screen gives you everything TMDB has: cast, backdrop, tagline, and a direct path to either log the film immediately or add it to your queue. Offline-first — once metadata is cached in Room it never hits the network again for the same film.

### 🏛 The Archive (Profile)

Your cinematic identity made visible. Built from live Room queries — every stat reflects exactly what you've logged.

- **Level & XP** — Five tiers: Cinephile (0 XP), Critic (500), Auteur (1000), Director (2000), Legend (4000). Earned by logging films (+10 XP), writing reviews (+20), completing watchlist entries (+15), maintaining daily streaks (+5), and unlocking badges (+50 each). The progress bar shows level-relative advancement, not raw XP against a global ceiling.
- **Day Streak** — Consecutive logging days tracked with correct yesterday / today / reset arithmetic. Streak label escalates from New Spectator → Cinema Enthusiast → Cinema Devotee → Cinema Buff → Cinema Legend (30+ days).
- **Genre Passport** — A ranked breakdown of your top 5 genres by mention count. A multi-genre film contributes to each genre independently. Displayed as a segmented colour bar with descending opacity tones plus a labelled ranked list, individual progress bars, and a raw count-per-genre stat. Empty state prompts you to log your first few films.
- **Archive Insights** — A Gemini-generated 1–2 sentence cryptic cinematic observation about your archive, assembled from your real data. Refreshes automatically when: the cache is over 24 hours old, a new film is logged after the last generation, or your top genre / director / recent film list changes. Context key tracks exact archive state — no unnecessary API calls.
- **Badges** — Eight collectible badges in a 3×2 showcase grid, each tappable to reveal a detail sheet with the unlock date. Locked badges are visible but dimmed.
- **Monthly Challenge** — A rotating monthly objective with a live progress bar and XP reward on completion.
- **Bottom Stats** — Favorite decade and top director, computed from your full log history.

### 🎙 The Projectionist's Booth

An archive-aware AI guide powered by Gemini 2.5 Flash. Before it responds to anything, it receives a structured context object assembled from your live Room data: your 10 most recently logged films, your top 5 watchlist entries, your dominant genre, your favorite decade, your top director, and your total archive size.

Ask it for a mood recommendation, a double feature, something dark and gorgeous for tonight, or a film that sounds like a particular piece of music. It has opinions. They are informed by what you've actually watched.

Built with OkHttp directly (not Retrofit) for fine-grained control over timeouts and streaming. The `PromptAssembler` object constructs the full prompt from a `ProjectionistContext` data class — context assembly is fully separated from the network call. The Booth UI renders responses in a monospace typeface inside a glass-card container, with a typing indicator and full scroll history for the session.

### 🏆 Milestones & Challenges

A complete gamification engine with no fake inflation.

**Badges**

| Badge | Condition |
|---|---|
| First Frame | Log your first film |
| Horror Fiend | Log 10 horror films |
| Old Soul | Log 10 films from before 1980 |
| Binge King | Log 5 films within any 7-day window |
| Marathon | Accumulate 1,000 minutes watched |
| The Critic | Write 10 reviews |
| Week Warrior | Maintain a 7-day logging streak |
| Centurion | Log 100 films |

Badge logic is idempotent — `checkBadges()` can be called multiple times without double-awarding. Each unlock awards +50 XP and is persisted with an unlock timestamp. The `old_soul` and decade filters use `.take(4)` on `releaseYear` to handle both `"1975"` and `"1975-10-22"` TMDB date formats safely.

**Monthly Challenges** rotate each calendar month via `MonthlyChallengeEngine`, a pure stateless `object` that derives the current challenge from the `YearMonth` alone — no hardcoded dates, no manual configuration. Progress is evaluated live against your Room logs and capped at the target count to prevent overflow in the display.

---

## Architecture

Clean MVVM with strict 3-layer separation. No shortcuts, no concerns leaking between layers.

```

app/
├── data/
│   ├── local/          # Room entities, DAOs, CineLogDatabase
│   ├── remote/         # TMDB + Gemini OkHttp clients
│   └── repository/     # Single source of truth per domain
├── domain/
│   ├── GamificationManager.kt      # XP, streaks, badge unlock logic
│   ├── MonthlyChallengeEngine.kt   # Pure stateless challenge evaluation
│   └── PromptAssembler.kt          # Gemini context construction
├── ui/
│   ├── screens/        # One folder per screen, ViewModel co-located
│   ├── navigation/     # NavHost + type-safe route definitions
│   └── theme/          # Colour, typography, shape tokens
├── di/                 # Hilt modules
├── utils/              # rethrowIfCancellation, extensions
└── worker/             # Background WorkManager tasks

```


**Data flow:** Room `Flow<>` → Repository → `combine()` in ViewModel → `UiState` data class → Composable via `collectAsState()`. No business logic in Composables. No direct DAO access outside repositories.

**Dependency injection:** Hilt throughout. All repositories and `GamificationManager` are `@Singleton`. ViewModels use `@HiltViewModel` with constructor injection.

**Concurrency:** Kotlin Coroutines with structured concurrency. `rethrowIfCancellation()` ensures `CancellationException` always propagates correctly, preventing coroutine leaks on screen exit. The `insightRefreshInFlight` flag prevents duplicate Gemini calls from concurrent `combine` emissions.

**AI relay:** The Gemini API key never ships in the APK. All Gemini traffic routes through a lightweight Node.js relay deployed on Render, provisioned declaratively via `render.yaml`. The relay injects the server-side key — the client sends only the assembled prompt.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material Design 3 |
| Architecture | MVVM + Repository Pattern |
| Dependency Injection | Hilt (Dagger) |
| Local Database | Room |
| Networking | Retrofit (TMDB) + OkHttp (Gemini relay) |
| Image Loading | Coil |
| AI | Gemini 2.5 Flash via server relay |
| Movie Metadata | TMDB API v3 |
| Testing | JUnit 4, MockK, Kotlin Coroutines Test |
| Backend Relay | Node.js on Render |
| Typography | Playfair Display (headings), system default (body) |

---

## Design Language

Deep black surfaces. Warm IMDb gold (`#F5C518`) and silver smoke used as the single `primaryContainer` accent token — never applied arbitrarily. Playfair Display for dramatic headers and large stat figures. System Inter for readable data labels and body copy.

Every surface follows a consistent treatment: `glassSurface` for cards, `NoirBackdrop` for screen backgrounds, `regalDivider` for section breaks. All uppercase section labels use 2sp letter spacing at 9–10sp. Spaced-out, editorial, unhurried.

Animations are treated as a system in `Animations.kt` — consistent durations and easing curves across all transitions. Bounce-click feedback on interactive elements. The Projectionist's Booth has a typing indicator that feels like the projector threading the next reel before it speaks.

Every screen is meant to feel like a cinema that exists only for you.

<details>
<summary>Contributor Notes</summary>
<br>

- Preserve the noir tone and gold-accented visual language at all times.
- Use the established typography system (`PlayfairDisplayFont`, spaced uppercase labels) — no ad hoc font choices.
- All new cards use `glassSurface` + `1.dp` border at `onSurfaceVariant` with `0.1f` alpha.
- `primaryContainer` is the accent colour — use it for highlights, never as a fill.
- Avoid generic dark UI patterns when extending screens.
- If a screen doesn't feel like it belongs in a private cinema, it doesn't belong in CineLog.

</details>

---

## Download

### [CineLog-v2.6-ProfileRefresh.apk](https://github.com/krypton-arch/Cine-Log/raw/main/releases/CineLog-v2.6-ProfileRefresh.apk)

1. Download to your Android device (Android 7.0+, API 24).
2. Allow installs from unknown sources if prompted.
3. This build includes all updates currently on `main` — archive insight refresh, corrected month boundary filtering, genre passport redesign, and monthly challenge fixes.
4. Signed with the Android debug keystore, not a production release key.
5. The Projectionist's Booth uses the hosted Gemini relay — no client-side API key required. The relay runs on Render's free tier and may have a cold-start delay of up to 30 seconds on the first request after inactivity.
6. Your archive data stays entirely on-device. Back it up before switching phones — there is no cloud sync.

---

## Build From Source

### Prerequisites

- Android Studio Ladybug or newer
- A TMDB API key — [get one free at themoviedb.org](https://www.themoviedb.org/settings/api)
- A deployed Gemini relay URL (see below)

### App Setup

Add to `local.properties` at the project root:

```properties
TMDB_API_KEY=your_tmdb_key_here
GEMINI_PROXY_BASE_URL=https://your-relay.example.com
```

Then build:

```bash
./gradlew assembleDebug
```

### Deploy the Relay

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/krypton-arch/Cine-Log)

The included `render.yaml` provisions the `gemini-relay` Node.js service and prompts for `GEMINI_API_KEY` as an environment secret on first deploy. Auto-deploys are disabled by default — intentional, to avoid surprise rebuilds. Once deployed, copy the service URL into `local.properties` as `GEMINI_PROXY_BASE_URL`.

### Run Tests

```bash
./gradlew test
```

Unit tests cover `ProfileViewModel` insight refresh and context-key invalidation logic using MockK and Kotlin Coroutines Test. `MonthlyChallengeEngine` and `GamificationManager` test coverage is in progress.

---

*CineLog — An archive for those who truly see.*
```
