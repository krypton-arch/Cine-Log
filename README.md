# CineLog: The Noir Archive Manifesto

> "Cinema is a mirror that can focus the shadows of the past into the light of the present."

**CineLog** is not a utility; it is a sanctuary. It is a high-end, premium film diary and personal library for Android, meticulously crafted for those who believe that watching a movie is an act of preservation. In an era of disposable content streams, CineLog offers a **Noir Archive**—a space to slow down, reflect, and build a permanent record of your cinematic journey.

![App Icon](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

---

## 🖤 The Noir Philosophy: Design as Respect

CineLog rejects the flat, generic "dark modes" of modern software. Instead, it embraces a **Brutalist Noir** aesthetic:
- **Pure Black Architecture**: The UI is built on a foundation of absolute black (`#000000`), allowing movie posters and archival data to emerge from the shadows.
- **Silver & Gold Accents**: Interactive elements are defined by single-pixel silver borders and regal primaryContainer (Gold) highlights, evoking the elegance of 1940s cinema houses.
- **Typography of Influence**: A deliberate juxtaposition between the elegant, serifed **Playfair Display** (for moments of reflection and headers) and the high-end, archival **Inter** (for technical data and readability).
- **Tactile Interaction**: Every tap and scroll is refined with bounce-click animations and luminous glowing borders (glassmorphism), making the digital archive feel like a physical artifact.

---

## 🎞️ The Archival Pillars: How We Preserve

### 1. The Diary (Visceral Logging)
Your watch history is more than a list of dates. CineLog’s Diary allows you to record:
- **Atmospheric Reviews**: Write down your thoughts while the film is still vivid.
- **Star Ratings**: A 5-star scale to quantify your personal impact.
- **Automatic Metadata**: Once logged, the Archive automatically extracts the **Director**, **Genres**, and **Release Decade**, ensuring your journey is indexed with professional precision.

### 2. The Vault (Library Curation)
The **Universal Library** (TMDB Integration) is your gateway to discovery. 
- **Identify Any Film**: Access a global database of cinema history.
- **Curated Watchlist**: Save discovered treasures to your **Personal Vault**.
- **Contextual Discovery**: Tap any film to dive into its overview, technical runtime, and specific genre tags before deciding to log it.

### 3. The Passport (Advanced Analytics)
CineLog analyzes your habits to reveal your cinematic identity:
- **The Genre Passport**: A custom-drawn `Canvas` donut chart that transforms your watch history into a visual "Passport." If you’ve spent your month in the shadows of Noir, your Passport will reflect it.
- **Archive Stats**: View total films logged, high-precision hours watched, and average ratings.
- **The Identity Engine**: The app identifies your **Top Director** (the auteur who speaks to you most) and your **Favorite Decade** (the era you find yourself dwelling in), updating in real-time as your archive grows.

---

## 🎖️ The Cinephile’s Path: Subtle Milestones

In CineLog, we avoid the jarring "notifications" and "leaderboards" of typical gamified apps. Instead, we offer **Subtle Milestones**—quiet markers of your discipline and taste.

### Persistent Badges (Memorabilia)
Unlock 8 milestones that represent your evolution:
- 🎬 **First Frame**: The preservation of your first film.
- 💯 **Centurion**: A monument to 100 films archived.
- 🎞️ **Old Soul**: A proven commitment to classic cinema (10+ pre-1980 films).
- 👻 **Horror Fiend**: For those who find comfort in the macabre (10+ horror films).
- 👑 **Binge King**: A marathon of 5 films in a single week.
- 🏃 **Marathon Runner**: Surpassing 1000 logged minutes of light and shadow.
- ✍️ **The Critic**: The discipline of writing 10+ detailed reviews.
- 🔥 **Week Warrior**: A 7-day streak of cinematic preservation.

### Monthly Invitations (Challenges)
Gentle, recurring invitations (like *Indie Films* or *Weekend Warrior*) that encourage you to broaden your horizons while respecting the pace of your own life.

---

## 🔮 Beyond the Shadows: The Vision

The Archive is static no longer. We are building toward **The Projectionist’s Booth**—a futuristic AI integration:
- **The Projectionist (AI)**: A moody, knowledgeable conversationalist (powered by Gemini Flash) who remembers your archive.
- **Archive Insights**: Contextual notes like *"The Critic's Note"* that appear beside your logs, offering technical analysis of cinematography and lighting based on your specific taste.

---

## 🛠️ Technical Integrity & Architecture

CineLog is engineered for permanence, ensuring your data is as safe as a film strip in a cool vault.

- **Architecture**: **MVVM + Clean Repository Pattern**. Logic is decoupled from the UI for maximum testability.
- **UI System**: **100% Jetpack Compose** (Material 3). Every custom view (like the Genre Passport) is built using the low-level `DrawScope` for performance and precision.
- **Data Engine**: **Room Database (v5)**. Featuring a self-healing seeding logic that ensures your profile and initial milestones are generated reliably even after updates.
- **Networking**: **Retrofit2 + OkHttp**. High-performance, asynchronous metadata fetching from the TMDB global catalog.
- **Security**: **Secrets Gradle Plugin** to protect TMDB API keys and environment-specific data.

## 🗝️ Download & Stewardship

To experience the Noir Archive on your own device, you can download the latest pre-built **CineLog APK** from this repository.

### [Download Latest: CineLog-v2-NoirArchive.apk](https://github.com/krypton-arch/Cine-Log/blob/main/releases/CineLog-v2-NoirArchive.apk)

1. Download the [APK](https://github.com/krypton-arch/Cine-Log/blob/main/releases/CineLog-v2-NoirArchive.apk) to your Android device.
2. Open the file to install (you may need to enable "Install from Unknown Sources" in your device settings).
3. **Stewardship**: As this is a personal archive, your data is stored entirely on your device. We recommend backing up your Room Database periodically if you switch devices.

---

## 🛠️ Building from Source

If you prefer to build the project yourself, follow these steps:

### Prerequisites
- **Android Studio (Ladybug or newer)**
- **TMDB API Key** (Get one at [themoviedb.org](https://developer.themoviedb.org/docs))

### Setup
1. Create/Modify your `local.properties` file in the project root:
   ```properties
   TMDB_API_KEY="your_api_key_here"
   ```
2. Sync the project with Gradle.
3. Build and Deploy:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🌑 Aesthetic Guidelines for Contributors
Contributors are expected to respect the **Noir Archive** design language:
1. **Never use generic colors**. Use the palette provided in `@ui.theme`.
2. **Typography is paramount**. Use `Playfair Display` for expression and `Inter` for information.
3. **Respect the Black**. The background is `#000000`. Do not use "light" grey or "card" grey unless it is for the single-pixel border accents.

---

*CineLog — An Archive for those who truly see.*
