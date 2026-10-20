package com.malekaltaf.smsapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // MMS receiving logic goes here. Required for Default SMS app.
    }
}
