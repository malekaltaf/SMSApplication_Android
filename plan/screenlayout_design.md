# 🖼️ Screen Layout Design

## SMS Application — Screen-by-Screen UI Specifications

> **Design Language**: Material 3 Expressive  
> **Framework**: Jetpack Compose  
> **Orientation**: Portrait primary, Landscape supported

---

## Screen Index

| Screen | Route | Access |
|--------|-------|--------|
| [Splash](#1-splash-screen) | `splash` | App launch |
| [Permissions](#2-permissions-screen) | `permissions` | First launch |
| [Home](#3-home-screen) | `home` | Bottom Nav Tab 1 |
| [Conversation](#4-conversation-screen) | `conversation/{threadId}` | From Home/Search/Contacts |
| [Compose](#5-compose-screen) | `compose?recipient={address}` | FAB / Bottom Nav Tab 3 |
| [Search](#6-search-screen) | `search` | Bottom Nav Tab 2 |
| [Contacts](#7-contacts-screen) | `contacts` | Bottom Nav Tab 4 |
| [Folders](#8-folders-screen) | `folders` | Navigation Drawer |
| [Tags](#9-tags-screen) | `tags` | Navigation Drawer / Context menu |
| [Settings](#10-settings-screen) | `settings` | Bottom Nav Tab 5 |

---

## 1. Splash Screen

```
┌─────────────────────────────────┐
│                                 │
│                                 │
│         ┌──────────┐            │
│         │  🗨️ Logo  │  ← Shape-morphing animated icon
│         │   72dp   │            │
│         └──────────┘            │
│                                 │
│       SMSApplication            │  ← Display Large (M3)
│       Your messages,            │
│       beautifully organized     │  ← Body Large
│                                 │
│     [━━━━━━━━━━   ] 65%         │  ← LinearProgressIndicator (M3)
│                                 │
└─────────────────────────────────┘
```

**Components**:
- `Scaffold` with no top/bottom bar
- Animated logo using `AnimatedVisibility` with `spring()` enter
- `LinearProgressIndicator` (indeterminate) at bottom
- Background: `MaterialTheme.colorScheme.background`
- Auto-navigates to `PermissionsScreen` or `HomeScreen` after 1.5s

---

## 2. Permissions Screen

```
┌─────────────────────────────────┐
│  ← (no top bar)                 │
│                                 │
│       🔒                        │  ← Icon 48dp
│  Set up your SMS app            │  ← Headline Large
│                                 │
│  ┌─────────────────────────┐    │
│  │ ✅ Read Messages        │    │  ← PermissionRow
│  │    Read SMS from device │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │ ⬜ Send Messages        │    │
│  │    Send SMS to contacts │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │ ⬜ Receive Messages     │    │
│  │    Get new SMS          │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │ ⬜ Read Contacts        │    │
│  │    Show contact names   │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │ ⬜ Notifications        │    │
│  │    Alert on new SMS     │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │   Grant All Permissions  │   │  ← FilledButton (M3)
│  └─────────────────────────┘    │
│                                 │
│  Set as default SMS app too?    │
│  ┌─────────────────────────┐    │
│  │   Set as Default        │   │  ← OutlinedButton
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

**Components**:
- `Column` with `verticalScroll`
- Each permission row: `ListItem` with leading `Icon`, trailing `Checkbox`
- `FilledButton` calls `ActivityResultContracts.RequestMultiplePermissions`
- `OutlinedButton` opens `RoleManager` for default SMS intent

---

## 3. Home Screen

```
┌─────────────────────────────────┐
│ ≡  SMSApplication      🔍  ⋮   │  ← TopAppBar (CenterAligned M3)
│    (hamburger)(title)(search)(menu)
├─────────────────────────────────┤
│ [All] [Unread] [Groups] [⭐]    │  ← FilterChipRow (horizontal scroll)
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ 👤  John Doe        2:45 PM │ │  ← ConversationCard
│ │     Hey are you there?  [2] │ │     (unread badge)
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 👤  +1-555-1234    Yesterday│ │
│ │     Your OTP is 847392      │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 👤  Mom            Mon      │ │
│ │     Ok beta, see you soon   │ │
│ └─────────────────────────────┘ │
│           ...                   │
│                                 │
│                        [✏️ FAB] │  ← Extended FAB (M3 Expressive)
└─────────────────────────────────┘
│ 🏠 Home │ 🔍 Search │ ✏️ │ 👤 Contacts │ ⚙️ │  ← NavigationBar (M3)
```

**Layout Details**:
- `Scaffold` with `TopAppBar`, `NavigationBar`, `FloatingActionButton`
- `LazyColumn` for conversation list
- `ConversationCard` uses `Card` with `shape = RoundedCornerShape(16.dp)`
- `FilterChipRow`: `LazyRow` with `FilterChip` (M3 Expressive)
- FAB: `ExtendedFloatingActionButton` — shrinks to `FloatingActionButton` on scroll
- Swipe on card: `SwipeToDismiss` — right=read, left=archive
- Sort menu: `DropdownMenu` from `⋮` icon button
- Navigation Drawer accessible via hamburger icon (for Folders, Tags)

---

## 4. Conversation Screen

```
┌─────────────────────────────────┐
│ ← John Doe            📞  ⋮    │  ← TopAppBar with back button
│   Online 5 min ago              │  ← subtitle (contact status)
├─────────────────────────────────┤
│        ─── Monday ───           │  ← StickyDateHeader
│                                 │
│  ┌────────────────────────┐     │
│  │ Hey are you there?     │     │  ← Incoming bubble (left-aligned)
│  │ Shape: RoundedTopRight │ 2:30│     Card with asymmetric corner
│  └────────────────────────┘     │
│                                 │
│          ┌──────────────────┐   │
│          │ Yes! Coming soon │   │  ← Outgoing bubble (right-aligned)
│          │                  │   │     Tonal color (M3 secondary)
│          │            ✅2:32│   │     Sent/Delivered indicator
│          └──────────────────┘   │
│                                 │
│  ┌────────────────────────┐     │
│  │ 🏷️ Work  ⭐ Starred    │     │  ← Tagged message with chips
│  │ See you at 5pm         │ 3:00│
│  └────────────────────────┘     │
│                                 │
│        ─── Today ───            │
│                                 │
├─────────────────────────────────┤
│ ┌──────────────────────┐ [Send]│  ← MessageInputRow
│ │ Type a message...    │  [🎤] │     OutlinedTextField + IconButton
│ └──────────────────────┘       │
└─────────────────────────────────┘
```

**Layout Details**:
- `Scaffold` with `TopAppBar` and bottom `MessageInputRow`
- `LazyColumn` (reversed) for messages — newest at bottom
- Sticky date headers using `stickyHeader {}` in LazyColumn
- Incoming bubble: `Card(shape = RoundedCornerShape(topStart=4,topEnd=16,bottomStart=16,bottomEnd=16))`
- Outgoing bubble: `Card(shape = RoundedCornerShape(topStart=16,topEnd=4,bottomStart=16,bottomEnd=16))`
- Incoming: `MaterialTheme.colorScheme.secondaryContainer`
- Outgoing: `MaterialTheme.colorScheme.primaryContainer`
- Long-press message → `DropdownMenu` (Copy, Delete, Tag, Star, Forward)
- `MessageInputRow`: `Row` with `OutlinedTextField` (weight=1f) + `IconButton` for send + `IconButton` for voice

---

## 5. Compose Screen

```
┌─────────────────────────────────┐
│ ←  New Message               ✅ │  ← TopAppBar with close + done
├─────────────────────────────────┤
│ To:                             │
│ ┌─────────────────────────────┐ │
│ │ 👤 [Jane] × [+1234] ×       │ │  ← ChipInputField (contact chips)
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │  Suggestions:               │ │  ← ContactSuggestionList
│ │  👤 Jane Doe   +1-555-5678  │ │
│ │  👤 James Bond +1-555-0099  │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│                                 │
│ ┌─────────────────────────────┐ │
│ │                             │ │
│ │   Type your message...      │ │  ← Multiline TextField
│ │                             │ │
│ └─────────────────────────────┘ │
│                                 │
│ 142 / 160 chars · 1 SMS        │  ← CharacterCounter
├─────────────────────────────────┤
│            [ ✏️ Send Message ]  │  ← FilledButton (M3 Expressive)
│            [ 💾 Save as Draft ] │  ← TextButton
└─────────────────────────────────┘
```

**Layout Details**:
- `Scaffold` with `TopAppBar`
- Recipient field: Custom `FlowRow` with `InputChip` for each added contact
- `LazyColumn` below recipient for live contact suggestions
- Message body: `TextField(modifier = Modifier.fillMaxWidth().weight(1f))`
- Character counter: `Text` styled with `MaterialTheme.typography.labelSmall`
- `FilledButton` with spring-scale animation on tap (M3 Expressive motion)

---

## 6. Search Screen

```
┌─────────────────────────────────┐
│ ←  ┌─────────────────────────┐ │  ← SearchBar (M3 Expressive)
│    │ 🔍 Search messages...   │ │
│    └─────────────────────────┘ │
├─────────────────────────────────┤
│ [📅 Date] [🏷️ Tag] [📁 Folder] │  ← Filter chips (horizontal scroll)
│ [Nov 2024 ×] [Work ×]          │  ← Active filter chips (dismissible)
├─────────────────────────────────┤
│ RECENT SEARCHES                 │  ← (shown when query empty)
│ 🕐 "bill payment"              │
│ 🕐 "meeting tomorrow"          │
├─────────────────────────────────┤
│ RESULTS (24)                    │  ← Result count
│ ┌─────────────────────────────┐ │
│ │ 👤 John Doe        2:45 PM  │ │  ← SearchResultCard
│ │  ...your **bill** is due... │ │  ← Highlighted match (bold)
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 👤 Bank SMS        12 Oct   │ │
│ │  **Bill** payment success   │ │
│ └─────────────────────────────┘ │
│           ...                   │
└─────────────────────────────────┘
│ 🏠 │ 🔍 Search │ ✏️ │ 👤 │ ⚙️ │  ← NavigationBar
```

**Layout Details**:
- `SearchBar` (M3) expands to `DockedSearchBar` on focus
- `FilterChipRow`: `LazyRow` below search bar
- `LazyColumn` with two sections: Recent Searches + Results
- Search result matches highlighted using `AnnotatedString` with `SpanStyle(fontWeight = FontWeight.Bold)`
- `BottomSheet` appears on filter chip tap for detailed filter selection
- Date filter: `DateRangePicker` (M3)
- Tag filter: `LazyVerticalGrid` of `FilterChip` with color dot
- Empty state: Illustration + "No messages found" text

---

## 7. Contacts Screen

```
┌─────────────────────────────────┐
│  Contacts               🔍  ⋮  │  ← TopAppBar
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ 🔍 Search contacts...       │ │  ← SearchField
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│ A                               │  ← AlphaHeader
│ ┌─────────────────────────────┐ │
│ │ 👤 Alice Brown   +1-555-001 │ │  ← ContactListItem
│ │    Last: "See you at 5pm"   │ │     (with last message preview)
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 👤 Alex Smith    +1-555-002 │ │
│ └─────────────────────────────┘ │
│ B                               │
│ ┌─────────────────────────────┐ │
│ │ 👤 Bob Jones     +1-555-010 │ │
│ └─────────────────────────────┘ │
│           ...                   │
│                                 │
│             Alphabetical index: │
│             A B C D E... (side) │  ← AlphaFastScroll
└─────────────────────────────────┘
│ 🏠 │ 🔍 │ ✏️ │ 👤 Contacts │ ⚙️│  ← NavigationBar
```

**Layout Details**:
- `LazyColumn` with `stickyHeader` for each letter group
- Right-side alphabet index: `Column` anchored to end with `Box(contentAlignment = Alignment.CenterEnd)`
- `ListItem` (M3) with `leadingContent = ContactAvatar`, `overlineContent = letter`, `trailingContent = IconButton`
- Tapping contact opens `ConversationScreen` for that contact's thread

---

## 8. Folders Screen

```
┌─────────────────────────────────┐
│ ←  Folders                      │  ← TopAppBar
├─────────────────────────────────┤
│ SYSTEM FOLDERS                  │
│ ┌─────────┐ ┌─────────┐        │
│ │ 📥 Inbox│ │ 📤 Sent │        │  ← LazyVerticalGrid (2 columns)
│ │   1,234 │ │     456 │        │     FolderCard with shape morphing
│ └─────────┘ └─────────┘        │
│ ┌─────────┐ ┌─────────┐        │
│ │📋 Drafts│ │ 🗑️ Trash│        │
│ │       3 │ │       12│        │
│ └─────────┘ └─────────┘        │
│ ┌─────────────────────┐        │
│ │ 📦 Archived      89 │        │
│ └─────────────────────┘        │
├─────────────────────────────────┤
│ AUTO FOLDERS                    │
│ ┌─────────┐ ┌─────────┐        │
│ │👤 By    │ │📅 By    │        │
│ │ Contact │ │  Date   │        │
│ └─────────┘ └─────────┘        │
├─────────────────────────────────┤
│ MY FOLDERS                [+ Add]
│ ┌─────────────────────────────┐ │
│ │ 💼 Work              234    │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 🏠 Family            89     │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

**Layout Details**:
- `LazyColumn` with sections (System, Auto, Custom)
- System Folders: `LazyVerticalGrid(columns = GridCells.Fixed(2))`
- `FolderCard`: `Card` with `animateContentSize()`, icon + name + count
- Custom folder creation: `AlertDialog` with `TextField` + icon picker
- Folder card long-press: edit/delete options

---

## 9. Tags Screen

```
┌─────────────────────────────────┐
│ ←  Tags                   [+ New]  ← TopAppBar with action
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ 🔴 ● Work           45 msgs │ │  ← TagListItem
│ │              [Edit] [Delete] │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 🟢 ● Personal       128 msgs│ │
│ │              [Edit] [Delete] │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 🔵 ● Banking         22 msgs│ │
│ │              [Edit] [Delete] │ │
│ └─────────────────────────────┘ │
│                                 │
│                                 │
│  No tags yet?                   │  ← EmptyState (if no tags)
│  Tap + to create your first tag │
└─────────────────────────────────┘
```

**Create Tag Dialog**:
```
┌─────────────────────────────────┐
│  Create New Tag                 │
│  ┌───────────────────────────┐  │
│  │ Tag name...               │  │  ← OutlinedTextField
│  └───────────────────────────┘  │
│  Color:                         │
│  ● ● ● ● ● ● ● ● ● (palette)  │  ← ColorPicker Row
│           [Cancel]  [Create]    │
└─────────────────────────────────┘
```

---

## 10. Settings Screen

```
┌─────────────────────────────────┐
│ ←  Settings                     │  ← TopAppBar
├─────────────────────────────────┤
│ DEFAULT APP                     │  ← Section header
│ ┌─────────────────────────────┐ │
│ │ 📱 Default SMS App          │ │  ← ListItem with trailing Switch
│ │    Currently: ✅ Set        │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│ APPEARANCE                      │
│ ┌─────────────────────────────┐ │
│ │ 🌙 Theme                    │ │
│ │    System / Light / Dark    │ │  ← SegmentedButton (M3)
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 🔤 Font Size                │ │
│ │    ── ●─────── (slider)     │ │  ← Slider (M3)
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│ NOTIFICATIONS                   │
│ ┌─────────────────────────────┐ │
│ │ 🔔 SMS Notifications      ✅│ │  ← Switch ListItem
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ ⚡ Quick Reply            ✅│ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│ STORAGE                         │
│ ┌─────────────────────────────┐ │
│ │ 🗑️ Trash Auto-Delete        │ │  ← ListItem with value
│ │    After 30 days         ›  │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 🚫 Blocked Numbers       ›  │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│ ABOUT                           │
│ ┌─────────────────────────────┐ │
│ │ ℹ️  Version 1.0.0           │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
│ 🏠 │ 🔍 │ ✏️ │ 👤 │ ⚙️ Settings│  ← NavigationBar
```

**Layout Details**:
- `LazyColumn` with `item` and `stickyHeader` for sections
- `ListItem` (M3) for each setting row
- `SegmentedButton` for theme selection (M3 Expressive component)
- `Slider` for font size
- `Switch` for boolean settings
- Section headers: `Text` with `MaterialTheme.typography.labelSmall` + tonal color

---

## Common Layout Patterns

### Scaffold Template
```kotlin
Scaffold(
    topBar = { /* TopAppBar */ },
    bottomBar = { /* NavigationBar (shared) */ },
    floatingActionButton = { /* FAB (Home only) */ },
    snackbarHost = { /* SnackbarHost */ }
) { paddingValues ->
    // Screen content with paddingValues
}
```

### Empty State Template
```kotlin
Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Image(painter = painterResource(R.drawable.ic_empty_state))
    Text("No messages yet", style = MaterialTheme.typography.titleLarge)
    Text("Start a conversation!", style = MaterialTheme.typography.bodyMedium)
    FilledButton("Compose Message") { /* navigate */ }
}
```
