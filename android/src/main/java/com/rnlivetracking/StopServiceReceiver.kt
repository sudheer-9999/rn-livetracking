package com.rnlivetracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("StopServiceReceiver", "User requested to stop tracking")
        
        // Stop the service
        val serviceIntent = Intent(context, LocationForegroundService::class.java)
        context.stopService(serviceIntent)
    }
}