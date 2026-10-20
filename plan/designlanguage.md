# 🎨 Design Language

## SMS Application — Material 3 Expressive Design System

> **Design System**: Material 3 Expressive (M3E)  
> **Framework**: Jetpack Compose  
> **Reference**: [material.io/blog/material-3-expressive](https://material.io/blog/material-3-expressive)

---

## 1. Design Philosophy

Material 3 Expressive evolves Material 3 beyond functional minimalism into **emotionally resonant, dynamic, and personality-driven** interfaces.

### Core Principles for This App

| Principle | Application in SMS App |
|-----------|----------------------|
| **Emotional Impact** | Chat bubbles with personality — warm colors, rounded shapes |
| **Intentional Asymmetry** | Different corner radii for sent vs received bubbles |
| **Spring-Based Motion** | FAB expansion, screen transitions, button taps |
| **Shape Morphing** | Contact avatars, folder cards, notification pills |
| **Hero Moments** | Compose screen send button, conversation opening transition |
| **Intelligence-Driven** | Dynamic color from wallpaper (Android 12+) |

---

## 2. Color System

### Dynamic Color (Android 12+)
The app uses `dynamicColorScheme()` when available, falling back to a hand-crafted seed color.

```kotlin
// Color.kt
val SeedColor = Color(0xFF6750A4)  // M3 default purple (fallback)

// Theme.kt
@Composable
fun SMSApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, ...) { content() }
}
```

### Custom Color Tokens

```kotlin
// Light scheme (fallback)
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),         // Message bubbles outgoing bg
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF), // Outgoing bubble background
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    secondaryContainer = Color(0xFFE8DEF8), // Incoming bubble background
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    tertiaryContainer = Color(0xFFFFD8E4), // Tag accent colors
    surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFE7E0EC),   // Conversation cards background
    outline = Color(0xFF79747E),
    error = Color(0xFFB3261E),
    background = Color(0xFFFFFBFE)
)

// Dark scheme (fallback)
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    primaryContainer = Color(0xFF4F378B),
    secondary = Color(0xFFCCC2DC),
    secondaryContainer = Color(0xFF4A4458),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF49454F),
    background = Color(0xFF1C1B1F)
)
```

### Semantic Color Usage

| Color Role | Used For |
|-----------|---------|
| `primaryContainer` | Outgoing SMS bubble background |
| `secondaryContainer` | Incoming SMS bubble background |
| `tertiaryContainer` | Tag chip backgrounds |
| `surface` | Screen backgrounds, cards |
| `surfaceVariant` | Input fields, conversation card bg |
| `error` | Failed message status, delete swipe |
| `outline` | Borders, dividers, unread indicator |

### Tag Color Palette (M3 Expressive Tonal)

```kotlin
val TagColors = listOf(
    Color(0xFFEF5350),  // Red
    Color(0xFFFF7043),  // Deep Orange
    Color(0xFFFFCA28),  // Amber
    Color(0xFF66BB6A),  // Green
    Color(0xFF26C6DA),  // Cyan
    Color(0xFF42A5F5),  // Blue
    Color(0xFF7E57C2),  // Deep Purple
    Color(0xFFEC407A),  // Pink
    Color(0xFF78909C),  // Blue Grey
)
```

---

## 3. Typography

M3 Expressive leverages **Roboto Flex** (variable font) for dynamic text scaling.

```kotlin
// Type.kt
val SMSTypography = Typography(
    // Display - Large hero text (Splash screen)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    // Headline - Screen titles
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    // Title - Card titles, section headers
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Body - Message text
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // Label - Timestamps, counts, chips
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Typography Usage Map

| Style | Used For |
|-------|---------|
| `displayLarge` | Splash screen app name |
| `headlineLarge` | Permissions screen title |
| `headlineMedium` | Screen titles (rarely used) |
| `titleLarge` | TopAppBar title |
| `titleMedium` | Conversation name in ConversationScreen |
| `bodyLarge` | Message bubble text |
| `bodyMedium` | Conversation preview text in HomeScreen |
| `labelLarge` | Button labels, send button |
| `labelMedium` | Timestamp on messages |
| `labelSmall` | Section headers, character counter |

---

## 4. Shape System (M3 Expressive — 35+ Shapes)

```kotlin
// Shape.kt
val SMSShapes = Shapes(
    // Small — chips, badges, small buttons
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    // Medium — cards, text fields, dialogs
    medium = RoundedCornerShape(12.dp),
    // Large — bottom sheets, nav drawer
    large = RoundedCornerShape(16.dp),
    // Extra Large — FAB, full-screen surfaces
    extraLarge = RoundedCornerShape(28.dp)
)

// Custom shapes for message bubbles
object BubbleShapes {
    // Incoming (received) — left-anchored
    val incoming = RoundedCornerShape(
        topStart = 4.dp,     // small — connected to avatar
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
    // Outgoing (sent) — right-anchored
    val outgoing = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 4.dp,       // small — tail indicator
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
    // First in a group (incoming)
    val incomingFirst = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 4.dp,
        bottomEnd = 16.dp
    )
    // Last in a group (outgoing)
    val outgoingLast = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 4.dp,
        bottomStart = 16.dp,
        bottomEnd = 4.dp
    )
}

