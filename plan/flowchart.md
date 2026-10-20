# 📊 SMS Application — Flowcharts & Navigation Diagrams

## 1. App Launch & Permission Flow

```mermaid
flowchart TD
    A([App Launch]) --> B{Permissions\nGranted?}
    B -- No --> C[PermissionsScreen]
    C --> D{User Grants\nAll Permissions?}
    D -- No --> E[Show Rationale\nDialog]
    E --> D
    D -- Yes --> F{Is Default\nSMS App?}
    B -- Yes --> F
    F -- No --> G[Show Default\nSMS App Dialog]
    G --> H{User Sets\nas Default?}
    H -- No --> I[Limited Mode\nRead-Only]
    H -- Yes --> J[HomeScreen]
    F -- Yes --> J
    I --> J
```

---

## 2. Main Navigation Graph

```mermaid
flowchart LR
    SPLASH([SplashScreen]) --> PERM[PermissionsScreen]
    PERM --> HOME

    HOME[🏠 HomeScreen] --> CONV[💬 ConversationScreen]
    HOME --> COMPOSE[✏️ ComposeScreen]
    HOME --> SEARCH[🔍 SearchScreen]
    HOME --> FOLDERS[📁 FoldersScreen]
    HOME --> CONTACTS[👤 ContactsScreen]
    HOME --> SETTINGS[⚙️ SettingsScreen]

    CONV --> COMPOSE
    CONV --> TAGS[🏷️ TagsScreen]
    CONTACTS --> CONV
    SEARCH --> CONV
    FOLDERS --> CONV
    TAGS --> CONV

    subgraph BottomNav["Bottom Navigation Bar"]
        HOME
        SEARCH
        COMPOSE
        CONTACTS
        SETTINGS
    end
```

---

## 3. SMS Send Flow

```mermaid
flowchart TD
    A([User taps Compose / Reply]) --> B[ComposeScreen / ConversationScreen]
    B --> C[User enters recipient + message]
    C --> D{Recipient\nvalid?}
    D -- No --> E[Show Error Snackbar]
    E --> C
    D -- Yes --> F{Message\nempty?}
    F -- Yes --> G[Disable Send Button]
    F -- No --> H[Enable Send Button]
    H --> I[User taps Send]
    I --> J{Is Default\nSMS App?}
    J -- No --> K[Open System SMS Chooser]
    J -- Yes --> L[SmsManager.sendTextMessage]
    L --> M{SMS > 160 chars?}
    M -- Yes --> N[sendMultipartTextMessage]
    M -- No --> O[Send Single Part]
    N --> P{Sent\nSuccessfully?}
    O --> P
    P -- Yes --> Q[Update UI - Sent Status]
    P -- No --> R[Show Retry Option]
    Q --> S[Refresh ConversationScreen]
```

---

## 4. SMS Receive Flow

```mermaid
flowchart TD
    A([Incoming SMS]) --> B[SmsReceiver BroadcastReceiver]
    B --> C[Parse PDU Data]
    C --> D[Extract sender + body + timestamp]
    D --> E{App in\nForeground?}
    E -- Yes --> F[Update ViewModel via Flow]
    F --> G[Refresh ConversationScreen in real-time]
    E -- No --> H[SmsNotificationService]
    H --> I[Show Notification with sender + preview]
    I --> J{User taps\nnotification?}
    J -- Yes --> K[Deep link → ConversationScreen]
    J -- No --> L[Notification remains in tray]
    G --> M[Save to Room DB metadata]
    K --> M
```

---

## 5. Search & Filter Flow

```mermaid
flowchart TD
    A([User opens Search]) --> B[SearchScreen]
    B --> C[SearchBar focused]
    C --> D[User types query]
    D --> E[SearchViewModel debounce 300ms]
    E --> F[SearchSmsUseCase]
    F --> G{Filter\nApplied?}
    G -- Date --> H[FilterSmsByDateUseCase\nday / month / year]
    G -- Tag --> I[Filter by Tag label]
    G -- Folder --> J[Filter by Folder type]
    G -- None --> K[Full text search in body + address]
    H --> L[Show filtered results]
    I --> L
    J --> L
    K --> L
    L --> M{Sort Order?}
    M -- Latest First --> N[Sort DESC by date]
    M -- Oldest First --> O[Sort ASC by date]
    N --> P[Display SmsListScreen]
    O --> P
```

