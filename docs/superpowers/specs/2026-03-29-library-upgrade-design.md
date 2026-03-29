# Library Screen Upgrade Design
*Combining the Multi-View Archive (A) and Cinematic Highlights (B).*

## Architecture & Structure
We will transition the `WatchlistScreen` from a standard `Column` + `LazyColumn` into a unified `LazyVerticalGrid`. This allows us to seamlessly scroll headers, horizontal carousels, and multi-column grids together without nested scrolling crashes.

### 1. Carousel Header (The "Highlights")
For users with an active watchlist, the very top of their list (right beneath the search bar) will feature a horizontal scrolling `LazyRow`.
- **Content**: The top 5 (or recently added) items in the watchlist.
- **Design**: Large, portrait-oriented glass-cards featuring beautiful TMDB posters and subtle title gradients.

### 2. The View Toggle
Situated directly below the Highlights carousel will be a sticky or standard "Settings" row containing:
- "ALL FILMS" header text
- A sleek toggle button to switch between `GridView` and `ListView`.

### 3. Grid View vs List View
We will maintain a `Boolean` state `isGridView`:
- **List View (Default)**: Reuses the newly polished `WatchlistMovieCard` we currently have.
- **Grid View**: Displays 3 columns across. Each item is just the movie poster in a `glassCard` with an elegant aspect ratio (2:3). A subtle `Icon` or long-press will handle actions like 'Log to Diary' or 'Remove'.

## Technical Considerations
- **GridItemSpan**: The Search bar, Carousel, and List View items will use `GridItemSpan(maxLineSpan)` to take up the full width, whereas the Grid View items will default to single-column spanning.
- **State Management**: The toggle state (`isGridView`) will be a simple `remember { mutableStateOf(false) }`.
