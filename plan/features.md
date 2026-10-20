# ✨ Features Specification

## SMS Application — Complete Feature List with Acceptance Criteria

---

## Feature Categories

| # | Category | Features |
|---|----------|----------|
| 1 | Core SMS | Read, Send, Receive SMS |
| 2 | Contacts | Read contacts, contact-linked conversations |
| 3 | Default App | Set as default SMS application |
| 4 | Search | Full-text search with filters |
| 5 | Sorting | Sort by date, contact, unread |
| 6 | Folders | Auto/Manual folder organization |
| 7 | Tags | Custom tag labeling system |
| 8 | Filtering | Date, month, year, tag-based filters |
| 9 | UI/UX | Material 3 Expressive, dark mode, animations |
| 10 | Notifications | Incoming SMS notifications with quick reply |

---

## 1. Core SMS Features

### F-001: Read SMS
- **Description**: Display all SMS messages from the system SMS content provider
- **Acceptance Criteria**:
  - All inbox messages displayed in conversation (thread) view
  - Messages grouped by sender (thread_id)
  - Each conversation card shows: contact name/number, message preview (truncated to 1 line), timestamp, unread count badge
  - Unread messages shown in **bold**
  - Real-time updates when new messages arrive

### F-002: Send SMS
- **Description**: Compose and send SMS to any phone number or contact
- **Acceptance Criteria**:
  - User can pick recipient from contacts or enter number manually
  - Character counter shown (160 chars for single SMS, indicator for multi-part)
  - Send button disabled when message body is empty
  - Message status shown: Sending → Sent → Delivered → Failed
  - Retry option for failed messages
  - Multi-part SMS (>160 chars) handled automatically

### F-003: Receive SMS
- **Description**: App receives incoming SMS when set as default
- **Acceptance Criteria**:
  - `SmsReceiver` BroadcastReceiver handles `SMS_RECEIVED` intent
  - Incoming message immediately appears in conversation thread
  - Heads-up notification shown when app is in background
  - Sound/vibration on notification (respects system settings)
  - Quick reply from notification shade

---

## 2. Contacts Features

### F-004: Read Contacts
- **Description**: Read device contacts to display names and avatars
- **Acceptance Criteria**:
  - Contact name displayed instead of phone number when available
  - Contact avatar shown in conversation card and thread header
  - If no contact exists, show initials avatar with random Material color
  - Contacts list screen shows all contacts with at least one SMS thread

### F-005: Contact-Linked Conversations
- **Description**: Conversations linked to contact profiles
- **Acceptance Criteria**:
  - Tapping contact avatar in ConversationScreen shows contact details
  - Multiple numbers for same contact grouped under one conversation (if same thread_id)
  - Contacts search in `ContactsScreen` filters by name

---

## 3. Default SMS App

### F-006: Set as Default SMS App
- **Description**: Prompt user to set app as the default SMS application
- **Acceptance Criteria**:
  - On first launch, show default SMS app dialog
  - App registers correct intent filters for SMS/MMS receiving
  - When set as default, full send/receive functionality is unlocked
  - When NOT default, app works in read-only mode with a banner explaining limitation
  - "Set as Default" button in Settings screen

---

## 4. Search

### F-007: Full-Text SMS Search
- **Description**: Search across all SMS message bodies and sender addresses
- **Acceptance Criteria**:
  - Search bar always accessible from HomeScreen top bar
  - Results update as user types (debounce 300ms)
  - Search highlights matched text in results
  - Recent search history shown when search bar is focused (empty query)
  - Tapping a result opens the ConversationScreen scrolled to the matching message

### F-008: Search with Filters
- **Description**: Combine text search with date, tag, or folder filters
- **Acceptance Criteria**:
  - Filter chips row below search bar: **Date Range**, **Tag**, **Folder**, **Unread**
  - Filters can be combined (e.g., search "bill" in "Work" tag within "November 2024")
  - Active filters shown as dismissible chips
  - "Clear All" button resets all filters

---

## 5. Sorting

### F-009: Sort SMS Conversations
- **Description**: Sort conversation list by multiple criteria
- **Acceptance Criteria**:
  - Sort options: **Newest First** (default), **Oldest First**, **Contact Name A→Z**, **Contact Name Z→A**, **Unread Count (High→Low)**
  - Sort selection persists across app restarts (DataStore)
  - Sort icon in HomeScreen top bar, opens bottom sheet with options
  - Sort applied instantly (no page reload)

### F-010: Sort Messages within Conversation
- **Description**: Messages in a thread always sorted chronologically
- **Acceptance Criteria**:
  - Messages sorted ASC by timestamp (oldest at top, newest at bottom)
  - Scroll position remembers last position per conversation
  - "Jump to latest" FAB appears when scrolled up

---

## 6. Folders

### F-011: System Folders
- **Description**: Standard SMS folder types
- **Acceptance Criteria**:
  - **Inbox**: All received SMS
  - **Sent**: All sent SMS
  - **Drafts**: Unsent composed messages (auto-saved)
  - **Trash**: Deleted messages (30-day retention before permanent delete)
  - **Archived**: Messages moved out of inbox

