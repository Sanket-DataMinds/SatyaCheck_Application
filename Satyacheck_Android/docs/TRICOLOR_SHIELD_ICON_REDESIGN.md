# TricolorShieldIcon Redesign

This document outlines the changes made to the Satya Check app's TricolorShieldIcon implementation.

## Design Changes

1. **Shield Shape**: Shield with Indian flag colors (saffron, white, green)
2. **Border Color**: Changed to a darker grey (#444444) for better visibility
3. **Checkmark**: Navy blue checkmark in the center white band
4. **White Band**: Made more visible by explicitly defining its height
5. **Animation**: Added pulse animation capability

## Files Updated

1. **Vector Drawables**:
   - `ic_tricolor_shield.xml`: Main shield icon resource
   - `ic_launcher_foreground.xml`: Launcher icon foreground
   - `ic_launcher_fallback.xml`: Fallback launcher icon

2. **Animation Resources**:
   - Created `pulse_animation.xml` with scale animation from 1.0 to 1.1 and back

3. **Kotlin Components**:
   - `TricolorShieldIcon.kt` in `components.ui` package: Updated with animation support
   - `TricolorShieldIcon.kt` in `presentation.components` package: Updated with animation and darker border

## Usage Examples

### Basic Usage

```kotlin
// Default icon
TricolorShieldIcon()

// Custom size
TricolorShieldIcon(size = 32.dp)

// With pulsing animation
TricolorShieldIcon(pulsing = true)

// Custom modifier
TricolorShieldIcon(
    modifier = Modifier.padding(8.dp),
    pulsing = true
)
```

### Animation Notes

The pulsing animation is implemented in two ways:

1. **Composable Animation**: Using Jetpack Compose's animation system with `rememberInfiniteTransition`
2. **XML Animation**: Using Android's animation framework via `pulse_animation.xml`

## Cross-Platform Consistency

A React component version of the icon has been documented in `docs/react-tricolor-shield-icon.md` to ensure consistent branding across web platforms.
