# 📱 SMS Application — Master Plan

> **Project**: SMSApplication  
> **Package**: `com.malekaltaf.smsapplication`  
> **Language**: Kotlin  
> **UI Framework**: Jetpack Compose  
> **Design Language**: Material 3 Expressive  
> **Min SDK**: 24 (Android 7.0) | **Target SDK**: 37  
> **Compile SDK**: 37 (AGP 9.4.1)

---

## 🎯 Project Goal

Build a **full-featured, production-quality SMS application** for Android that replaces the default SMS app. The app provides a rich, expressive messaging experience using Material 3 Expressive design principles, with advanced features like smart folder organization, contact grouping, tagging, date-based filtering, and deep search.

---

## 📋 Phase Overview

| Phase | Title | Description |
|-------|-------|-------------|
| **1** | Screen Design | Design all UI screens using Jetpack Compose with M3 Expressive |
| **2** | Navigation | Wire screens with Navigation Compose + bottom nav |
| **3** | Permissions | Runtime permission handling (SMS, Contacts, Default SMS) |
| **4** | Functionality | Implement business logic, ViewModels, Repository, Room DB |

---

## 🏗️ Architecture

```
MVVM + Clean Architecture
┌─────────────────────────────────────────────────────┐
│                    UI Layer                          │
│  Jetpack Compose Screens ↔ ViewModels (StateFlow)   │
├─────────────────────────────────────────────────────┤
│                  Domain Layer                        │
│  Use Cases  ·  Repository Interfaces  ·  Models     │
├─────────────────────────────────────────────────────┤
│                   Data Layer                         │
│  SmsContentProvider  ·  ContactsProvider             │
│  Room DB (tags, folders, labels, search history)    │
│  SmsReceiver (BroadcastReceiver)                    │
│  SmsSender (SmsManager API)                         │
└─────────────────────────────────────────────────────┘
```

---

## 📦 Module & Package Structure

```
app/src/main/java/com/malekaltaf/smsapplication/
├── data/
│   ├── local/
│   │   ├── SmsDatabase.kt              (Room DB)
│   │   ├── dao/
│   │   │   ├── TagDao.kt
│   │   │   ├── FolderDao.kt
│   │   │   └── SearchHistoryDao.kt
│   │   └── entity/
│   │       ├── TagEntity.kt
│   │       ├── FolderEntity.kt
│   │       └── SearchHistoryEntity.kt
│   ├── remote/
│   │   ├── SmsContentResolver.kt       (reads system SMS)
│   │   └── ContactsContentResolver.kt
│   └── repository/
│       ├── SmsRepositoryImpl.kt
│       └── ContactRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── SmsMessage.kt
│   │   ├── Conversation.kt
│   │   ├── Contact.kt
│   │   ├── Tag.kt
│   │   └── Folder.kt
│   ├── repository/
│   │   ├── SmsRepository.kt
│   │   └── ContactRepository.kt
│   └── usecase/
│       ├── GetConversationsUseCase.kt
│       ├── SendSmsUseCase.kt
│       ├── SearchSmsUseCase.kt
│       ├── FilterSmsByDateUseCase.kt
│       ├── TagSmsUseCase.kt
│       └── SortSmsUseCase.kt
├── ui/
│   ├── navigation/
│   │   ├── AppNavGraph.kt
│   │   └── Screen.kt                   (sealed class routes)
│   ├── screens/
│   │   ├── splash/
│   │   │   └── SplashScreen.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt           (conversation list)
│   │   │   └── HomeViewModel.kt
│   │   ├── conversation/
│   │   │   ├── ConversationScreen.kt   (chat thread)
│   │   │   └── ConversationViewModel.kt
│   │   ├── compose/
│   │   │   ├── ComposeScreen.kt        (new SMS)
│   │   │   └── ComposeViewModel.kt
│   │   ├── search/
│   │   │   ├── SearchScreen.kt
│   │   │   └── SearchViewModel.kt
│   │   ├── contacts/
│   │   │   ├── ContactsScreen.kt
│   │   │   └── ContactsViewModel.kt
│   │   ├── folders/
│   │   │   ├── FoldersScreen.kt
│   │   │   └── FoldersViewModel.kt
│   │   ├── tags/
│   │   │   ├── TagsScreen.kt
│   │   │   └── TagsViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   └── SettingsViewModel.kt
│   │   └── permissions/
│   │       └── PermissionsScreen.kt
│   ├── components/
│   │   ├── ConversationCard.kt
│   │   ├── MessageBubble.kt
│   │   ├── ContactAvatar.kt
│   │   ├── SearchBar.kt
│   │   ├── TagChip.kt
│   │   ├── FilterBottomSheet.kt
│   │   ├── SortMenu.kt
│   │   └── FolderCard.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       ├── Type.kt
│       ├── Shape.kt
│       └── Motion.kt
├── service/
│   ├── SmsReceiver.kt                  (BroadcastReceiver)
│   └── SmsNotificationService.kt
├── permission/
│   └── PermissionManager.kt
├── di/
│   └── AppModule.kt                    (Hilt/Manual DI)
└── MainActivity.kt
```

