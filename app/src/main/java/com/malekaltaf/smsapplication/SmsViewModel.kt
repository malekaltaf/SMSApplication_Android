package com.malekaltaf.smsapplication

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)

    private val _messages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val messages: StateFlow<List<SmsMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            if (_hasPermission.value) {
                loadMessages()
            }
        }
    }

    init {
        getApplication<Application>().contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true,
            contentObserver
        )
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }

    fun updatePermissionStatus(isGranted: Boolean) {
        _hasPermission.value = isGranted
        if (isGranted) {
            loadMessages()
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedMessages = repository.getSmsMessages()
                _messages.value = fetchedMessages
            } catch (e: Exception) {
                // In a real app, handle errors better
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
