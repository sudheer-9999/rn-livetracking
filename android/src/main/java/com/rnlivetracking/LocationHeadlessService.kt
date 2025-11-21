package com.rnlivetracking

import android.content.Intent
import android.os.Bundle
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import javax.annotation.Nullable

class LocationHeadlessService : HeadlessJsTaskService() {
    
    @Nullable
    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        val extras = intent?.extras ?: return null
        
        return HeadlessJsTaskConfig(
            "BridgeKeepAliveTask",
            Arguments.fromBundle(extras),
            15000, // 15 second timeout
            true // allowed in foreground
        )
    }
}