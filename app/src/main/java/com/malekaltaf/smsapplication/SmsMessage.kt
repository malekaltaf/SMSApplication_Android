package com.malekaltaf.smsapplication

data class SmsMessage(
    val address: String,
    val body: String,
    val date: Long,
    val type: Int // 1 = Inbox, 2 = Sent
)
