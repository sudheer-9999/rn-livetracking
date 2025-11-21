import { useEffect, useState } from 'react';
import { Text, View, StyleSheet, Button, Alert, Platform } from 'react-native';
import { PermissionsAndroid } from 'react-native';
import {
  startRealtimeTracking,
  stopRealtimeTracking,
  addLocationListener,
} from 'react-native-rn-livetracking';

export default function App() {
  const [location, setLocation] = useState<string>('No location yet');
  const [hasPermission, setHasPermission] = useState<boolean>(false);

  useEffect(() => {
    requestLocationPermission();

    const subscription = addLocationListener((loc) => {
      setLocation(
        `Lat: ${loc.latitude.toFixed(6)}, Lng: ${loc.longitude.toFixed(
          6
        )}, Acc: ${loc.accuracy.toFixed(1)}m\nTime: ${loc.time}\nSource: ${
          loc.source
        }`
      );
    });

    return () => {
      if (
        subscription &&
        typeof subscription === 'object' &&
        'remove' in subscription
      ) {
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

      if (allGranted) {
        setHasPermission(true);
      } else {
        Alert.alert(
          'Permission Required',
          'Location permission is required for tracking to work.'
        );
      }
    } catch (err) {
      console.warn(err);
    }
  };

  const handleStartTracking = () => {
    if (!hasPermission) {
      Alert.alert(
        'Permission Required',
        'Please grant location permissions first.'
      );
      requestLocationPermission();
      return;
    }
    startRealtimeTracking();
    Alert.alert(
      'Tracking Started',
      'Location tracking has been started. Check the notification.'
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Location Tracking Test</Text>
      <Text style={styles.text}>{location}</Text>
      <Text style={styles.status}>
        Permission: {hasPermission ? '✅ Granted' : '❌ Not Granted'}
      </Text>
      <View style={styles.buttonContainer}>
        <Button title="Start Tracking" onPress={handleStartTracking} />
        <View style={styles.spacer} />
        <Button title="Stop Tracking" onPress={stopRealtimeTracking} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  text: {
    fontSize: 14,
    marginBottom: 10,
    textAlign: 'center',
    fontFamily: 'monospace',
    backgroundColor: '#fff',
    padding: 10,
    borderRadius: 5,
    width: '100%',
  },
  status: {
    fontSize: 14,
    marginBottom: 20,
    fontWeight: '600',
  },
  buttonContainer: {
    width: '100%',
    gap: 10,
  },
  spacer: {
    height: 10,
  },
});
