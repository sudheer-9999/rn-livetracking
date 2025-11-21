package com.rnlivetracking

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LocationForegroundService : Service() {
    companion object {
        private const val TAG = "LocationForegroundService"
        private const val NOTIFICATION_ID = 12345678
        private const val CHANNEL_ID = "location_service_channel"
        private const val LOCATION_INTERVAL = 5 * 60 * 1000L
        private const val ALARM_REQUEST_CODE = 98765
        const val ACTION_FORCE_LOCATION = "FORCE_LOCATION_UPDATE"
    }
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmPendingIntent: PendingIntent
    
    private var wakeLock: PowerManager.WakeLock? = null
    private val executor = Executors.newScheduledThreadPool(2) // Reduced from 3
    private var lastLocationTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "📍 Creating ULTIMATE location service")
        
        acquireWakeLock()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createPersistentNotification())
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()
        setupAlarmManager()
        startDualTracking() // Combined approach
        
        Log.d(TAG, "✅ ULTIMATE location service started")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "📍 Service command received")
        
        intent?.action?.let { action ->
            when (action) {
                ACTION_FORCE_LOCATION -> {
                    Log.d(TAG, "🔔 Alarm triggered - forcing immediate location")
                    forceImmediateLocationUpdate()
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 Service destroyed")
        stopDualTracking()
        cancelAlarms()
        releaseWakeLock()
    }
    
    // === WAKE LOCK === (From Code 2 - better flags)
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "LocationService:WakeLock"
        )
        wakeLock?.acquire()
        Log.d(TAG, "🔋 WakeLock acquired")
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
    
    // === ALARM MANAGER === (From Code 1 - for exact timing)
    private fun setupAlarmManager() {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, LocationAlarmReceiver::class.java).apply {
            action = ACTION_FORCE_LOCATION
        }
        alarmPendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun scheduleExactAlarm() {
        val triggerTime = SystemClock.elapsedRealtime() + LOCATION_INTERVAL
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                alarmPendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                alarmPendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                alarmPendingIntent
            )
        }
        
        val nextTime = SimpleDateFormat("HH:mm:ss").format(Date(System.currentTimeMillis() + LOCATION_INTERVAL))
        Log.d(TAG, "⏰ Exact alarm scheduled for $nextTime")
    }
    
    private fun cancelAlarms() {
        alarmManager.cancel(alarmPendingIntent)
        Log.d(TAG, "⏰ Alarms cancelled")
    }
    
    // === NOTIFICATION === (From Code 2 - better UX)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Essential Location Tracking",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Critical location tracking service - DO NOT CLOSE"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createPersistentNotification(): Notification {
    // Use launcher intent instead of MainActivity (library packages don't have MainActivity)
    val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
    val pendingIntent = PendingIntent.getActivity(
        this, 0, notificationIntent, 
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    
    val stopIntent = Intent(this, StopServiceReceiver::class.java)
    val stopPendingIntent = PendingIntent.getBroadcast(
        this, 0, stopIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("📍 Essential Location Tracking")
        .setContentText("Active • High accuracy • Required for tracking")
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setOngoing(true) // ⭐ KEY: Prevents swipe dismissal
        .setShowWhen(false)
        .setOnlyAlertOnce(true)
        .setAutoCancel(false)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setCategory(Notification.CATEGORY_SERVICE)
        .setColor(Color.RED)
        .setSilent(true)
        .build()
}
    
    // === LOCATION SETUP === (Combined approach)
    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = LOCATION_INTERVAL
            fastestInterval = 30000L // More frequent when available (from Code 1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = LOCATION_INTERVAL
            smallestDisplacement = 0f
        }
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.lastLocation?.let { location ->
                    processLocation(location, "fused_provider")
                }
            }
        }
    }
    
    // === DUAL TRACKING STRATEGY === (BEST OF BOTH)
    private fun startDualTracking() {
        try {
            // 1. Start regular location updates (from both)
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // 2. Start AlarmManager for exact timing (from Code 1)
            scheduleExactAlarm()
            
            // 3. Start JS bridge keep-alive (from Code 2 - but reduced frequency)
            executor.scheduleAtFixedRate({
                keepJsBridgeAlive()
            }, 0, 30, TimeUnit.SECONDS) // Reduced from 20 to 30 seconds
            
            Log.d(TAG, "✅ DUAL tracking started: FusedLocation + AlarmManager + JS Keep-alive")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting dual tracking", e)
        }
    }
    
    private fun stopDualTracking() {
        try {
            if (::locationCallback.isInitialized) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
            executor.shutdown()
            Log.d(TAG, "🛑 Dual tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking", e)
        }
    }
    
    // === FORCED LOCATION UPDATE === (From Code 1 - improved)
    private fun forceImmediateLocationUpdate() {
        Log.d(TAG, "🚀 Alarm: Forcing immediate location")
        
        try {
            // Try last known location first (fastest)
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        Log.d(TAG, "🎯 Alarm: Last known location acquired")
                        processLocation(it, "alarm_forced")
                    } ?: run {
                        // Fallback to high-priority request
                        requestHighPriorityLocation()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in alarm location update", e)
            requestHighPriorityLocation()
        } finally {
            // Always reschedule
            scheduleExactAlarm()
        }
    }
    
    private fun requestHighPriorityLocation() {
        try {
            val priorityRequest = LocationRequest.create().apply {
                numUpdates = 1
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                expirationTime = System.currentTimeMillis() + 10000 // 10s timeout
            }
            
            val priorityCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult?.lastLocation?.let { location ->
                        Log.d(TAG, "🎯 Alarm: High-priority location acquired")
                        processLocation(location, "alarm_high_priority")
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                priorityRequest,
                priorityCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in high-priority request", e)
        }
    }
    
    // === LOCATION PROCESSING === (Combined logic)
    private fun processLocation(location: Location, source: String) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastLocation = currentTime - lastLocationTime
        
        // Smart deduplication (30 seconds minimum between locations)
        if (timeSinceLastLocation < 30000) {
            Log.d(TAG, "📍 Skipping location - too soon: ${timeSinceLastLocation/1000}s")
            return
        }
        
        lastLocationTime = currentTime
        val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        Log.d("ULTIMATE_TRACKER", 
            "🎯 [$timeString] Source: $source | " +
            "LAT: ${"%.6f".format(location.latitude)}, " +
            "LNG: ${"%.6f".format(location.longitude)}, " +
            "ACC: ${location.accuracy}m"
        )
        
        sendToReactNative(location, source)
    }
    
    // === JS BRIDGE MANAGEMENT === (From Code 2 - proven)
    private fun keepJsBridgeAlive() {
        try {
            val reactApplication = application as? com.facebook.react.ReactApplication
            val context = reactApplication?.reactNativeHost?.reactInstanceManager?.currentReactContext
            
            if (context != null) {
                try {
                    val eventEmitter = context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    val heartbeatData = Arguments.createMap().apply {
                        putDouble("timestamp", System.currentTimeMillis().toDouble())
                        putString("type", "heartbeat")
                    }
                    eventEmitter.emit("jsBridgeHeartbeat", heartbeatData)
                    Log.d(TAG, "💓 JS bridge heartbeat sent")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ JS bridge context may be destroyed")
                    wakeJsBridge()
                }
            } else {
                Log.w(TAG, "⚠️ JS bridge not active, waking...")
                wakeJsBridge()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in JS bridge keep-alive", e)
            wakeJsBridge()
        }
    }
    
    private fun wakeJsBridge() {
        try {
            val wakeIntent = Intent(this, LocationHeadlessService::class.java)
            val wakeData = Arguments.createMap().apply {
                putString("action", "wake_bridge")
                putDouble("timestamp", System.currentTimeMillis().toDouble())
            }
            wakeIntent.putExtra("wakeData", Arguments.toBundle(wakeData))
            startService(wakeIntent)
            HeadlessJsTaskService.acquireWakeLockNow(this)
            Log.d(TAG, "🔔 Attempting to wake JS bridge")
        } catch (e: Exception) {
            Log.e(TAG, "Error waking JS bridge", e)
        }
    }
    
    // === REACT NATIVE COMMUNICATION === (From Code 2 - with improvements)
    private fun sendToReactNative(location: Location, source: String) {
        try {
            val reactApplication = application as? com.facebook.react.ReactApplication
            val context = reactApplication?.reactNativeHost?.reactInstanceManager?.currentReactContext
            
            if (context != null) {
                try {
                    val eventEmitter = context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    val locationMap = Arguments.createMap().apply {
                        putDouble("latitude", location.latitude)
                        putDouble("longitude", location.longitude)
                        putDouble("accuracy", location.accuracy.toDouble())
                        putDouble("timestamp", location.time.toDouble())
                        putString("time", SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()))
                        putBoolean("realtime", true)
                        putString("source", source) // Added source tracking
                    }
                    
                    eventEmitter.emit("onRealtimeLocationUpdate", locationMap)
                    Log.d(TAG, "📡 Location sent to JS from $source")
                } catch (e: Exception) {
                    Log.w(TAG, "📱 JS bridge not available for location")
                    storeLocationForLater(location, source)
                }
            } else {
                Log.w(TAG, "📱 React context not available")
                storeLocationForLater(location, source)
                wakeJsBridge()
            }
        } catch (e: Exception) {
            Log.e(TAG, "📱 Error sending to React Native", e)
            storeLocationForLater(location, source)
        }
    }
    
    private fun storeLocationForLater(location: Location, source: String) {
        val prefs = getSharedPreferences("location_cache", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putLong("last_location_time", location.time)
            putString("last_location_lat", location.latitude.toString())
            putString("last_location_lng", location.longitude.toString())
            putString("last_location_acc", location.accuracy.toString())
            putString("last_location_source", source)
            apply()
        }
    }
}