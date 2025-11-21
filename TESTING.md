# Testing Guide

## Prerequisites
- Node.js >= 20
- Android Studio with Android SDK
- Physical Android device or emulator (API 24+)
- Enable Developer Options and USB Debugging on your device

## Setup Steps

1. **Install dependencies:**
   ```bash
   # From the root directory
   yarn install
   
   # Build the package
   yarn prepare
   ```

2. **Navigate to example directory:**
   ```bash
   cd example
   yarn install
   ```

3. **For Android:**
   ```bash
   # Make sure you have an Android device connected or emulator running
   # Check with: adb devices
   
   # Run the example app
   yarn android
   ```

## Testing the Location Tracking

1. **Grant Permissions:**
   - When the app launches, it will request location permissions
   - Grant "Allow all the time" for background location access
   - This is required for the foreground service to work

2. **Start Tracking:**
   - Tap "Start Tracking" button
   - You should see a persistent notification: "📍 Essential Location Tracking"
   - Location updates will appear in the app every 5 minutes (or when location changes significantly)

3. **Verify Location Updates:**
   - The app will display:
     - Latitude and Longitude
     - Accuracy in meters
     - Timestamp
     - Source of the location update

4. **Test Background Behavior:**
   - Put the app in the background
   - The notification should remain visible
   - Location tracking should continue
   - Check logcat for location updates: `adb logcat | grep "ULTIMATE_TRACKER"`

5. **Stop Tracking:**
   - Tap "Stop Tracking" button
   - The notification should disappear
   - Location updates will stop

## Debugging

### View Logs
```bash
# Filter location service logs
adb logcat | grep -E "LocationModule|LocationForegroundService|ULTIMATE_TRACKER"

# View all React Native logs
adb logcat | grep ReactNativeJS
```

### Check Service Status
```bash
# Check if the service is running
adb shell dumpsys activity services | grep LocationForegroundService
```

### Common Issues

1. **No location updates:**
   - Ensure location permissions are granted (especially background location)
   - Check if location services are enabled on the device
   - Verify GPS is enabled
   - Check logcat for permission errors

2. **Service stops:**
   - Check battery optimization settings (disable for the app)
   - Ensure the app is not force-stopped
   - Check if the device has enough battery

3. **Build errors:**
   - Clean and rebuild: `cd example/android && ./gradlew clean`
   - Rebuild: `yarn android`

## Testing Checklist

- [ ] App installs successfully
- [ ] Permissions are requested and granted
- [ ] Start Tracking button works
- [ ] Persistent notification appears
- [ ] Location updates are received in the app
- [ ] Location updates continue in background
- [ ] Stop Tracking button works
- [ ] Notification disappears when stopped
- [ ] Service restarts after device reboot (if LocationRestartReceiver is configured)

