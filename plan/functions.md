# ⚙️ Functions & API Surface

## SMS Application — Complete Function Reference

> All functions are written in Kotlin. UI functions use Jetpack Compose. Business logic functions use coroutines (suspend) and Flow.

---

## 1. Repository Layer

### SmsRepository (Interface)

```kotlin
interface SmsRepository {
    // Conversations (grouped by thread)
    fun getConversations(): Flow<List<Conversation>>
    fun getConversationById(threadId: Long): Flow<Conversation?>

    // Messages in a thread
    fun getMessages(threadId: Long): Flow<List<SmsMessage>>
    fun getMessageById(id: Long): Flow<SmsMessage?>

    // Send
    suspend fun sendSms(recipient: String, body: String): Result<Unit>
    suspend fun sendMultipartSms(recipient: String, parts: List<String>): Result<Unit>

    // CRUD
    suspend fun deleteMessage(id: Long): Result<Unit>
    suspend fun deleteConversation(threadId: Long): Result<Unit>
    suspend fun markAsRead(threadId: Long): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>

    // Draft
    suspend fun saveDraft(recipient: String, body: String): Result<Long>
    suspend fun deleteDraft(draftId: Long): Result<Unit>
    fun getDrafts(): Flow<List<SmsMessage>>

    // Search
    suspend fun searchMessages(query: String): List<SmsMessage>
    suspend fun searchWithFilters(
        query: String,
        dateRange: DateRange?,
        tagIds: List<Long>,
        folderId: Long?
    ): List<SmsMessage>

    // Filtering
    suspend fun filterByDateRange(from: Long, to: Long): List<Conversation>
    suspend fun filterByMonth(year: Int, month: Int): List<Conversation>
    suspend fun filterByYear(year: Int): List<Conversation>

    // Sorting
    suspend fun getSortedConversations(order: SortOrder): List<Conversation>

    // Archive / Trash
    suspend fun archiveConversation(threadId: Long): Result<Unit>
    suspend fun moveToTrash(messageId: Long): Result<Unit>
    suspend fun restoreFromTrash(messageId: Long): Result<Unit>
    suspend fun permanentlyDelete(messageId: Long): Result<Unit>
}
```

---

### ContactRepository (Interface)

```kotlin
interface ContactRepository {
    fun getAllContacts(): Flow<List<Contact>>
    suspend fun getContactByNumber(phoneNumber: String): Contact?
    suspend fun getContactById(contactId: Long): Contact?
    fun searchContacts(query: String): Flow<List<Contact>>
    suspend fun getContactAvatarUri(contactId: Long): Uri?
}
```

---

### TagRepository (Interface)

```kotlin
interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun createTag(name: String, color: Int): Long
    suspend fun updateTag(tag: Tag): Result<Unit>
    suspend fun deleteTag(tagId: Long): Result<Unit>
    suspend fun mergeTags(sourceId: Long, targetId: Long): Result<Unit>
    suspend fun assignTagToMessage(messageId: Long, tagId: Long): Result<Unit>
    suspend fun removeTagFromMessage(messageId: Long, tagId: Long): Result<Unit>
    fun getTagsForMessage(messageId: Long): Flow<List<Tag>>
    fun getMessagesForTag(tagId: Long): Flow<List<SmsMessage>>
}
```

---

### FolderRepository (Interface)

```kotlin
interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    fun getSystemFolders(): Flow<List<Folder>>
    fun getCustomFolders(): Flow<List<Folder>>
    suspend fun createFolder(name: String, iconRes: Int): Long
    suspend fun updateFolder(folder: Folder): Result<Unit>
    suspend fun deleteFolder(folderId: Long): Result<Unit>
    suspend fun movMessageToFolder(messageId: Long, folderId: Long): Result<Unit>
    fun getMessagesInFolder(folderId: Long): Flow<List<SmsMessage>>
    fun getConversationsByContact(): Flow<Map<Contact, List<SmsMessage>>>
    fun getConversationsByMonth(): Flow<Map<YearMonth, List<SmsMessage>>>
}
```

---

## 2. Use Cases

### GetConversationsUseCase

```kotlin
class GetConversationsUseCase(
    private val smsRepository: SmsRepository,
    private val contactRepository: ContactRepository
) {
    operator fun invoke(sortOrder: SortOrder = SortOrder.NEWEST_FIRST): Flow<List<Conversation>>
}
```

### SendSmsUseCase

```kotlin
class SendSmsUseCase(
    private val smsRepository: SmsRepository
) {
    // Automatically splits into multi-part if > 160 chars
    suspend operator fun invoke(
        recipient: String,
        body: String,
        onSent: (Boolean) -> Unit,
        onDelivered: (Boolean) -> Unit
    ): Result<Unit>
}
```

### SearchSmsUseCase

```kotlin
class SearchSmsUseCase(
    private val smsRepository: SmsRepository
) {
    suspend operator fun invoke(
        query: String,
        dateRange: DateRange? = null,
        tagIds: List<Long> = emptyList(),
        folderId: Long? = null,
        sortOrder: SortOrder = SortOrder.NEWEST_FIRST
    ): List<SmsMessage>
}
```

