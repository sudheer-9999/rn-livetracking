package com.rnlivetracking

import android.content.Intent
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.util.Log

class LocationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    override fun getName(): String = "LocationModule"
    
    @ReactMethod
    fun startRealtimeTracking() {
        Log.d("LocationModule", "startRealtimeTracking called from JS")
        try {
            // Stop any existing service first to prevent duplicates
            val stopIntent = Intent(reactApplicationContext, LocationForegroundService::class.java)
            reactApplicationContext.stopService(stopIntent)
            
            // Start fresh service
            val intent = Intent(reactApplicationContext, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(reactApplicationContext, intent)
            Log.d("LocationModule", "Persistent tracking service started")
        } catch (e: Exception) {
            Log.e("LocationModule", "Error starting tracking", e)
        }
    }
    
    @ReactMethod
    fun stopRealtimeTracking() {
        Log.d("LocationModule", "stopRealtimeTracking called from JS")
        try {
            val intent = Intent(reactApplicationContext, LocationForegroundService::class.java)
            reactApplicationContext.stopService(intent)
            Log.d("LocationModule", "Persistent tracking stopped")
        } catch (e: Exception) {
            Log.e("LocationModule", "Error stopping tracking", e)
        }
    }
}