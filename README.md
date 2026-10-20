# SMS Application

A native Android SMS app built with **Material 3 Expressive** design. It helps you keep your messages organized with **custom folders**, **tags**, and **fast search**, so bank alerts, OTPs, and everyday conversations never get buried in one long list.

---

## Features

### Material 3 Expressive UI
- Dark theme with tonal surfaces, large rounded cards, and pill-shaped chips
- Expressive shapes and typography for a modern, polished feel
- Floating action button for starting new messages
- Smooth, touch-friendly layout designed for one-handed use

### Folder Support
- Filter conversations using folder chips at the top: **All**, custom folders (e.g. *Kotak Bank*), and **+ Folder**
- Create a new folder from the **+ Folder** chip through a simple "New Folder" dialog
- Quickly switch between folders to see only the conversations that belong there

### SMS Search
- Persistent search bar at the bottom of the screen, easy to reach with your thumb
- Search across all messages on the main list (**Search messages...**)
- Search inside a single conversation (**Search conversation...**)
- Clear the query with a single tap on the **X** button

### SMS Tagging
- Conversations are tagged with labels (for example, a **Kotak Bank** tag shown on the message card)
- Tags make it easy to recognize and group messages from the same sender or category
- Tags work together with folders for flexible organization

### Messaging
- Conversation list showing sender, message count, date, and a preview of the latest message
- Full conversation view with date separators and timestamps
- Start a new conversation by entering a phone number from the **New Message** dialog
- Senders that don't accept replies (such as bank sender IDs) are clearly marked: *"This sender does not allow replying messages."*
- **Set as Default** button in the top bar to make the app your default SMS handler

---

## Screenshots

<table>
  <tr>
    <td align="center"><b>Main Screen</b><br><img src="SMS%20Application/main_screen.jpg" width="250" alt="Main screen"></td>
    <td align="center"><b>Create Folder</b><br><img src="SMS%20Application/create_sms_folder.jpg" width="250" alt="Create folder"></td>
    <td align="center"><b>Folder View</b><br><img src="SMS%20Application/sms_folder_view.jpg" width="250" alt="Folder view"></td>
  </tr>
  <tr>
    <td align="center"><b>Search</b><br><img src="SMS%20Application/sms_search_feature.jpg" width="250" alt="Search feature"></td>
    <td align="center"><b>Conversation View</b><br><img src="SMS%20Application/sms_conversation_view.jpg" width="250" alt="Conversation view"></td>
    <td align="center"><b>New Message</b><br><img src="SMS%20Application/send_new_sms.jpg" width="250" alt="Send new SMS"></td>
  </tr>
</table>

| Screenshot | Description |
|---|---|
| `main_screen.jpg` | Browse all conversations with folder chips and search bar |
| `create_sms_folder.jpg` | Create a custom folder from the **+ Folder** chip |
| `sms_folder_view.jpg` | Show only the conversations that belong to a folder |
| `sms_search_feature.jpg` | Search messages by keyword |
| `sms_conversation_view.jpg` | Read a thread with date grouping and timestamps |
| `send_new_sms.jpg` | Start a new conversation by phone number |

---

## Tech Stack

- **Platform:** Android (native)
- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3 (Expressive)
- **Architecture:** MVVM
- **Data:** Android Telephony / SMS Content Provider, plus a local database (Room) for folders and tags

---

## Permissions

The app requires the following permissions to work as an SMS client:

| Permission | Purpose |
|---|---|
| `READ_SMS` | Read existing messages |
| `RECEIVE_SMS` | Receive incoming messages |
| `SEND_SMS` | Send messages |
| `READ_CONTACTS` | Show contact names (optional) |

To send and receive messages fully, tap **Set as Default** and grant default SMS app status.

---

## Getting Started

### Prerequisites
- Android Studio (latest stable version)
- Android SDK 26 or higher
- A physical device or emulator with SMS support

### Build and Run

```bash
# Clone the repository
git clone https://github.com/<your-username>/sms-application.git
cd sms-application

# Open in Android Studio, or build from the command line
./gradlew assembleDebug
```

Install the generated APK from `app/build/outputs/apk/debug/` on your device, then open the app and grant the requested permissions.

---

## Usage

1. **Set as default:** Tap **Set as Default** in the top bar.
2. **Create a folder:** Tap **+ Folder**, enter a name (e.g. `Bank`), and press **Create**.
3. **Tag and organize:** Assign tags to conversations so they appear in the matching folder.
4. **Search:** Type in the bottom search bar to filter messages instantly.
5. **New message:** Tap the **+** button, enter a phone number, and press **Start**.

---

## Project Structure

```
app/
├── ui/            # Compose screens, components, Material 3 theme
├── data/          # SMS provider access, Room database (folders, tags)
├── viewmodel/     # State holders for list, folders, search
├── receiver/      # Incoming SMS broadcast receivers
└── MainActivity.kt
```

---

## Roadmap

- [ ] Auto-tagging rules by sender (e.g. all `KOTAKB` senders go to *Kotak Bank*)
- [ ] Rename and delete folders
- [ ] Light theme and dynamic color support
- [ ] Message backup and restore
- [ ] Spam and promotional message filtering

---

## Contributing

Contributions are welcome. Fork the repo, create a feature branch, and open a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.