### FilterSmsByDateUseCase

```kotlin
class FilterSmsByDateUseCase(
    private val smsRepository: SmsRepository
) {
    suspend fun filterByDay(date: LocalDate): List<Conversation>
    suspend fun filterByMonth(year: Int, month: Month): List<Conversation>
    suspend fun filterByYear(year: Int): List<Conversation>
    suspend fun filterByRange(from: LocalDate, to: LocalDate): List<Conversation>
}
```

### SortSmsUseCase

```kotlin
class SortSmsUseCase {
    fun sortConversations(
        conversations: List<Conversation>,
        order: SortOrder
    ): List<Conversation>
}

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    CONTACT_NAME_ASC,
    CONTACT_NAME_DESC,
    UNREAD_COUNT_DESC
}
```

### TagSmsUseCase

```kotlin
class TagSmsUseCase(
    private val tagRepository: TagRepository
) {
    suspend fun addTag(messageId: Long, tagId: Long): Result<Unit>
    suspend fun removeTag(messageId: Long, tagId: Long): Result<Unit>
    suspend fun createAndAssignTag(
        messageId: Long,
        tagName: String,
        color: Int
    ): Result<Unit>
}
```

---

## 3. ViewModels

### HomeViewModel

```kotlin
class HomeViewModel(
    private val getConversationsUseCase: GetConversationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var sortOrder by mutableStateOf(SortOrder.NEWEST_FIRST)
        private set

    fun loadConversations()
    fun onSortOrderChanged(order: SortOrder)
    fun onSwipeToArchive(threadId: Long)
    fun onSwipeToDelete(threadId: Long)
    fun onMarkAllRead()
    fun onSearchQueryChanged(query: String)  // Quick search from HomeScreen
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val conversations: List<Conversation>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    object Empty : HomeUiState()
}
```

### ConversationViewModel

```kotlin
class ConversationViewModel(
    private val smsRepository: SmsRepository,
    private val contactRepository: ContactRepository,
    private val tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val threadId: Long = checkNotNull(savedStateHandle["threadId"])

    val messages: StateFlow<List<SmsMessage>>
    val contact: StateFlow<Contact?>
    val inputText: MutableStateFlow<String>

    fun sendMessage()
    fun deleteMessage(messageId: Long)
    fun copyMessageText(messageId: Long)
    fun forwardMessage(messageId: Long, recipient: String)
    fun addTagToMessage(messageId: Long, tagId: Long)
    fun removeTagFromMessage(messageId: Long, tagId: Long)
    fun starMessage(messageId: Long)
    fun onInputTextChanged(text: String)
}
```

### SearchViewModel

```kotlin
class SearchViewModel(
    private val searchSmsUseCase: SearchSmsUseCase,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    val query = MutableStateFlow("")
    val results: StateFlow<List<SmsMessage>>
    val searchHistory: StateFlow<List<String>>
    val activeFilters: StateFlow<SearchFilters>

    fun onQueryChanged(q: String)
    fun onDateRangeSelected(from: LocalDate, to: LocalDate)
    fun onTagFilterSelected(tagId: Long)
    fun onFolderFilterSelected(folderId: Long)
    fun onClearFilters()
    fun onSortOrderChanged(order: SortOrder)
    fun onHistoryItemClicked(query: String)
    fun onClearHistory()
}
```

### ComposeViewModel

```kotlin
class ComposeViewModel(
    private val sendSmsUseCase: SendSmsUseCase,
    private val contactRepository: ContactRepository
) : ViewModel() {

    val recipient = MutableStateFlow("")
    val messageBody = MutableStateFlow("")
    val characterCount: StateFlow<CharacterCount>
    val sendState: StateFlow<SendState>
    val contactSuggestions: StateFlow<List<Contact>>

    fun onRecipientChanged(value: String)
    fun onMessageBodyChanged(value: String)
    fun onSendClicked()
    fun onContactSelected(contact: Contact)
    fun onSaveDraft()
    fun clearForm()
}
```

---

## 4. Data Models

```kotlin
data class SmsMessage(
    val id: Long,
    val threadId: Long,
    val address: String,        // sender phone number
    val body: String,
    val timestamp: Long,        // epoch millis
    val type: MessageType,      // INBOX, SENT, DRAFT
    val isRead: Boolean,
    val status: MessageStatus,  // NONE, COMPLETE, PENDING, FAILED
    val contact: Contact? = null,
    val tags: List<Tag> = emptyList(),
    val folderId: Long? = null,
    val isStarred: Boolean = false,
    val isArchived: Boolean = false
)

data class Conversation(
    val threadId: Long,
    val contact: Contact?,
    val address: String,
    val lastMessage: SmsMessage,
    val messageCount: Int,
    val unreadCount: Int,
    val tags: List<Tag> = emptyList()
)

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumbers: List<String>,
    val avatarUri: Uri?,
    val initials: String   // derived from name
)

data class Tag(
    val id: Long,
    val name: String,
    val color: Int,         // Color int (ARGB)
    val messageCount: Int = 0
)

data class Folder(
    val id: Long,
    val name: String,
    val type: FolderType,   // SYSTEM, AUTO_CONTACT, AUTO_DATE, CUSTOM
    val iconRes: Int,
    val messageCount: Int = 0
)

enum class MessageType { INBOX, SENT, DRAFT, OUTBOX }
enum class MessageStatus { NONE, COMPLETE, PENDING, FAILED, DELIVERED }
enum class FolderType { SYSTEM, AUTO_CONTACT, AUTO_DATE, CUSTOM }

data class DateRange(
    val from: LocalDate,
    val to: LocalDate
)

data class CharacterCount(
    val current: Int,
    val perSms: Int = 160,
    val smsCount: Int,
    val remaining: Int
)
```

