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

Merlinsbag is a single activity, no fragment, Android application. Standard UI and navigation are all accomplished using
Jetpack Compose and Navigation with Compose. The important keywords to look for to understand navigation within the application
 are: *NavHost*, *NavController*, *NavHostController*, *Route*, *navigateTo*, *navOptions*.

- NavHost
  - A Composable function that acts as the interceptor to route navigation requests sent to the NavController
  - Defines arguments for specific routes and pulls arguments out from bundles to pass to the next screen
- NavController
  - NavController extension functions created per screen to simplify the construction of Routes
  - Used by Routes to request the navigation to some other screen
- NavHostController *(Subclass of NavController, no special use cases in this application)*
- Route
  - A Composable function that acts as a wrapper around some Composable screen
  - Acquires and maintains the NavController and ViewModel
    - Rarely also maintains ActivityResultContracts
  - As much state as possible is hoisted all the way up to the Route 
    - Most importantly, this allows Screens can be easily verified using @Preview annotated Composable functions
- navigateTo-
  - Prefix to NavController extension functions for requesting the navigation to some specified Route represented by the suffix
- NavOptions
  - Used by navigateTo- extension functions to create fancier transitions between Routes.
    - Includes: Animations, Popping the Backstack, restoring previous state

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
- Hilt (Dagger2 wrapper for Android)
- Jetpack Compose
  - Navigation with Compose
  - Material 3 Composables & Icons
- Kotlin Flow
- Kotlin Coroutines
- Firebase Crashlytics

### Conventions

Disclaimer: These are "conventions", not "best practices". Conventions are not always the optimal way
to program or structure code (nor are "best practices", for that matter...). But the aim here is to stick
with known good practices to produce a more cohesive codebase. "Convention"/"Practice" is the goal. *Not* "best".
As even the worst practice will become easy to reason about if used consistently. And a consistent pattern is 
easier to refactor when discovering they are indeed a bad practice.

#### Test Structure

Tests are structured using the Arrange-Act-Assert pattern.
1) *Arrange* the initial state, data, and measurements for the test.
2) Perform the *Actions* that are under test.
3) Measure and *Assert* the initial and expected results.

**Desired Outcome**: Both assertions and setup can build up and harm the readable purpose of the test. Although a great test function
name can go a long way, isolating the actions-under-test clearly demonstrates the intention of said test.
**Note**: This convention perfectly demonstrates the difference between "convention" and "best practice". Why not assert initial state
during the *Arrange* portion? Why take measurements in the *Assert* portion? For the first question, maybe it seems counterintuitive to 
assert outside of the *Assert* portion? For the second question, maybe it's fair to say that the purity of the *Action* portion is the main
appeal of the convention. However, the best answer to both of these questions is that it has simply been chosen to be convention.

Ex: 
```
@Test
fun purgeDatabase() = runBlocking{
  val entityCount = 10
  articleDao.insertArticles(*createArticleEntity(entityCount))
  val allArticlesBefore = articleDao.getArticlesCount().first()

  purgeDao.purgeDatabase()

  val allArticlesAfter = articleDao.getArticlesCount().first()
  assertEquals(0, allArticlesAfter)
  assertEquals(entityCount, allArticlesBefore)
}
```

#### Compose Preview naming convention: Preview- prefix

**Desired Outcome**: Function names that scream "I am a compose preview" for improved code completion and searchability. 
```
@Preview
@Composable
fun PreviewXxxXxx(){
    AppTheme {
        XxxXxx()
    }
}
```

#### Composable components should not contain default or nullable parameters for the *sole* benefit of composable previews
**Desired Outcome**: Compilation errors for custom composable components with missing or unsuitable parameters.  
**Cons**: Slightly longer to write previews. Slightly longer when refactoring custom composable components.  
**Additional**: Helper preview functions with default values can help reduce the pain of the cons.

```
@Composable
fun ProductionCameraButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
){
    // ...
}

@Preview
@Composable
fun PreviewProductionButtion(){
    AppTheme {
        ProductionButtion(
            icon = ComposePreviewIcons.Add
            contentDescription = COMPOSE_PREVIEW_CONTENT_DESCRIPTION,
            onClick = {},
        )
    }
}
```
