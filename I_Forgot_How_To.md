# Forgettable Infrequent Actions

## import Android material icons for use in .xml files
- open Vector Asset
  - Can't open from this file as this file does not exist where the app resources are
- click "Clip art" to find the icon

## Find files on device
- Internal App Storage
  - /data/data/{app name}
- External App Storage
  - /sdcard/
    - /sdcard/Android/data/{app name}
    - /sdcard/pictures/{app name}

## Change Adaptive App Icon
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

## Analyze performance and hardware usage with traces using androidx.tracing library
- Use the androidx.tracing library to create a trace
  - Examples:
``` 
trace(label = "ArticleDetailViewModel: exportArticle"){
  articleRepository.exportArticle(articleId)
}
``` 
``` 
beginSection(label = "ArticleDetailViewModel: exportArticle")
  articleRepository.exportArticle(articleId)
endSection()
``` 
``` 
beginAsyncSection(label = "ArticleDetailViewModel: exportArticle", cookie = A_UNIQUE_TRACE_ID)
  articleRepository.exportArticle(articleId) // if suspendable
endSection(A_UNIQUE_TRACE_ID)
``` 
- View traces using Android Studio Profiler
  - Run app as profileable (low overhead)
  - Start a profiler task using Capture System Activities (System Trace)
  - When the capture begins (it may restart the app), perform desired actions that are being traced
  - Click "stop recording and show results"
  - The capture results will open up in the Profiler tool window
  - In the "Analysis" pane, select the "All Threads" tab
  - Choose either the "Top Down" or "Bottom Up" tabs
  - Search for the supplied label for the trace
- Common Issues:
  - *NEVER* profile an app as debuggable. 
    - Debug builds provide irrelevant information as performance is drastically different in release builds.
  - Dropping traces for seemingly no reason? Search is jank and might be the problem. Traces might have actually been dropped for no reason. Try again. 🤷‍♀️
  - Often times, System.nanoTime() works just fine and performing a SystemTrace is overkill
    - SystemTrace is great if you have little idea of what is slow. Sometimes it might be a good idea to start with a SystemTrace and then move back to System.nanoTime()
  - Ensure the work that you are attempting to measure is not dispatched on a separate thread.
    - For example, the following code will not measure any work done in the exportArticle() function.
``` 
trace("ArticleDetailViewModel: exportArticle"){
  viewModelScope.launch(Dispatchers.IO) {
    articleRepository.exportArticle(articleId)
  }
}
``` 

## App Snapshot
### Save
- Install app in debug mode on device
- Open "device explorer" in Android Studio
- pull out the following files & directories from /data/data/com.inasweaterpoorlyknit.merlinsbag
  - databases/
    - noop-database (SQLite database)
    - noop-database-wal (may contain uncommitted database writes)
  - files/datastore/user_preferences.pb (datastore preference values)
  - files/articles/* (article image data)
- Remember to reinstall app as release to restore performance
### Load
- Install app in debug mode on device
- Open "device explorer" in Android Studio
- In /data/data/com.inasweaterpoorlyknit.merlinsbag
  - databases/
    - delete all files, insert noop-database and noop-database-wal
  - files/articles
    - delete all, replace folder
  - files/datastore
    - delete all, insert user_preferences.pb
- Remember to reinstall app as release to restore performance

## IDE Errors
### Error running 'Android Java Debugger (pid: 28478, debug port: 53060)' Unable to open debugger port (localhost:53060): java.net.SocketException "Connection reset"
- Restart Android Studio (killing adb server did not work)

## Runtime Errors
### java.lang.IllegalStateException: Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number. You can simply fix this by increasing the version number.
- The answer to solve this problem when legitimate is obvious. However, it can occur on completely fresh installs when it is undesireable to update the database version just to satisfy this error.
- Solution: Install app. Let it crash at least once. Go into 'App Info' on Android OS -> Storage -> Clear Data. Restart app.

## Test

### Errors
#### NoClassDefFoundError
- run tests as with gradle, *NOT* as JUnit tests

### Run all core module unit tests
- Open gradle window in Android Studio
- run merlinsbag/core/verification/testReleaseUnitTest