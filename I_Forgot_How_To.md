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

#### Upload ML Kit NDK Debug Symbols (NOTE: There must be a more automatic way to do this.)
- zip everything in the directory app/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib into a single file
- In Google Play Console, click the vertical "More" icon
- Upload the zip as "native debug symbols (.zip)"

#### Change Adaptive App Icon
- Open "Resource Manager"
- load monochrome icon with "Image Asset" tool using name "ic_launcher_monochrome"
- load color icon with "Image Asset" tool using name "ic_launcher_color"
- Ensure that res/mipmap-anydpi-v26/ic_launcher & res/mipmap-anydpi-v26/ic_launcher_round.xml contain the following:
```
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_color_background"/>
    <foreground android:drawable="@drawable/ic_launcher_color_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_monochrome_foreground"/>
</adaptive-icon>
```

## IDE Errors
#### Error running 'Android Java Debugger (pid: 28478, debug port: 53060)' Unable to open debugger port (localhost:53060): java.net.SocketException "Connection reset"
- Restart Android Studio (killing adb server did not work)