---

## 🗓️ Implementation Timeline

| Week | Phase | Tasks |
|------|-------|-------|
| 1 | Phase 1 – Screen Design | Design all 9 screens (Splash, Home, Conversation, Compose, Search, Contacts, Folders, Tags, Settings) |
| 2 | Phase 2 – Navigation | NavGraph setup, BottomNavBar, screen transitions |
| 3 | Phase 3 – Permissions | Runtime permission flow, Default SMS dialog |
| 4–5 | Phase 4 – Core Data | Room DB, ContentResolver for SMS + Contacts |
| 6–7 | Phase 4 – ViewModels | ViewModels, UseCases, StateFlow wiring |
| 8 | Phase 4 – Features | Search, Filter, Sort, Tags, Folders |
| 9 | Testing & Polish | Unit tests, UI tests, performance tuning |

---

## 📎 Planning Documents Index

| File | Description |
|------|-------------|
| [`features.md`](features.md) | Full feature list with acceptance criteria |
| [`functions.md`](functions.md) | All functions / API surface area |
| [`screenlayout_design.md`](screenlayout_design.md) | Screen-by-screen UI layout descriptions |
| [`permissions.md`](permissions.md) | Android permissions strategy |
| [`flowchart.md`](flowchart.md) | App flow and navigation diagrams |
| [`agents_work_assignment.md`](agents_work_assignment.md) | Agent task assignments |
| [`designlanguage.md`](designlanguage.md) | Material 3 Expressive design system |

---

## ✅ Verification Plan

### Automated Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

### Manual Verification
1. Install on device/emulator (API 24+)
2. Grant all permissions on first launch
3. Set as default SMS app
4. Send/receive SMS from another number
5. Verify conversation grouping, tags, folders
6. Test search with keyword + date filter
7. Verify sorting (newest/oldest)
8. Test dark mode toggle in Settings

---

## 🔑 Key Dependencies to Add

```toml
# libs.versions.toml additions
navigation-compose = "2.8.4"
hilt = "2.52"
hilt-navigation-compose = "1.2.0"
room = "2.6.1"
lifecycle-viewmodel-compose = "2.8.7"
coroutines = "1.9.0"
```

```kotlin
// Additional dependencies in app/build.gradle.kts
implementation(libs.navigation.compose)
implementation(libs.hilt.android)
kapt(libs.hilt.compiler)
implementation(libs.hilt.navigation.compose)
implementation(libs.room.runtime)
implementation(libs.room.ktx)
kapt(libs.room.compiler)
implementation(libs.lifecycle.viewmodel.compose)
implementation(libs.kotlinx.coroutines.android)
```
