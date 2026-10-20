package com.malekaltaf.smsapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malekaltaf.smsapplication.ui.theme.SMSApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SmsFolder(val id: String, val name: String, val senders: Set<String>)

fun serializeFolders(folders: List<SmsFolder>): String {
    return folders.joinToString(";;") { folder ->
        "${folder.id}|${folder.name}|${folder.senders.joinToString(",")}"
    }
}

fun deserializeFolders(data: String): List<SmsFolder> {
    if (data.isBlank() || data == "[]") return emptyList()
    return data.split(";;").mapNotNull {
        val parts = it.split("|")
        if (parts.size >= 3) {
            SmsFolder(parts[0], parts[1], parts[2].split(",").filter { it.isNotBlank() }.toSet())
        } else null
    }
}

@Composable
fun SmsListScreen(
    viewModel: SmsViewModel = viewModel(),
    onRequestPermission: () -> Unit,
    onRequestDefaultSms: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()
    
    val context = LocalContext.current
    
    // Periodically recheck if we are the default SMS app
    var isDefaultSms by remember { mutableStateOf(context.packageName == android.provider.Telephony.Sms.getDefaultSmsPackage(context)) }
    LaunchedEffect(Unit) {
        while(true) {
            isDefaultSms = context.packageName == android.provider.Telephony.Sms.getDefaultSmsPackage(context)
            kotlinx.coroutines.delay(1000)
        }
    }

    val prefs = remember { context.getSharedPreferences("sms_folders", Context.MODE_PRIVATE) }
    var folders by remember {
        mutableStateOf(deserializeFolders(prefs.getString("folders", "") ?: ""))
    }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var deletedSenders by remember { 
        mutableStateOf(prefs.getStringSet("deleted_senders", emptySet()) ?: emptySet()) 
    }

    LaunchedEffect(folders) {
        prefs.edit().putString("folders", serializeFolders(folders)).apply()
    }
    
    LaunchedEffect(deletedSenders) {
        prefs.edit().putStringSet("deleted_senders", deletedSenders).apply()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var showNewSmsDialog by remember { mutableStateOf(false) }

    val filteredMessages = remember(messages, searchQuery, selectedFolderId, folders, deletedSenders) {
        val base = if (searchQuery.isBlank()) messages else messages.filter {
            it.address.contains(searchQuery, ignoreCase = true) ||
            it.body.contains(searchQuery, ignoreCase = true)
        }
        
        val withoutDeleted = base.filter { !deletedSenders.contains(it.address) }

        if (selectedFolderId == null) {
            withoutDeleted
        } else {
            val folderSenders = folders.find { it.id == selectedFolderId }?.senders ?: emptySet()
            withoutDeleted.filter { folderSenders.contains(it.address) }
        }
    }

    if (showNewSmsDialog) {
        var newNumber by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewSmsDialog = false },
            title = { Text("New Message") },
            text = {
                OutlinedTextField(
                    value = newNumber,
                    onValueChange = { newNumber = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newNumber.isNotBlank()) {
                        selectedAddress = newNumber
                        showNewSmsDialog = false
                    }
                }) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSmsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedAddress == null) {
        SmsListScreenContent(
            messages = filteredMessages,
            isLoading = isLoading,
            hasPermission = hasPermission,
            isDefaultSms = isDefaultSms,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onRequestPermission = onRequestPermission,
            onRequestDefaultSms = onRequestDefaultSms,
            folders = folders,
            selectedFolderId = selectedFolderId,
            onFolderSelected = { selectedFolderId = it },
            onFoldersChange = { folders = it },
            onThreadDelete = { addr ->
                if (isDefaultSms) {
                    try {
                        val rowsDeleted = context.contentResolver.delete(
                            Uri.parse("content://sms"),
                            "address=?",
                            arrayOf(addr)
                        )
                        if (rowsDeleted > 0) {
                            Toast.makeText(context, "Thread deleted permanently.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Fallback if content resolver fails even when default
                            deletedSenders = deletedSenders + addr
                        }
                    } catch (_: Exception) {
                        deletedSenders = deletedSenders + addr
                    }
                } else {
                    deletedSenders = deletedSenders + addr
                    Toast.makeText(context, "Hidden locally. Set as Default SMS app to delete from device.", Toast.LENGTH_LONG).show()
                }
            },
            onThreadClick = { selectedAddress = it },
            onNewSmsClick = { showNewSmsDialog = true }
        )
    } else {
        BackHandler {
            selectedAddress = null
        }
        val threadMessages = messages.filter { it.address == selectedAddress }.sortedBy { it.date }
        SmsConversationScreen(
            address = selectedAddress!!,
            messages = threadMessages,
            onBack = { selectedAddress = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmsListScreenContent(
    messages: List<SmsMessage>,
    isLoading: Boolean,
    hasPermission: Boolean,
    isDefaultSms: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onRequestDefaultSms: () -> Unit,
    folders: List<SmsFolder>,
    selectedFolderId: String?,
    onFolderSelected: (String?) -> Unit,
    onFoldersChange: (List<SmsFolder>) -> Unit,
    onThreadDelete: (String) -> Unit,
    onThreadClick: (String) -> Unit,
    onNewSmsClick: () -> Unit
) {
    val listState = rememberLazyListState()

    var threadOptionsAddress by remember { mutableStateOf<String?>(null) }
    var showMoveToFolder by remember { mutableStateOf<String?>(null) }
    var showCreateFolder by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<SmsFolder?>(null) }
    var threadToDelete by remember { mutableStateOf<String?>(null) }

    if (threadOptionsAddress != null) {
        AlertDialog(
            onDismissRequest = { threadOptionsAddress = null },
            title = { Text("Options") },
            text = {
                Column {
                    TextButton(onClick = { 
                        threadToDelete = threadOptionsAddress
                        threadOptionsAddress = null
                    }) { Text("Delete Thread", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { 
                        showMoveToFolder = threadOptionsAddress
                        threadOptionsAddress = null
                    }) { Text("Move to Folder") }
                }
            },
            confirmButton = {
                TextButton(onClick = { threadOptionsAddress = null }) { Text("Cancel") }
            }
        )
    }

    if (threadToDelete != null) {
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            title = { Text("Delete Thread") },
            text = { Text("Are you sure you want to delete this thread? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onThreadDelete(threadToDelete!!)
                        threadToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { threadToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showMoveToFolder != null) {
        AlertDialog(
            onDismissRequest = { showMoveToFolder = null },
            title = { Text("Select Folder") },
            text = {
                LazyColumn {
                    items(folders) { folder ->
                        val isCurrentlyInFolder = folder.senders.contains(showMoveToFolder!!)
                        TextButton(onClick = {
                            val newFolders = folders.map { 
                                if (it.id == folder.id) {
                                    // Toggle logic: if it's currently in the folder, remove it. Otherwise, add it.
                                    if (isCurrentlyInFolder) it.copy(senders = it.senders - showMoveToFolder!!)
                                    else it.copy(senders = it.senders + showMoveToFolder!!)
                                } else {
                                    // Remove from all OTHER folders when moving
                                    it.copy(senders = it.senders - showMoveToFolder!!)
                                }
                            }
                            onFoldersChange(newFolders)
                            showMoveToFolder = null
                        }) {
                            Text(folder.name + if (isCurrentlyInFolder) " (Current)" else "")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoveToFolder = null }) { Text("Cancel") }
            }
        )
    }

    if (showCreateFolder) {
        var newFolderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolder = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newFolderName.isNotBlank()) {
                        onFoldersChange(folders + SmsFolder(UUID.randomUUID().toString(), newFolderName, emptySet()))
                        showCreateFolder = false
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolder = false }) { Text("Cancel") }
            }
        )
    }

    if (folderToEdit != null) {
        var editName by remember { mutableStateOf(folderToEdit!!.name) }
        AlertDialog(
            onDismissRequest = { folderToEdit = null },
            title = { Text("Edit Folder") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Folder Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (editName.isNotBlank()) {
                        val newFolders = folders.map { if (it.id == folderToEdit!!.id) it.copy(name = editName) else it }
                        onFoldersChange(newFolders)
                        folderToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { 
                        onFoldersChange(folders.filter { it.id != folderToEdit!!.id })
                        if (selectedFolderId == folderToEdit!!.id) onFolderSelected(null)
                        folderToEdit = null
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { folderToEdit = null }) { Text("Cancel") }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Application") },
                actions = {
                    if (!isDefaultSms && hasPermission) {
                        TextButton(onClick = onRequestDefaultSms) {
                            Text("Set as Default", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        bottomBar = {
            if (hasPermission && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, CircleShape),
                        placeholder = { Text("Search messages...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                }
            }
        },
        floatingActionButton = {
            if (hasPermission && !isLoading) {
                FloatingActionButton(
                    onClick = onNewSmsClick,
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New SMS")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !hasPermission -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SMS permissions are required to read messages.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(onClick = onRequestPermission) {
                            Text("Grant Permission")
                        }
                    }
                }
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    val groupedMessages = remember(messages) {
                        messages.groupBy { it.address }
                            .mapValues { entry -> entry.value.sortedByDescending { it.date } }
                            .values
                            .sortedByDescending { list -> list.first().date }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Folder List Top Bar
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedFolderId == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onFolderSelected(null) }
                                ) {
                                    Text("All", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                }
                            }
                            items(folders) { folder ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedFolderId == folder.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.combinedClickable(
                                        onClick = { onFolderSelected(folder.id) },
                                        onLongClick = { folderToEdit = folder }
                                    )
                                ) {
                                    Text(folder.name, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                }
                            }
                            item {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { showCreateFolder = true }
                                ) {
                                    Text("+ Folder", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                }
                            }
                        }

                        if (messages.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No messages match your search." else "No messages found."
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    groupedMessages.forEach { messageThread ->
                                        val latestMessage = messageThread.first()
                                        item(key = "thread_${latestMessage.address}_${latestMessage.date}") {
                                            SmsThreadItem(
                                                messages = messageThread,
                                                folders = folders,
                                                onClick = onThreadClick,
                                                onLongClick = { threadOptionsAddress = it }
                                            )
                                        }
                                    }
                                }
                                
                                FastScrollbar(
                                    state = listState,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmsThreadItem(
    messages: List<SmsMessage>,
    folders: List<SmsFolder>,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit
) {
    val latestMessage = messages.first()
    val threadFolders = folders.filter { it.senders.contains(latestMessage.address) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(latestMessage.address) },
                onLongClick = { onLongClick(latestMessage.address) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${latestMessage.address} (${messages.size})",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatDate(latestMessage.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = latestMessage.body,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )

            if (threadFolders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    threadFolders.forEach { folder ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = folder.name,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsConversationScreen(
    address: String,
    messages: List<SmsMessage>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var replyText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    // If the address contains only valid phone number characters, it's replyable. Letters = shortcode/business
    val isReplyable = address.matches(Regex("^\\+?[0-9\\s\\-()]+$"))
    val listState = rememberLazyListState()

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter {
                it.body.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(address) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Search Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, CircleShape),
                        placeholder = { Text("Search conversation...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                }

                // Reply Input or Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    if (isReplyable) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Type a message...") },
                                shape = CircleShape,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        try {
                                            val smsManager = android.telephony.SmsManager.getDefault()
                                            smsManager.sendTextMessage(address, null, replyText, null, null)
                                            replyText = ""
                                            Toast.makeText(context, "Message sent", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    } else {
                        Text(
                            text = "This sender does not allow replying messages.",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val messagesByDate = remember(filteredMessages) {
                // Group by formatted date string (e.g., "12 Oct, 2023")
                filteredMessages
                    .sortedByDescending { it.date }
                    .groupBy { 
                        val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                        formatter.format(Date(it.date))
                    }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                messagesByDate.forEach { (dateHeader, dateMessages) ->
                    item(key = "header_$dateHeader") {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    items(dateMessages, key = { it.date }) { message ->
                        val isSent = message.type == 2 // 2 = Sent message in Android Telephony Provider
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = message.body,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.date)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            FastScrollbar(
                state = listState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

private fun formatDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

@Composable
fun FastScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    width: Dp = 12.dp
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val maxOffsetY = maxHeight.value // Container height in dp (as float)
        
        // Use derived state to avoid frequent recomposition warnings
        val listMetrics by remember(state) {
            androidx.compose.runtime.derivedStateOf {
                val totalItems = state.layoutInfo.totalItemsCount
                val visibleItemsCount = state.layoutInfo.visibleItemsInfo.size
                val firstItem = state.layoutInfo.visibleItemsInfo.firstOrNull()
                Triple(totalItems, visibleItemsCount, firstItem)
            }
        }
        
        val totalItems = listMetrics.first
        val visibleItemsCount = listMetrics.second
        val firstVisibleItem = listMetrics.third
        
        if (totalItems > 0 && visibleItemsCount > 0 && totalItems > visibleItemsCount) {
            // Calculate scrollbar height
            val scrollbarHeightDp = maxOf((visibleItemsCount.toFloat() / totalItems) * maxOffsetY, 40f)
            val maxScrollbarOffsetY = maxOffsetY - scrollbarHeightDp
            
            // Calculate current thumb offset based on list position
            val scrollOffset = state.firstVisibleItemScrollOffset
            val itemHeight = firstVisibleItem?.size?.toFloat() ?: 1f
            val fraction = if (itemHeight > 0) scrollOffset / itemHeight else 0f
            val exactIndex = state.firstVisibleItemIndex + fraction
            
            val scrollbarOffsetY = (exactIndex / (totalItems - visibleItemsCount)) * maxScrollbarOffsetY
            
            var currentThumbOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(scrollbarOffsetY) }
            if (!isDragging) {
                currentThumbOffsetY = scrollbarOffsetY
            }

            Box(
                modifier = Modifier
                    .offset(y = currentThumbOffsetY.dp)
                    .width(width)
                    .height(scrollbarHeightDp.dp)
                    // Increase touch target slightly by padding left
                    .padding(start = 2.dp)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            isDragging = true
                            // delta is in pixels, we divide by density to get dp
                            val deltaDp = delta / density
                            currentThumbOffsetY = (currentThumbOffsetY + deltaDp).coerceIn(0f, maxScrollbarOffsetY)
                            
                            val newExactIndex = (currentThumbOffsetY / maxScrollbarOffsetY) * (totalItems - visibleItemsCount)
                            val newIndex = newExactIndex.toInt().coerceIn(0, totalItems - visibleItemsCount)
                            val newScrollOffset = ((newExactIndex - newIndex) * itemHeight).toInt()
                            
                            coroutineScope.launch {
                                state.scrollToItem(newIndex, newScrollOffset)
                            }
                        },
                        onDragStarted = { isDragging = true },
                        onDragStopped = { isDragging = false }
                    )
            ) {
                // The actual visible thumb
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(1.dp, CircleShape)
                        .background(
                            color = if (state.isScrollInProgress || isDragging) Color.Gray.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmsListScreenPreview_WithMessages() {
    SMSApplicationTheme {
        SmsListScreenContent(
            messages = listOf(
                SmsMessage(
                    address = "+1234567890",
                    body = "Hey, don't forget our meeting tomorrow at 10 AM.",
                    date = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                    type = 1
                ),
                SmsMessage(
                    address = "Bank",
                    body = "Your account balance is \$100.00.",
                    date = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                    type = 1
                )
            ),
            isLoading = false,
            hasPermission = true,
            isDefaultSms = true,
            searchQuery = "",
            onSearchQueryChange = {},
            onRequestPermission = {},
            onRequestDefaultSms = {},
            folders = emptyList(),
            selectedFolderId = null,
            onFolderSelected = {},
            onFoldersChange = {},
            onThreadDelete = {},
            onThreadClick = {},
            onNewSmsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmsListScreenPreview_Loading() {
    SMSApplicationTheme {
        SmsListScreenContent(
            messages = emptyList(),
            isLoading = true,
            hasPermission = true,
            isDefaultSms = false,
            searchQuery = "",
            onSearchQueryChange = {},
            onRequestPermission = {},
            onRequestDefaultSms = {},
            folders = emptyList(),
            selectedFolderId = null,
            onFolderSelected = {},
            onFoldersChange = {},
            onThreadDelete = {},
            onThreadClick = {},
            onNewSmsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmsListScreenPreview_Empty() {
    SMSApplicationTheme {
        SmsListScreenContent(
            messages = emptyList(),
            isLoading = false,
            hasPermission = true,
            isDefaultSms = false,
            searchQuery = "",
            onSearchQueryChange = {},
            onRequestPermission = {},
            onRequestDefaultSms = {},
            folders = emptyList(),
            selectedFolderId = null,
            onFolderSelected = {},
            onFoldersChange = {},
            onThreadDelete = {},
            onThreadClick = {},
            onNewSmsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmsListScreenPreview_NoPermission() {
    SMSApplicationTheme {
        SmsListScreenContent(
            messages = emptyList(),
            isLoading = false,
            hasPermission = false,
            isDefaultSms = false,
            searchQuery = "",
            onSearchQueryChange = {},
            onRequestPermission = {},
            onRequestDefaultSms = {},
            folders = emptyList(),
            selectedFolderId = null,
            onFolderSelected = {},
            onFoldersChange = {},
            onThreadDelete = {},
            onThreadClick = {},
            onNewSmsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmsMessageItemPreview() {
    SMSApplicationTheme {
        SmsThreadItem(
            messages = listOf(
                SmsMessage(
                    address = "John Doe",
                    body = "Hey! Let's catch up later this week.",
                    date = System.currentTimeMillis(),
                    type = 1
                ),
                SmsMessage(
                    address = "John Doe",
                    body = "Are we still on for tomorrow?",
                    date = System.currentTimeMillis() - 86400000,
                    type = 1
                )
            ),
            folders = listOf(SmsFolder("1", "Work", setOf("John Doe"))),
            onClick = {},
            onLongClick = {}
        )
    }
}