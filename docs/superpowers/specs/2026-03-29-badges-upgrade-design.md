# Empty State & Regal Badges UI Design
*Replacing standard emojis and pill-badges with elements aligned to our Noir Archive.*

## Architecture & Structure
We are removing the last remnants of "standard/generic app UI" (emojis and pill-shaped genre tags) to enforce the high-end cinematic feel across the application.

### 1. The Phantom Reel Empty State
- **Current**: A unicode robot/projector emoji (`📽️`)
- **New Pattern**: A massive, perfectly-scaled outlined icon (`Icons.Outlined.LocalMovies` or `Outlined.Theaters`) wrapped in our custom `glassSurface` modifier to provide a soft, luxurious glowing drop-shadow.
- **Typography**: The text "The archive awaits" below it will utilize the regal serif font (Playfair Display) in an elegant italic format, making the empty state feel like an invitation rather than an error.

### 2. Regal Cursive "No-Badge" Genres
- **Current**: Heavy, rounded background pills surrounding capitalized sans-serif text.
- **New Pattern**: A sheer, elegant text string separating up to three genres with classic cinematic bullets (`•`). 
- **Typography Implementation**: We will utilize a lightweight italic serif font configuration (the 'regal cursive' feel) with a subtle muted-gold or soft-silver tint. E.g., `DRAMA • NOIR • MYSTERY`.
- **Location**: This applies anywhere badges used to be (the lists, the movie detail hero, and the Watchlist search cards).

## Technical Implementation
- Remove all `Surface` wraps acting as badge backgrounds.
- Compose a unified `joinToString(" • ")` from the genre lists.
- Apply `fontFamily = CineLogTypography.titleLarge.fontFamily`, `fontStyle = FontStyle.Italic` directly to the `Text` composable handling the string.
