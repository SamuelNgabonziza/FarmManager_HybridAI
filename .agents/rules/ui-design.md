---
trigger: always_on
---

# FarmOS UI Design & Organization Rules

1. **Dark Forest-Green Material 3 Palette & High Contrast**
   - Dark mode background: `#12261C` (deep forest green)
   - Dark mode elevated cards/surfaces: `#1B3628`
   - Primary accents: `#4CAF50` / `#66BB6A`
   - Always maintain high-contrast text (`#F1F5F1` / `#FFFFFF` on dark surfaces, `#1B241D` on light surfaces) and adapt cleanly to both light and dark system themes via `FarmUi` tokens.

2. **UI State Modeling & Layout Hierarchy**
   - Model screen states explicitly and clearly (`Loading`, `Empty`, `Content`, `Error`) with recoverable error actions and no blank/stuck placeholders.
   - Standardize card corner radii to `16.dp` (`RoundedCornerShape(16.dp)`), horizontal screen padding to `16.dp`, and vertical card spacing to `12.dp`.
   - Protect all compact badges, pills, and single-line labels with `maxLines = 1`, `softWrap = false`, and `overflow = TextOverflow.Ellipsis`.
   - Apply `imePadding()` on screens with text inputs or chat composers, and maintain proper top content padding + `clipToBounds()` on scrollable containers so banners and headers never clip behind top bars.

3. **Component Organization**
   - Keep reusable UI components (`FloatingHybridAiOrb`, `FarmAnimatedBackground`, `LivestockHeatStressCard`, etc.) in `ui/components/` and theme tokens in `ui/screens/FarmUi.kt` / `ui/theme/` so screens remain modular and readable without breaking existing navigation routes.

4. **Mandatory `@Preview` Functions**
   - Every new or updated UI component and screen must include light/dark `@Preview` functions (`FarmOsTheme`) and be inspected via `compose-preview` when available.
