package com.malekaltaf.smsapplication

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Quick reply handling goes here. Required for Default SMS app.
        return START_NOT_STICKY
    }
}
