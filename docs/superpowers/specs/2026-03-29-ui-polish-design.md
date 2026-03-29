# UI/UX Polish Design: Immersive Scrolling & Advanced Depth

## Goal
Elevate the CineLog "Noir Archive" aesthetic to a Tier-1 app through premium scrolling behaviors, immersive shimmer loading, and dynamic glassmorphism depth.

## Approaches Selected
1. **Immersive Scroll & Loading States (Option B)**
2. **Advanced Glassmorphism Depth (Option C)**

## Implementation Details

### B. Immersive Scroll & Shimmer
- **Shimmer Replacements**: In `WatchlistScreen.kt` and `MovieDetailScreen.kt`, remove the static `CircularProgressIndicator` and replace it with a full-page beautiful shimmer skeleton. For `WatchlistScreen` search, this involves showing a grid of `Box(modifier = Modifier.aspectRatio(2f/3f).shimmerEffect())`. For `MovieDetailScreen`, it's a hero box shimmer and layout skeleton.
- **Scroll-Linked Blur / Darken**: In `MovieDetailScreen.kt`, tie the `scrollState.value` not just to parallax translation, but to an overlay alpha or blur modifier on the hero `AsyncImage`, fading the image to black/blur as it scrolls up under the content.

### C. Advanced Glassmorphism Depth
- **Dynamic Shifting Glow**: In `AnimationUtils.kt`, upgrade `Modifier.glassCard`. We will animate the inner `Brush.radialGradient`'s center offset slowly using `rememberInfiniteTransition`, creating a slow-moving light catching behind the frosted glass.
- **Enhanced Drop Shadows**: Add a subtle colored drop shadow (`elevation`) reflecting the gold tint (`#F5C518`) to distinguish layered elements more luxuriously.
