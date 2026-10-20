# 🔐 Permissions Strategy

## SMS Application — Android Permissions Plan

---

## Permission Overview

| Permission | Type | Required For | API Level |
|-----------|------|-------------|-----------|
| `READ_SMS` | Dangerous | Read inbox/sent messages | All |
| `SEND_SMS` | Dangerous | Send messages | All |
| `RECEIVE_SMS` | Dangerous | Receive incoming SMS | All |
| `RECEIVE_MMS` | Dangerous | Receive MMS (default app) | All |
| `READ_CONTACTS` | Dangerous | Show contact names/avatars | All |
| `READ_PHONE_STATE` | Dangerous | SIM state, phone number | All |
| `POST_NOTIFICATIONS` | Dangerous | Show notification | API 33+ |
| `RECEIVE_BOOT_COMPLETED` | Normal | Restart receiver on boot | All |
| `VIBRATE` | Normal | Notification vibration | All |
| `WAKE_LOCK` | Normal | Process SMS in background | All |

---

## 1. Permission Groups

### Critical Permissions (App non-functional without these)
```kotlin
val CRITICAL_SMS_PERMISSIONS = arrayOf(
    Manifest.permission.READ_SMS,
    Manifest.permission.SEND_SMS,
    Manifest.permission.RECEIVE_SMS
)
```

### Important Permissions (Degraded experience without these)
```kotlin
val IMPORTANT_PERMISSIONS = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.READ_PHONE_STATE
)
```

### Conditional Permissions (API-level gated)
```kotlin
// Only request on API 33+
val NOTIFICATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
} else {
    emptyArray()
}
```

---

## 2. Default SMS App Requirements

To function as the **default SMS application**, the app must:

### AndroidManifest.xml Intent Filters Required

```xml
<!-- SMS Receiver -->
<receiver
    android:name=".service.SmsReceiver"
    android:permission="android.permission.BROADCAST_SMS"
    android:exported="true">
    <intent-filter android:priority="1000">
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>

<!-- MMS Receiver -->
<receiver
    android:name=".service.MmsReceiver"
    android:permission="android.permission.BROADCAST_WAP_PUSH"
    android:exported="true">
    <intent-filter>
        <action android:name="android.provider.Telephony.WAP_PUSH_DELIVER" />
        <data android:mimeType="application/vnd.wap.mms-message" />
    </intent-filter>
</receiver>

<!-- HeadlessSmsSendService (required for default SMS app) -->
<service
    android:name=".service.HeadlessSmsSendService"
    android:permission="android.permission.SEND_RESPOND_VIA_MESSAGE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.RESPOND_VIA_MESSAGE" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:scheme="sms" />
        <data android:scheme="smsto" />
        <data android:scheme="mms" />
        <data android:scheme="mmsto" />
    </intent-filter>
</service>

<!-- Main Activity SMS handling -->
<activity android:name=".MainActivity" ...>
    <!-- Existing MAIN + LAUNCHER intent filters -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    <!-- Handle sms: URIs (deep link from notifications etc.) -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="sms" />
        <data android:scheme="smsto" />
    </intent-filter>
    <!-- Handle SEND action for default SMS app -->
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

### Setting as Default SMS App

```kotlin
// For API 29+ (Android 10+) use RoleManager
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    val roleManager = context.getSystemService(RoleManager::class.java)
    if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
        if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            startActivityForResult(intent, REQUEST_CODE_DEFAULT_SMS)
        }
    }
} else {
    // For API < 29 use Telephony.Sms.getDefaultSmsPackage
    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
        putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
    }
    startActivity(intent)
}
```

---

## 3. Runtime Permission Flow

```
App Launch
    │
    ▼
Check Permissions (PermissionManager.hasAllSmsPermissions)
    │
    ├── ALL GRANTED ──────────────────────→ HomeScreen
    │
    └── MISSING ──────────────────────────→ PermissionsScreen
                                                │
                                                ▼
                                           Request Permissions
                                                │
                                     ┌──────────┴──────────┐
                                     │                     │
                                   GRANTED               DENIED
                                     │                     │
                                     ▼                     ▼
                               Check "Never          Show Rationale
                               Ask Again"?           Dialog
                                     │                     │
                              ┌──────┴──────┐             ▼
                            YES            NO         Re-request
                              │            │
                              ▼            ▼
                        Open App      Continue with
                        Settings      limited mode
```

---

## 4. PermissionManager Implementation

```kotlin
object PermissionManager {

    val SMS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS
    )

    val CONTACT_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val NOTIFICATION_PERMISSIONS = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    fun hasAllSmsPermissions(context: Context): Boolean =
        SMS_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    fun hasReadContactsPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

    fun hasNotificationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    fun isDefaultSmsApp(context: Context): Boolean =
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

    fun openDefaultSmsSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            activity.startActivityForResult(intent, 0)
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
            }
            activity.startActivity(intent)
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}
```

---

## 5. Permissions Screen State Machine

```kotlin
data class PermissionsUiState(
    val readSmsGranted: Boolean = false,
    val sendSmsGranted: Boolean = false,
    val receiveSmsGranted: Boolean = false,
    val readContactsGranted: Boolean = false,
    val notificationsGranted: Boolean = false,
    val isDefaultSmsApp: Boolean = false,
    val showRationale: Boolean = false,
    val showNeverAskDialog: Boolean = false
) {
    val allCriticalGranted: Boolean
        get() = readSmsGranted && sendSmsGranted && receiveSmsGranted

    val canProceed: Boolean
        get() = allCriticalGranted
}
```

---

## 6. Graceful Degradation

| Missing Permission | App Behavior |
|-------------------|-------------|
| `READ_SMS` | Show "Permission needed" empty state, cannot read messages |
| `SEND_SMS` | Hide compose button, show tooltip explaining limitation |
| `RECEIVE_SMS` | Show banner "Not set as default - can't receive SMS" |
| `READ_CONTACTS` | Show phone numbers only (no names or avatars) |
| `POST_NOTIFICATIONS` | No notification for incoming SMS; still functional in app |
| Not default SMS app | Read-only mode; can still read but not receive new SMS |

---

## 7. Permission Persistence

- Permission state stored in `PermissionsViewModel` (backed by `DataStore`)
- Checked on every `onResume()` of MainActivity (user might revoke from Settings)
- If critical permission revoked: redirect to `PermissionsScreen`

```kotlin
// In MainActivity.kt
override fun onResume() {
    super.onResume()
    if (!PermissionManager.hasAllSmsPermissions(this)) {
        navController.navigate(Screen.Permissions.route) {
            popUpTo(navController.graph.startDestinationId)
        }
    }
}
```

---

## 8. Complete Manifest Permissions Block

```xml
<!-- SMS Core Permissions -->
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.RECEIVE_MMS" />

<!-- Contact Permissions -->
<uses-permission android:name="android.permission.READ_CONTACTS" />

<!-- Phone State -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />

<!-- Notification (API 33+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- System -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />

<!-- Default SMS App (system-level) -->
<uses-permission android:name="android.permission.BROADCAST_SMS" />
<uses-permission android:name="android.permission.BROADCAST_WAP_PUSH" />
<uses-permission android:name="android.permission.SEND_RESPOND_VIA_MESSAGE"
    tools:ignore="ProtectedPermissions" />
```
