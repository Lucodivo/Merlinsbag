# Forgettable Infrequent Actions

#### import Android material icons for use in .xml files
- open Vector Asset
  - Can't open from this file as this file does not exist where the app resources are
- click "Clip art" to find the icon

#### Find files on device
- Internal App Storage
  - /data/data/{app name}
- External App Storage
  - /sdcard/
    - /sdcard/Android/data/{app name}
    - /sdcard/pictures/{app name}

## IDE Errors
#### Error running 'Android Java Debugger (pid: 28478, debug port: 53060)' Unable to open debugger port (localhost:53060): java.net.SocketException "Connection reset"
- Restart Android Studio (killing adb server did not work)