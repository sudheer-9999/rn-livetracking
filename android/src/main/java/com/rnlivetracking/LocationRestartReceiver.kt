package com.rnlivetracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LocationRestartReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "LocationRestartReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "🔄 Received restart broadcast: ${intent.action}")
        
        when (intent.action) {
            "RESTART_LOCATION_SERVICE" -> {
                Log.d(TAG, "🚀 Restarting location service...")
                try {
                    val serviceIntent = Intent(context, LocationForegroundService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d(TAG, "✅ Service restart initiated")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to restart service", e)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "📱 Device booted - restarting location service")
                val serviceIntent = Intent(context, LocationForegroundService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}