// Folder card — morphing shape (uses Squircle-like rounding)
val FolderCardShape = RoundedCornerShape(20.dp)

// Contact avatar — Circle
val AvatarShape = CircleShape

// Tag chip — Stadium/Pill
val TagChipShape = RoundedCornerShape(50)

// Bottom sheet — Large top rounded
val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

// FAB — Expressive large rounded
val FabShape = RoundedCornerShape(16.dp)
```

---

## 5. Motion System (Spring-Based Physics)

M3 Expressive replaces easing curves with **spring-based physics** for all transitions.

```kotlin
// Motion.kt
object SMSMotion {

    // Standard spring — most UI transitions
    val StandardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Fast spring — button taps, chip selections
    val FastSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Gentle spring — background transitions, theme changes
    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Bouncy spring — FAB, send button, success animations
    val BounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessLow
    )
}

// Screen transition specs
val enterTransition: EnterTransition = slideInHorizontally(
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    initialOffsetX = { it }
) + fadeIn(animationSpec = tween(150))

val exitTransition: ExitTransition = slideOutHorizontally(
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    targetOffsetX = { -it / 3 }
) + fadeOut(animationSpec = tween(150))
```

### Motion Usage Guide

| Animation | Spring Type | Used For |
|-----------|------------|---------|
| Screen entry | `StandardSpring` | Navigate to new screen |
| FAB expand/shrink | `BounceSpring` | Scroll up/down on HomeScreen |
| Button press scale | `FastSpring` | Send button, FAB tap |
| Bottom sheet | `StandardSpring` | Filter/sort sheet open |
| Tag chip add | `BounceSpring` | Tag assigned to message |
| Message bubble appear | `GentleSpring` | New message received |
| Swipe-to-dismiss | Linear | Delete/archive swipe |
| Shared element | `StandardSpring` | ConversationCard → ConversationScreen |

---

## 6. Component Design Specifications

### ConversationCard (HomeScreen list item)

```
Shape:      RoundedCornerShape(12.dp)
Elevation:  0dp (flat, uses surface color)
Padding:    horizontal 16dp, vertical 12dp
Color:      MaterialTheme.colorScheme.surface
Hover:      surfaceVariant tint
Press:      scale(0.98f) with FastSpring
Unread:     bold title + primaryContainer left border 3dp
Avatar:     48dp circle with initials fallback
Time:       labelSmall, secondary color
Preview:    bodyMedium, maxLines=1, ellipsis
Badge:      12dp circle, primary color, white count text
```

### MessageBubble (ConversationScreen)

```
Outgoing:
  Shape:        BubbleShapes.outgoing
  Background:   primaryContainer
  Text color:   onPrimaryContainer
  Max width:    80% of screen width
  Padding:      12dp horizontal, 8dp vertical
  Tail:         triangle/shape indicator at topEnd corner

Incoming:
  Shape:        BubbleShapes.incoming
  Background:   secondaryContainer
  Text color:   onSecondaryContainer
  Max width:    80% of screen width

Timestamp:
  Position:     below-right of bubble
  Style:        labelSmall + outline color
  Content:      "2:45 PM" + status icon

Long-press:
  Action:       scale(1.05f) with BounceSpring
  Result:       ContextMenuDropdown (Copy / Delete / Tag / Star / Forward)
```

### FAB (HomeScreen)

```
Type:           ExtendedFloatingActionButton
Icon:           Icons.Default.Edit
Text:           "Compose" (visible when not scrolling)
Shape:          RoundedCornerShape(16.dp)
Color:          primaryContainer
Animation:      Collapse to small FAB on scroll down (spring)
                Expand to extended FAB on scroll up (bounce)
Shadow:         6dp elevation
Position:       bottom-end with 16dp margin
```

### NavigationBar (Bottom Nav)

```
Items:          Home (🏠) | Search (🔍) | Compose (✏️) | Contacts (👤) | Settings (⚙️)
Indicator:      Pill/rounded rectangle under selected item (M3 Nav indicator)
Label:          Always visible
Color:          surfaceVariant background
Selected icon:  primary color
Unselected:     onSurfaceVariant color
Animation:      indicator slides with spring
```

### SearchBar (M3 Expressive)

```
Type:           DockedSearchBar
Shape:          RoundedCornerShape(28.dp) when collapsed
                Fills top area when expanded