---

## 6. Folder & Grouping Flow

```mermaid
flowchart TD
    A([User opens Folders]) --> B[FoldersScreen]
    B --> C{Folder Type}
    C --> D[📥 Inbox - All received]
    C --> E[📤 Sent - All sent]
    C --> F[📋 Drafts - Unsent]
    C --> G[🗑️ Trash - Deleted]
    C --> H[📦 Archived]
    C --> I[⭐ Starred / Tagged]
    C --> J[📅 Auto - by Date]
    C --> K[👥 Auto - by Contact]
    D --> L[ConversationList filtered]
    E --> L
    F --> L
    G --> L
    H --> L
    I --> L
    J --> L
    K --> L
    L --> M[User taps conversation]
    M --> N[ConversationScreen]
```

---

## 7. Tag Management Flow

```mermaid
flowchart TD
    A([User long-presses SMS]) --> B[Show Context Menu]
    B --> C{Action}
    C --> D[Add Tag]
    C --> E[Remove Tag]
    C --> F[Move to Folder]
    C --> G[Delete]
    C --> H[Star / Mark Important]
    D --> I[TagPickerBottomSheet]
    I --> J{Existing Tag\nor New?}
    J -- Existing --> K[Select from list]
    J -- New --> L[Create Tag dialog]
    L --> M[Enter tag name + color]
    M --> N[Save to Room DB]
    K --> O[Assign tag to message]
    N --> O
    O --> P[Update UI with TagChip]
```

---

## 8. Contact Grouping Flow

```mermaid
flowchart TD
    A([App loads / refresh]) --> B[GetConversationsUseCase]
    B --> C[Read SMS from ContentProvider]
    C --> D[For each thread - lookup contact]
    D --> E{Contact\nFound?}
    E -- Yes --> F[Use contact name + avatar]
    E -- No --> G[Use raw phone number]
    F --> H[Group messages by thread_id]
    G --> H
    H --> I[Sort by date DESC - latest first]
    I --> J[Display in HomeScreen list]
    J --> K{User changes\nsort order?}
    K -- Oldest First --> L[Sort ASC by date]
    K -- By Contact Name --> M[Sort by contact name A-Z]
    L --> N[Refresh list]
    M --> N
```

---

## 9. Settings Flow

```mermaid
flowchart TD
    A([User opens Settings]) --> B[SettingsScreen]
    B --> C{Setting Type}
    C --> D[Default SMS App]
    C --> E[Notification Settings]
    C --> F[Theme - Light / Dark / System]
    C --> G[Font Size]
    C --> H[Backup & Restore]
    C --> I[Block Numbers]
    C --> J[Auto-Archive Rules]
    D --> K[Open system default SMS chooser]
    E --> L[Notification channel settings]
    F --> M[Apply theme immediately]
    H --> N[Export SMS to JSON/CSV]
    I --> O[BlockListManager]
    J --> P[Configure auto-folder rules]
```

---

## 10. Full Screen State Machine

```mermaid
stateDiagram-v2
    [*] --> SplashScreen
    SplashScreen --> PermissionsScreen : permissions missing
    SplashScreen --> HomeScreen : all permissions granted
    PermissionsScreen --> HomeScreen : permissions granted
    HomeScreen --> ConversationScreen : tap conversation
    HomeScreen --> ComposeScreen : tap FAB/compose
    HomeScreen --> SearchScreen : tap search
    HomeScreen --> FoldersScreen : nav drawer
    HomeScreen --> ContactsScreen : bottom nav
    HomeScreen --> SettingsScreen : bottom nav / menu
    ConversationScreen --> ComposeScreen : forward message
    ConversationScreen --> TagsScreen : long press message
    ConversationScreen --> HomeScreen : back
    ComposeScreen --> ConversationScreen : message sent
    ComposeScreen --> HomeScreen : discard
    SearchScreen --> ConversationScreen : tap result
    FoldersScreen --> ConversationScreen : tap message
    TagsScreen --> ConversationScreen : back
    ContactsScreen --> ConversationScreen : tap contact
    SettingsScreen --> HomeScreen : back
```