### F-012: Auto-Folder — Group by Contact
- **Description**: Automatically create a folder per contact
- **Acceptance Criteria**:
  - Each contact with SMS history has an auto-generated folder
  - Folder shows contact name, avatar, message count
  - Sorted by most recently active contact

### F-013: Auto-Folder — Group by Date
- **Description**: Group conversations by month/year buckets
- **Acceptance Criteria**:
  - Folders created dynamically: "October 2024", "September 2024", etc.
  - Each date folder shows message count
  - Folders for years: "2024", "2023" as a roll-up view

### F-014: Manual Custom Folders
- **Description**: User-defined folders with custom names
- **Acceptance Criteria**:
  - User can create, rename, delete custom folders
  - Messages can be moved to any manual folder via long-press context menu
  - Folder icon customizable (from preset icon set)

---

## 7. Tags

### F-015: Tag SMS Messages
- **Description**: Apply custom tags/labels to individual messages or conversations
- **Acceptance Criteria**:
  - Long-press on message → "Add Tag" option in context menu
  - Tag picker bottom sheet with existing tags + "Create New" option
  - Tag shown as a colored chip on the message bubble
  - Multiple tags per message supported

### F-016: Tag Management
- **Description**: CRUD operations on tags
- **Acceptance Criteria**:
  - Tags screen shows all tags with name, color, message count
  - Create tag: name + color picker (from M3 Expressive palette)
  - Edit tag name or color
  - Delete tag (removes from all messages)
  - Merge two tags into one

### F-017: Filter by Tag
- **Description**: View all messages/conversations with a specific tag
- **Acceptance Criteria**:
  - Tapping a tag in TagsScreen shows all tagged messages
  - Tag filter chip in SearchScreen filters results

---

## 8. Date/Time Filtering

### F-018: Filter SMS by Date
- **Description**: Filter messages by specific date, month, or year
- **Acceptance Criteria**:
  - Date filter options: **Today**, **Yesterday**, **This Week**, **This Month**, **Custom Range**
  - Month picker: select any month from scrollable month list
  - Year filter: select year from list
  - Date range: from/to date picker with M3 DateRangePicker
  - Filters combine with text search and tags

### F-019: Date Group Headers in Lists
- **Description**: Messages in conversation view grouped by date
- **Acceptance Criteria**:
  - Sticky date headers in ConversationScreen: "Today", "Yesterday", "Monday", "12 Oct 2024"
  - Conversation list in HomeScreen shows relative time: "2 min ago", "Yesterday", "Mon", "12 Oct"

---

## 9. UI/UX Features

### F-020: Material 3 Expressive Design
- **Description**: Full M3 Expressive implementation
- **Acceptance Criteria**:
  - Dynamic color from wallpaper (Android 12+)
  - Spring-based motion for transitions
  - Shape morphing on interactive elements
  - Expanded shape library used throughout

### F-021: Dark Mode
- **Description**: Full dark mode support
- **Acceptance Criteria**:
  - Follows system setting by default
  - Manual override in Settings (Light / Dark / System)
  - All screens render correctly in dark mode
  - No hard-coded colors (all from MaterialTheme.colorScheme)

### F-022: Swipe Gestures
- **Description**: Swipe actions on conversation cards
- **Acceptance Criteria**:
  - Swipe right: Mark as read/unread
  - Swipe left: Archive or Delete (configurable in Settings)
  - Swipe reveal shows action icon + color (green for read, red for delete)

---

## 10. Notification Features

### F-023: Incoming SMS Notification
- **Description**: Rich notification for incoming SMS
- **Acceptance Criteria**:
  - Heads-up notification with sender name/number and message preview
  - Quick reply action in notification (Android 7+)
  - "Mark as Read" action button in notification
  - Grouped notifications for multiple messages from same sender
  - Notification taps deep-link to correct ConversationScreen

---

## Priority Matrix

| Feature | Priority | Effort | Phase |
|---------|----------|--------|-------|
| F-001 Read SMS | P0 🔴 | Medium | 4 |
| F-002 Send SMS | P0 🔴 | Medium | 4 |
| F-003 Receive SMS | P0 🔴 | High | 4 |
| F-006 Default SMS App | P0 🔴 | Medium | 3 |
| F-004 Read Contacts | P1 🟠 | Low | 4 |
| F-007 Search | P1 🟠 | High | 4 |
| F-009 Sort | P1 🟠 | Low | 4 |
| F-011 System Folders | P1 🟠 | Medium | 4 |
| F-015 Tag SMS | P2 🟡 | Medium | 4 |
| F-012 Auto-Folder Contact | P2 🟡 | Medium | 4 |
| F-018 Date Filter | P2 🟡 | Medium | 4 |
| F-020 M3 Expressive | P1 🟠 | High | 1 |
| F-021 Dark Mode | P1 🟠 | Low | 1 |
| F-022 Swipe Gestures | P2 🟡 | Medium | 4 |
| F-023 Notifications | P1 🟠 | Medium | 4 |
| F-013 Auto-Folder Date | P3 🟢 | Low | 4 |
| F-014 Manual Folders | P3 🟢 | Medium | 4 |