Leading:        Search icon
Trailing:       Avatar / profile icon
Placeholder:    "Search messages..."
Expand anim:    StandardSpring
History chips:  FilterChip row below when expanded
```

### TagChip

```
Shape:          RoundedCornerShape(50) (pill)
Color:          Tag.color (semi-transparent 20% alpha background)
Text:           Tag.name (labelMedium)
Leading:        Colored dot (●) 8dp circle
Trailing:       Close icon (optional, for removal)
Border:         1dp solid Tag.color at 40% alpha
```

---

## 7. Elevation & Depth

M3 Expressive uses **tonal elevation** (color overlay) instead of drop shadows for most surfaces.

| Surface | Elevation | Effect |
|---------|-----------|--------|
| Background | 0dp | Base color |
| Cards | 1dp | Slightly tinted surface |
| Elevated cards | 3dp | More tinted |
| FAB | 6dp | Drop shadow |
| Modal bottom sheet | 8dp | Heavy tint |
| Dialog | 24dp | Dramatic separation |

---

## 8. Iconography

```
Icon Set:       Material Symbols (Rounded style)
Default size:   24dp
Touch target:   48dp minimum
Nav icons:      28dp
FAB icon:       24dp
```

### Icon Map

| Feature | Icon |
|---------|------|
| Home / Inbox | `Icons.Rounded.Inbox` |
| Compose / Write | `Icons.Rounded.Edit` |
| Search | `Icons.Rounded.Search` |
| Contacts | `Icons.Rounded.Contacts` |
| Settings | `Icons.Rounded.Settings` |
| Folders | `Icons.Rounded.Folder` |
| Tags | `Icons.Rounded.Label` |
| Archive | `Icons.Rounded.Archive` |
| Delete / Trash | `Icons.Rounded.Delete` |
| Star / Favorite | `Icons.Rounded.Star` |
| Send | `Icons.Rounded.Send` |
| Back | `Icons.AutoMirrored.Rounded.ArrowBack` |
| More (menu) | `Icons.Rounded.MoreVert` |
| Notification | `Icons.Rounded.Notifications` |
| Phone | `Icons.Rounded.Call` |
| Check (read) | `Icons.Rounded.Done` / `Icons.Rounded.DoneAll` |

---

## 9. Spacing & Layout Grid

```
Base unit:          4dp
Layout margin:      16dp (phones), 24dp (tablets)
Content padding:    16dp
List item height:   72dp (standard), 56dp (compact)
Section gap:        8dp
Component gap:      4dp (tight), 8dp (normal), 16dp (loose)
FAB margin:         16dp from bottom + bottom nav height
TopAppBar height:   64dp (standard)
NavigationBar:      80dp height
```

---

## 10. Dark Mode Adaptations

```kotlin
// Example: Message bubble adapts to dark mode
@Composable
fun MessageBubble(isOutgoing: Boolean, ...) {
    val bubbleColor = if (isOutgoing) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    // In dark mode, M3 automatically provides darker tonal variants
    // No manual color switching needed when using colorScheme tokens
    Card(
        colors = CardDefaults.cardColors(containerColor = bubbleColor),
        shape = if (isOutgoing) BubbleShapes.outgoing else BubbleShapes.incoming
    ) { ... }
}
```

**Rule**: Never use hardcoded colors in composables. Always reference `MaterialTheme.colorScheme.*` tokens so dark mode works automatically.

---

## 11. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Minimum touch target | 48dp × 48dp for all interactive elements |
| Text contrast | All M3 color pairs meet WCAG AA (4.5:1) |
| Content descriptions | All icons have `contentDescription` |
| Font scaling | All text uses `sp` units (respects system font size) |
| Reduce motion | Check `LocalDensity` or `WindowManager` flags |
| Screen reader | `Semantics` modifiers on custom components |

---

## 12. Design Tokens Reference

```kotlin
// Quick reference for common design decisions
object DesignTokens {
    // Spacing
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 16.dp
    val SpacingL = 24.dp
    val SpacingXL = 32.dp

    // Sizes
    val AvatarSize = 48.dp
    val AvatarSizeSmall = 36.dp
    val IconSize = 24.dp
    val BadgeSize = 20.dp

    // Durations (for reference - prefer spring specs)
    val DurationShort = 150        // ms
    val DurationMedium = 300       // ms
    val DurationLong = 500         // ms

    // Bubble constraints
    val BubbleMaxWidthFraction = 0.80f  // 80% of screen width
    val BubblePaddingH = 12.dp
    val BubblePaddingV = 8.dp

    // Card
    val CardCornerRadius = 12.dp
    val CardElevation = 1.dp
}
```
