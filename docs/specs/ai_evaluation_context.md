# CineLog: AI Evaluation Context & Specification

## 🏛️ System Identity
- **Project Name**: CineLog (The Noir Archive)
- **Primary Goal**: High-end, moody film preservation and logging.
- **Tone**: Deeply respectful, archival, and cinematic.

## ⚙️ Core Architecture (MVVM + Clean Repository)
CineLog is architected for maximum data integrity and offline-first performance, separating UI from business logic via:
1.  **UI Layer**: Jetpack Compose (Material 3).
2.  **View Model Layer**: State aggregation (Combine flows for logs/stats).
3.  **Repository Layer**: Single source of truth (Room DB) with TMDB API fallback.
4.  **Data Layer**: Room Persistence with complex relationship mappings.

## 🏛️ Data Persistence (Room v5)
The database has reached version 5 to support the "Archival Restoration" features.

### Entity Schemas
- **`MovieEntity`**: `id`, `title`, `genres` (CSV), `releaseYear`, `runtime`, `director` (extracted from credits).
- **`LogEntry`**: `id`, `movieId`, `watchDate`, `rating`, `reviewText`.
- **`UserProfile`**: `userId`, `xp`, `level`, `currentStreak`.
- **`Badge`**: `badgeId`, `name`, `description`, `isUnlocked`.
- **`Challenge`**: `challengeId`, `title`, `targetCount`, `currentCount`.

### Resilience Patterns
- **Self-Healing Seeds**: Uses an `onOpen` database callback to verify that the 8 core badges and 4 initial challenges exist. If missing (e.g., after migration), they are force-seeded to ensure UI consistency.

## 🧠 Business Logic & Gamification

### The Genre Passport (Manual Canvas Visualization)
The `GenrePassportCard` uses a custom `DrawScope` on a donut chart.
- **Logic**: Aggregates all genres from the user's `logs`.
- **Calculation**: Percentage-based arc lengths mapping 0-100% to 360 degrees.
- **Visuals**: Primary genres get full gold strokes; secondary genres get silver/grey.

### Gamification Engine (`GamificationManager`)
A dedicated controller handles logic for:
1.  **XP/Leveling**: 100 XP per film log, with milestones every 5 levels.
2.  **Badge Conditions**: Scans entire Room DB for criteria (e.g., *Old Soul* = 10+ films pre-1980).
3.  **Streaks**: Logic to check daily consecutive entries.
4.  **Challenge Progress**: Live scanners that count genre-specific logs (e.g., *Indie Films* challenge).

## 🌑 Aesthetic Guidelines (Noir Branding)
- **Background**: `#000000` (Pure Black).
- **Primary Accent**: `#D4AF37` (Archive Gold).
- **Surface Accent**: Glassmorphism with `#1A1A1A` opacity.
- **Typography**:
    - **Headers**: `Playfair Display` (Serif).
    - **Information**: `Inter` (Sans-Serif).
    - **AI Conversation**: `Roboto Mono` (Typewriter).

## 🔮 Future Capabilities (The Projectionist)
- **Model**: Gemini 1.5 Flash (via Google AI SDK).
- **Features**: Deep-context "Critic's Notes" and "Projectionist Booth" conversational cinephile chat.

---
*Created for AI Evaluation | 2026-03-30*
