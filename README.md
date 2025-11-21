# react-native-rn-livetracking

A React Native library for real-time location tracking with background support on Android. This package provides persistent location tracking using foreground services with high accuracy and reliability.

## Features

- ✅ **Real-time location tracking** - Get location updates every 5 minutes or on significant location changes
- ✅ **Background tracking** - Continues tracking even when app is in background
- ✅ **Foreground service** - Uses Android foreground service for reliable tracking
- ✅ **High accuracy** - Uses Google Play Services Fused Location Provider
- ✅ **Battery optimized** - Smart location updates with deduplication
- ✅ **Auto-restart** - Service automatically restarts after device reboot
- ✅ **Event-based updates** - Receive location updates via React Native events

## Installation

```bash
npm install react-native-rn-livetracking
# or
yarn add react-native-rn-livetracking
```

### Android Setup

#### 1. Add LocationPackage to MainApplication

In your `android/app/src/main/java/.../MainApplication.kt` (or `.java`):

```kotlin
import com.rnlivetracking.LocationPackage

class MainApplication : Application(), ReactApplication {
  override fun getPackages(): List<ReactPackage> {
    return PackageList(this).packages.apply {
      add(LocationPackage()) // Add this line
    }
  }
}
```

#### 2. Add Permissions to AndroidManifest.xml

The library's AndroidManifest already includes required permissions, but ensure your app's `AndroidManifest.xml` has:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

#### 3. Request Runtime Permissions

You need to request location permissions at runtime:

```typescript
import { PermissionsAndroid, Platform } from 'react-native';

const requestLocationPermission = async () => {
  if (Platform.OS !== 'android') {
    return;
  }

  try {
    const granted = await PermissionsAndroid.requestMultiple([
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
      PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
    ]);

    const allGranted = Object.values(granted).every(
      (status) => status === PermissionsAndroid.RESULTS.GRANTED
    );

    return allGranted;
  } catch (err) {
    console.warn(err);
    return false;
  }
};
```

## Usage

### Basic Example

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text, Button } from 'react-native';
import {
  startRealtimeTracking,
  stopRealtimeTracking,
  addLocationListener,
  type LocationData,
} from 'react-native-rn-livetracking';

export default function App() {
  const [location, setLocation] = useState<LocationData | null>(null);

  useEffect(() => {
    // Subscribe to location updates
    const subscription = addLocationListener((loc: LocationData) => {
      setLocation(loc);
      console.log('Location update:', loc);
    });

    return () => {
      // Cleanup subscription
      if (subscription && typeof subscription === 'object' && 'remove' in subscription) {
        subscription.remove();
      } else if (typeof subscription === 'function') {
        subscription();
      }
    };
  }, []);

  return (
    <View style={{ flex: 1, justifyContent: 'center', padding: 20 }}>
      {location && (
        <View>
          <Text>Latitude: {location.latitude}</Text>
          <Text>Longitude: {location.longitude}</Text>
          <Text>Accuracy: {location.accuracy}m</Text>
          <Text>Time: {location.time}</Text>
          <Text>Source: {location.source}</Text>
        </View>
      )}
      <Button title="Start Tracking" onPress={startRealtimeTracking} />
      <Button title="Stop Tracking" onPress={stopRealtimeTracking} />
    </View>
  );
}
```

### Complete Example with Permissions

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text, Button, Alert, Platform } from 'react-native';
import { PermissionsAndroid } from 'react-native';
import {
  startRealtimeTracking,
  stopRealtimeTracking,
  addLocationListener,
  type LocationData,
} from 'react-native-rn-livetracking';

export default function App() {
  const [location, setLocation] = useState<LocationData | null>(null);
  const [hasPermission, setHasPermission] = useState(false);

  useEffect(() => {
    requestLocationPermission();
    
    const subscription = addLocationListener((loc: LocationData) => {
      setLocation(loc);
    });

    return () => {
      if (subscription && typeof subscription === 'object' && 'remove' in subscription) {
        subscription.remove();
      } else if (typeof subscription === 'function') {
        subscription();
      }
    };
  }, []);

  const requestLocationPermission = async () => {
    if (Platform.OS !== 'android') {
      setHasPermission(true);
      return;
    }

    try {
      const granted = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
        PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
      ]);

      const allGranted = Object.values(granted).every(
        (status) => status === PermissionsAndroid.RESULTS.GRANTED
      );

      setHasPermission(allGranted);
    } catch (err) {
      console.warn(err);
    }
  };

  const handleStart = () => {
    if (!hasPermission) {
      Alert.alert('Permission Required', 'Please grant location permissions first.');
      requestLocationPermission();
      return;
    }
    startRealtimeTracking();
    Alert.alert('Tracking Started', 'Location tracking has been started.');
  };

  return (
    <View style={{ flex: 1, justifyContent: 'center', padding: 20 }}>
      <Text>Permission: {hasPermission ? '✅ Granted' : '❌ Not Granted'}</Text>
      {location && (
        <View style={{ marginVertical: 20 }}>
          <Text>Lat: {location.latitude.toFixed(6)}</Text>
          <Text>Lng: {location.longitude.toFixed(6)}</Text>
          <Text>Accuracy: {location.accuracy.toFixed(1)}m</Text>
          <Text>Time: {location.time}</Text>
        </View>
      )}
      <Button title="Start Tracking" onPress={handleStart} />
      <Button title="Stop Tracking" onPress={stopRealtimeTracking} />
    </View>
  );
}
```

