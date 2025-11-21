import { NativeModules } from 'react-native';

interface LocationModuleInterface {
  startRealtimeTracking(): void;
  stopRealtimeTracking(): void;
}

const { LocationModule } = NativeModules;

export default LocationModule as LocationModuleInterface;
