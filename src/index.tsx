import LocationModule from './NativeLocationModule';
import { NativeEventEmitter } from 'react-native';

// Location tracking methods
export function startRealtimeTracking(): void {
  LocationModule?.startRealtimeTracking();
}

export function stopRealtimeTracking(): void {
  LocationModule?.stopRealtimeTracking();
}

// Event emitter for location updates
export const locationEventEmitter =
  LocationModule &&
  typeof (LocationModule as any).addListener === 'function' &&
  typeof (LocationModule as any).removeListeners === 'function'
    ? new NativeEventEmitter(LocationModule as any)
    : null;

// Location data type
export interface LocationData {
  latitude: number;
  longitude: number;
  accuracy: number;
  timestamp: number;
  time: string;
  realtime: boolean;
  source: string;
}

// Event listener helper
export function addLocationListener(
  callback: (location: LocationData) => void
) {
  if (!locationEventEmitter) {
    console.warn('LocationModule is not available');
    return () => {};
  }
  return locationEventEmitter.addListener(
    'onRealtimeLocationUpdate',
    (data: any) => {
      callback(data as LocationData);
    }
  );
}
