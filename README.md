# CineLog: The Noir Archive

> "Cinema is a mirror that can focus the shadows of the past into the light of the present."

CineLog is a premium Android film diary and personal movie archive for people who treat watching movies like an act of preservation. It combines a noir-inspired visual identity with practical tools for logging, curating, and reflecting on your cinematic life.

![App Icon](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

---

## What CineLog Includes

### The Diary
- Log films with ratings and written reviews.
- Preserve key metadata like director, genre, runtime, and release decade.
- Build a personal record that feels more archival than disposable.

### The Vault
- Browse TMDB-backed movie data.
- Save titles to a personal watchlist.
- Dive into context before you decide what to log next.

### The Passport
- Track total films logged, total watch time, and average ratings.
- Surface top genres, favorite decades, and most-watched directors.
- Turn your habits into a visible cinematic identity.

### The Projectionist's Booth
- Chat with an archive-aware AI guide powered by Gemini 2.5 Flash.
- Get longer-form movie recommendations, comparisons, and discussion.
- Use quick conversation cues for moods, double features, and tailored picks.
- Explore a redesigned booth UI with a cinema-lounge hero panel, richer message bubbles, and faster entry points into conversation.

### Subtle Milestones
- Unlock badges like First Frame, Old Soul, Horror Fiend, and Week Warrior.
- Track gentle challenges without turning the app into a noisy leaderboard.

---

## Design Direction

CineLog follows a luxury noir visual system:
- Deep black surfaces that let posters and metadata emerge from the dark.
- Warm gold and silver accents inspired by old cinema houses.
- Playfair Display for dramatic headings and Inter for readable data.
- Glass-card treatments, tactile micro-interactions, and sharp editorial spacing.

---

## Technical Stack

- Android + Kotlin
- Jetpack Compose (Material 3)
- MVVM + repository pattern
- Room database
- Retrofit + OkHttp
- Hilt
- TMDB API
- Gemini API

---

## Download

Latest APK:

### [CineLog-v2.1-ProjectionistBooth.apk](https://github.com/krypton-arch/Cine-Log/blob/main/releases/CineLog-v2.1-ProjectionistBooth.apk)

1. Download the APK to your Android device.
2. Open it to install. You may need to allow installs from unknown sources.
3. This build uses the restored original launcher artwork and is packaged as a signed release APK.
4. The current release artifact is signed with the Android debug keystore, not a production release key.
5. Your archive data stays on-device, so back it up if you move to a new phone.

---

## Build From Source

### Prerequisites
- Android Studio Ladybug or newer
- A TMDB API key
- A Gemini API key

### Setup

Create or update `local.properties` in the project root:

```properties
TMDB_API_KEY=your_tmdb_key_here
GEMINI_API_KEY=your_gemini_key_here
```

Then build:

```bash
./gradlew assembleDebug
```

---

## Contributor Notes

- Preserve the noir tone and gold-accented visual language.
- Prefer the established typography system over ad hoc font choices.
- Avoid generic dark UI patterns when extending screens.

---

*CineLog - An archive for those who truly see.*
