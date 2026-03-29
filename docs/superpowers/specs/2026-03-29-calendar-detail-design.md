# Movie Diary Calendar Detail Design

## Goal
Enhance the existing `DiaryScreen` calendar grid so that users can tap on a specific date to view all movies logged on that day, presented natively using real data from the local database.

## Approach
Implement a sleek `ModalBottomSheet` displaying a vertical grid of all movie logs matching the tapped date.

## Interaction Flow
1. **Calendar Update**: The existing `logMap` generation, which currently transforms the list of logs directly into a `Map<Int, String>` mapping a day to a single `posterPath` string, will be refactored to `Map<Int, List<LogWithMovie>>`.
2. **Calendar Cell Visuals**: For days mapping to more than one log, we will render a subtle "+N" indicator box in the top-right corner to signify a multi-movie day.
3. **Selection**: When a calendar cell with `logs.isNotEmpty()` is tapped, an `onClick` event occurs that sets a local state `selectedDayLogs` to the list of `LogWithMovie` entries and opens the `ModalBottomSheet`.
4. **Bottom Sheet UI**: The UI will dynamically generate a header reading "OCTOBER 14, 2026" (or equivalent based on selection) next to the total string (e.g., "3 FILMS"). 

## Constraints
- **NO MOCK DATA**: The list must be strictly populated by the already loaded Kotlin Flow from the `DiaryViewModel` (`viewModel.logs`), filtering correctly via milliseconds `Instant` matching that day.
- **Consistent Aesthetics**: Components will be built using existing Noir Archive styling (glassmorphism cards, deep blacks, rich golds).

## Architecture Details
- State variable: `var selectedDate by remember { mutableStateOf<LocalDate?>(null) }` inside `DiaryScreen`
- Filter logic: `val selectedDayLogs = logs.filter { it.date == selectedDate }`
- View Component: `ModalBottomSheet` with a `LazyVerticalGrid` representing each `LogWithMovie` card, consisting of an `AsyncImage` for the poster and a custom translucent overlay reading out `it.logEntry.rating` inside a row with `Icons.Default.Star`.
