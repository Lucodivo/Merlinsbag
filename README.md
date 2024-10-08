# Merlinsbag
<a href="https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.merlinsbag"><img alt="Download" src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/logo/merlinsbag_logo_96x96_rounded.png"></a><br>
<a href="https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.merlinsbag"><img alt="Download" src="https://img.shields.io/badge/Google%20Play-%20?logo=googleplay&amp;color=grey"></a>

Merlinsbag is an Android application for cataloging the things in your life.

## Video Demo
[![YouTube Demo Video of Merlinsbag App](https://img.youtube.com/vi/uUQYMU2N4kA/0.jpg)](https://youtu.be/uUQYMU2N4kA )

## Terminology
- "Article": an item that has been cataloged 
  - stemming from an "article of clothing"
  - examples: a shirt, a pair of pant, a poster, a cat, a record, a book, etc.
- "Ensemble": a collection of articles
  - analogous to hashtags on other platforms
  - examples: "tshirt", "yellow", "vinyl", "gaming", "non-fiction", "painting", etc.

## Features By Screen

### Add Article
- Provide photos of the things to catalog
- Adjust the tightness of the article cutout area
- Rotate the image in 90 degree increments
- Confirm cutout as newly cataloged article or attach cutout to existing article

### Articles
- View articles in one large catalog
- Select and edit articles in bulk
- Navigate to article detail view

<p float="left">
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_addArticle.png" alt="Add Article Screen" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_articles.png" alt="Articles Screen" width="200"/>
</p>

### Article Detail
- View an article in its full glory
- Attach additional images to article
- Easily select and view each image attached to article
- Attach article to existing or newly created ensembles
- Navigate among articles via horizontal swiping
- Export article images as PNGs to easily share outside the application
- View and easily navigate to attached ensembles

<p float="left">
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_articleDetail.png" alt="Article Detail Screen" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_addToEnsemble.png" alt="Add to Ensemble" width="200"/>
</p>

### Add Ensemble
- Create an ensemble with a given title
- Select articles to initialize ensemble with

### View Ensembles
- View all ensembles with short previews of the ensemble's articles
- Search among all ensembles
- Select and edit ensembles in bulk
- Navigate to ensemble detail view

### Ensemble Detail
- View all articles attached to an ensemble on one screen
- Select and remove articles from ensemble in bulk
- Attach existing articles to ensemble
- Navigate to article detail view that is filtered by the current ensemble

<p float="left">
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_createEnsemble.png" alt="Add Ensemble Dialog" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_ensembles.png" alt="Ensembles Screen" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_ensembleDetail.png" alt="EnsembleDetail" width="200"/>
</p>

### Settings
- Adjust the app-wide appearance of Merlinsbag:
  - Color Scheme
  - Font
  - Dark Mode
  - High Contrast
- View: 
  - Tips & information
  - Statistics about cataloged articles and ensembles
  - Welcome page (onboarding screen) 
  - App version numbers
- Links to:
  - Video demo
  - Privacy information
  - Developer information
  - Source code
  - Merlinsbag on Google Play Store for rating & reviewing
  - Eccohedra on Google Play Store (advertisement)
- Data Management:
  - Clear cache
  - Delete all data
<p float="left">
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Merlinsbag_snapshot_settings.png" alt="Settings" width="200"/>
</p>

### Accessibility
- Landscape view supported in entire app with unique layout configurations
- Great efforts made to ensure UI elements contain content descriptions for screen readers  

🐛 [If you experience any problems accessibility, please create an issue and it will quickly become a priority.](https://github.com/Lucodivo/Merlinsbag/issues/new) 🐛

## Codebase

### Terminology
- "Noop" is a simple prefix for application specific implementations of over general Android library classes and functions
  - Ex: NoopApplication, NoopIcons, NoopTheme, NoopDatabase

### Android Architecture
Merlinsbag is a single activity, no fragment, Android application. All UI and navigation is accomplished using
Jetpack Compose and Navigation with Compose. Important keywords to look for to understand navigation within the application
 are: *NavHost*, *NavController*, *NavHostController*, *Route*, *RouteArgs*, *navigateTo-*, *navOptions*, *Screen*

- *Screen*
  - Composable functions representing the actual UI implementation of the various app "screens" or "destinations"
  - In general, state is hoisted out of a *Screen* and into it's associated *Route*
    - All UI displayed is driven by a *Screen* function's arguments
    - This allows any *Screen* to be easily verified using @Preview annotated Composable functions
- *Route*
  - Composable functions that serve as a wrapper around a specific *Screen*
  - Acquire and maintain the navigation lambdas, ViewModels, and ActivityResultContracts
  - Provides state supplied by ViewModels to *Screen*
- *RouteArgs*
  - Data classes that hold the necessary information to fulfilling a navigation request to a specified *Route*
- *NavController*
  - Used to request navigation destination changes or otherwise manage the navigation back stack
    - Navigation requests are intercepted by the *NavHost*
- *navigateTo-*
  - Prefix of *NavController* extension functions that requesting the navigation to some specified *Route* represented by the suffix
  - It is also used as a prefix for *Route* navigation lambda arguments which are just wrappers around the *NavController* extension functions mentioned above
- *NavOptions*
  - Used by *navigateTo-* extension functions to create specified transitions between *Routes*
    - Includes: Screen transition animations, popping the navigation backstack, and saving or restoring previous state
- *NavHost*
  - A Composable function that acts as the interceptor to *Route* navigation requests via navigation request lambdas
  - Provides all *Routes* with relevant navigation request lambdas (wrappers of *navigateTo-* extension functions)
  - Supplies arguments from *RouteArgs* to their associated *Route*
- *NavHostController* (Subclass of NavController, no special use cases in this application)

### Modules
- :app
  - Application, UI, ViewModels, Navigation
  - Most likely module to contain code that has yet to find a better home
- :core:database
  - Room Persistence Library Database, Entities, DAOs
- :core:datastore
  - Proto DataStore .proto files, Serializers, DAOs
- :core:data
  - Repository abstraction layer isolating data sources from :app module
- :core:common
  - Utility functions & classes used by multiple modules
- :core:ml
  - ML Kit related files like SegmentedImage which uses Subject Segmentation
- :core:model
  - Classes & interfaces that abstract data passed between modules
    - :app can indirectly communicate with :core:datastore without requiring it to become a dependency
- :core:ui
  - Reusable Compose UI components that are not tied to any particular screen
    - On top of being convenient when creating new screens, it also serves as the foundation for the general design system 
    of Merlinsbag.
- :core:common
  - Generic helper & utility classes or functions used in multiple modules

### Technology
- ML Kit
- Room Persistence Library (SQLite wrapper for Android)
- Proto DataStore
- Hilt (Dagger2 wrapper for Android)
- Jetpack Compose
  - Navigation with Compose
  - Material 3 Composables & Icons
- Kotlin Flow
- Kotlin Coroutines
- Firebase Crashlytics
- JUnit
- Robolectric

### Documentation
#### [Conventions](Conventions.md)
#### [I Forgot How To (solutions for infrequent workflows & issues)](I_Forgot_How_To.md)