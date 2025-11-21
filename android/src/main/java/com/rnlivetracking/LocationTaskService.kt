package com.rnlivetracking

import android.content.Intent
import android.os.Bundle
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import javax.annotation.Nullable

class LocationTaskService : HeadlessJsTaskService() {
    
    @Nullable
    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        try {
            val extras = intent?.extras ?: return null
            
           return HeadlessJsTaskConfig(
            "LocationTrackingTask", // task key
            Arguments.fromBundle(extras), // data
            30000, // timeout in ms
            true // ✅ FIX: Allow task to run in foreground
        )
        } catch (e: Exception) {
            return null
        }
    }
}