---

## 5. BroadcastReceiver Functions

### SmsReceiver

```kotlin
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent)

    private fun extractMessages(intent: Intent): List<SmsMessage>
    private fun parsePdu(pdu: ByteArray, format: String): SmsMessage
    private fun notifyIncomingMessage(context: Context, message: SmsMessage)
    private fun updateConversationInDb(context: Context, message: SmsMessage)
}
```

### SmsDeliveryReceiver

```kotlin
class SmsDeliveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent)
    private fun updateMessageStatus(messageId: Long, status: MessageStatus)
}
```

---

## 6. Permission Functions

```kotlin
object PermissionManager {
    val SMS_PERMISSIONS: Array<String>
    val CONTACT_PERMISSIONS: Array<String>
    val NOTIFICATION_PERMISSIONS: Array<String>   // API 33+

    fun hasAllSmsPermissions(context: Context): Boolean
    fun hasSendSmsPermission(context: Context): Boolean
    fun hasReceiveSmsPermission(context: Context): Boolean
    fun hasReadContactsPermission(context: Context): Boolean
    fun hasNotificationPermission(context: Context): Boolean
    fun isDefaultSmsApp(context: Context): Boolean
    fun openDefaultSmsSettings(context: Context)
    fun openAppSettings(context: Context)
}
```

---

## 7. Helper Functions

### SmsSenderHelper

```kotlin
object SmsSenderHelper {
    fun splitMessage(message: String): List<String>
    fun calculatePartCount(message: String): Int
    fun calculateRemainingChars(message: String): Pair<Int, Int>  // (remaining, partCount)

    suspend fun send(
        context: Context,
        recipient: String,
        body: String,
        sentIntent: PendingIntent,
        deliveryIntent: PendingIntent
    )
}
```

### DateFilterHelper

```kotlin
object DateFilterHelper {
    fun todayRange(): DateRange
    fun yesterdayRange(): DateRange
    fun thisWeekRange(): DateRange
    fun thisMonthRange(): DateRange
    fun thisYearRange(): DateRange
    fun monthRange(year: Int, month: Month): DateRange
    fun yearRange(year: Int): DateRange
    fun formatRelativeTime(timestamp: Long): String  // "2 min ago", "Yesterday", etc.
    fun formatSectionHeader(timestamp: Long): String // "Today", "Monday", "12 Oct 2024"
}
```

### ContactAvatarHelper

```kotlin
object ContactAvatarHelper {
    fun getInitials(name: String): String
    fun generateAvatarColor(seed: String): Color  // deterministic color from name
}
```

---

## 8. Compose UI Functions

### Screen Composables

```kotlin
@Composable fun HomeScreen(navController: NavController, viewModel: HomeViewModel)
@Composable fun ConversationScreen(threadId: Long, navController: NavController, viewModel: ConversationViewModel)
@Composable fun ComposeScreen(navController: NavController, viewModel: ComposeViewModel)
@Composable fun SearchScreen(navController: NavController, viewModel: SearchViewModel)
@Composable fun ContactsScreen(navController: NavController, viewModel: ContactsViewModel)
@Composable fun FoldersScreen(navController: NavController, viewModel: FoldersViewModel)
@Composable fun TagsScreen(navController: NavController, viewModel: TagsViewModel)
@Composable fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel)
@Composable fun PermissionsScreen(navController: NavController)
@Composable fun SplashScreen(navController: NavController)
```

### Reusable Components

```kotlin
@Composable fun ConversationCard(
    conversation: Conversation,
    onClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
)

@Composable fun MessageBubble(
    message: SmsMessage,
    onLongPress: () -> Unit
)

@Composable fun ContactAvatar(
    contact: Contact?,
    size: Dp = 48.dp
)

@Composable fun TagChip(
    tag: Tag,
    onRemove: (() -> Unit)? = null
)

@Composable fun FilterBottomSheet(
    onDateRangeSelected: (DateRange) -> Unit,
    onTagSelected: (Tag) -> Unit,
    onDismiss: () -> Unit
)

@Composable fun SortMenuDropdown(
    currentOrder: SortOrder,
    onOrderSelected: (SortOrder) -> Unit
)

@Composable fun SearchBarWithHistory(
    query: String,
    history: List<String>,
    onQueryChanged: (String) -> Unit,
    onHistorySelected: (String) -> Unit
)
```
