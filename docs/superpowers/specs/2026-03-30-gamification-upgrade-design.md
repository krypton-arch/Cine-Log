# Gamification Upgrade Design Spec

## Overview
The gamification system (Profile Screen) is shedding its generic styling and emoji-based badges. We are bringing it completely in line with the "Noir Archive" styling by switching to the "Golden Relics" approach and infusing the screen with elegant cursive/serif typography.

## The Typography
To make the "Archive" feel authentic, nearly all descriptive headings and major numeric/stat outputs on the Profile page will transition to the cursive serif style.
- The `levelName` and "Badge Showcase" titles will be strongly italicized.
- Stat card titles ("Total Films", "Current Streak") will read elegantly utilizing italic treatments in our core serif or custom title font.

## The "Golden Relic" Badges
- **Icon Strategy**: We will no longer display the database-provided `iconRes` (the emojis). Instead, the UI layer will map the `badgeId` to high-quality Material Icons (`Icons.Outlined.*`).
  - `first_log` -> `Icons.Outlined.Theaters`
  - `centurion` -> `Icons.Outlined.WorkspacePremium`
  - `horror_fiend` -> `Icons.Outlined.Visibility` (or similar eerie abstract icon)
  - `old_soul` -> `Icons.Outlined.SlowMotionVideo`
  - `binge_king` -> `Icons.Outlined.Weekend`
  - `marathon` -> `Icons.Outlined.DirectionsRun`
  - `critic` -> `Icons.Outlined.EditNote`
  - `streak_7` -> `Icons.Outlined.LocalFireDepartment`
- **Visual State System**:
  - `Locked`: A frosted `glassSurface` with `alpha = 0.2f`. The icon is faded back to a 30% alpha variant of the dark surface text.
  - `Unlocked`: A `glassCard` providing depth. The icon is drawn with the rich gold `primaryContainer` accent color and uses a shadow/glow modifier to imply active energy.

## Data Layer Consistency
We will not modify the Room database `iconRes` structure to avoid unnecessary migrations. All overrides happen purely computationally in the UI.
