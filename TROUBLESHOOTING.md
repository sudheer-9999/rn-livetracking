# Troubleshooting Metro Bundler Issues

## "Unable to load script. Make sure you're running Metro" Error

### Solution 1: Start Metro Bundler Manually

1. **Open a new terminal window/tab**

2. **Navigate to the example directory:**
   ```bash
   cd example
   ```

3. **Start Metro bundler:**
   ```bash
   yarn start
   # or
   npx react-native start
   ```

4. **In another terminal, run the Android app:**
   ```bash
   cd example
   yarn android
   ```

### Solution 2: Clear Cache and Restart

```bash
cd example

# Clear Metro cache
yarn start --reset-cache

# In another terminal
yarn android
```

### Solution 3: Full Clean and Rebuild

```bash
# From root directory
cd example

# Clear all caches
rm -rf node_modules
rm -rf android/app/build
rm -rf android/build
rm -rf android/.gradle
watchman watch-del-all  # if you have watchman installed
rm -rf $TMPDIR/react-*
rm -rf $TMPDIR/metro-*

# Reinstall
yarn install

# Rebuild
cd android
./gradlew clean
cd ..

# Start Metro
yarn start --reset-cache

# In another terminal, run app
yarn android
```

### Solution 4: Check Port 8081

Metro runs on port 8081 by default. If it's in use:

```bash
# Check if port is in use
lsof -i :8081

# Kill the process if needed
kill -9 <PID>

# Or use a different port
yarn start --port 8082
# Then in android/app/build.gradle, update the dev server port
```

### Solution 5: Check Network Configuration

If using a physical device:

1. **Find your computer's IP address:**
   ```bash
   # Linux/Mac
   ifconfig | grep "inet "
   # or
   ip addr show
   ```

2. **Shake device → Dev Settings → Debug server host & port for device**
   - Enter: `YOUR_IP:8081` (e.g., `192.168.1.100:8081`)

3. **Or use ADB reverse:**
   ```bash
   adb reverse tcp:8081 tcp:8081
   ```

### Solution 6: Verify Metro Config

Make sure `example/metro.config.js` is properly configured for monorepo setup.

### Quick Fix Command

```bash
# One-liner to fix most issues
cd example && yarn start --reset-cache
# Then in another terminal:
cd example && yarn android
```