## API Reference

### Functions

#### `startRealtimeTracking()`

Starts the location tracking service. A persistent notification will appear indicating that tracking is active.

```typescript
startRealtimeTracking(): void
```

#### `stopRealtimeTracking()`

Stops the location tracking service and removes the persistent notification.

```typescript
stopRealtimeTracking(): void
```

#### `addLocationListener(callback)`

Subscribes to location update events. Returns a subscription object with a `remove()` method.

```typescript
addLocationListener(
  callback: (location: LocationData) => void
): EventSubscription | (() => void)
```

**Example:**
```typescript
const subscription = addLocationListener((location) => {
  console.log('New location:', location);
});

// Later, to unsubscribe:
if (subscription && typeof subscription === 'object' && 'remove' in subscription) {
  subscription.remove();
}
```

### Types

#### `LocationData`

```typescript
interface LocationData {
  latitude: number;      // Latitude in degrees
  longitude: number;     // Longitude in degrees
  accuracy: number;      // Accuracy in meters
  timestamp: number;     // Unix timestamp in milliseconds
  time: string;          // Formatted time string (HH:mm:ss)
  realtime: boolean;     // Always true for realtime tracking
  source: string;        // Source of location update (e.g., "fused_provider", "alarm_forced")
}
```

## How It Works

1. **Foreground Service**: Uses Android's foreground service to ensure the tracking continues even when the app is in the background.

2. **Dual Tracking Strategy**:
   - Fused Location Provider for regular updates
   - AlarmManager for exact timing (every 5 minutes)
   - JS Bridge keep-alive to maintain React Native context

3. **Smart Deduplication**: Prevents duplicate location updates within 30 seconds.

4. **Event Emission**: Location updates are sent to JavaScript via React Native's event emitter system.

## Permissions

The following permissions are required:

- `ACCESS_FINE_LOCATION` - For high-accuracy location
- `ACCESS_COARSE_LOCATION` - For network-based location
- `ACCESS_BACKGROUND_LOCATION` - For location updates when app is in background (Android 10+)
- `FOREGROUND_SERVICE` - For foreground service
- `FOREGROUND_SERVICE_LOCATION` - For location foreground service (Android 14+)

**Note**: Background location permission requires special handling on Android 10+. Users must grant "Allow all the time" permission in system settings.

## Battery Optimization

For best results, users should:

1. Disable battery optimization for your app in Android settings
2. Grant "Allow all the time" for background location permission
3. Keep the app in the foreground when first starting tracking

## Troubleshooting

### Location updates not received

1. **Check permissions**: Ensure all location permissions are granted, especially background location
2. **Check GPS**: Ensure GPS is enabled on the device
3. **Check battery optimization**: Disable battery optimization for your app
4. **Check logs**: Use `adb logcat | grep "LocationModule"` to see debug logs

### Service stops unexpectedly

1. **Battery optimization**: Disable battery optimization
2. **Doze mode**: Test with device not in Doze mode
3. **Force stop**: Ensure app is not force-stopped by user

### Build errors

1. **Clean build**: `cd android && ./gradlew clean`
2. **Rebuild**: `yarn android`
3. **Check dependencies**: Ensure Google Play Services is available

See [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) for more details.

## Requirements

- React Native >= 0.60
- Android API Level 24+ (Android 7.0+)
- Google Play Services (for location services)

## Limitations

- **Android only**: iOS support is not currently available
- **Battery usage**: Continuous location tracking will consume battery
- **Background restrictions**: Some Android devices may restrict background location access

## Development

See [TESTING.md](./TESTING.md) for development and testing instructions.

## License

MIT

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and feature requests, please use the [GitHub Issues](https://github.com/sudheer-9999/react-native-rn-livetracking/issues) page.
