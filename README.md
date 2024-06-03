# Merlinsbag

<a href="https://play.google.com/apps/internaltest/4701048209300859521"><img alt="Download" src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/logo/merlin_logo_96x96_rounded.png"></a><br>
<a href="https://play.google.com/apps/internaltest/4701048209300859521"><img alt="Download" src="https://img.shields.io/badge/Google%20Play-%20?logo=googleplay&amp;color=grey"></a>

Merlinsbag is an Android application for virtualizing & organizing a user's wardrobe and favorite items.

## Features

### Adding Articles
"Articles" refers to an item that a user has digitized, stemming from an "article of clothing".  
Users can provide photos of the things they love and the app will utilize ML Kit's Subject Segmentation to provide a stencil. Due to imperfections in the model's output, the user can adjust the tightness of the stencil around the isolated subject. The user can also rotate the image by 90 or 180 degrees.

### Adding Ensembles

"Ensembles" refers to a collection of articles.
Users can create titled ensembles that contains any articles currently available in their catalog.

### Ensembles
Ensembles can be viewed as a list displaying a short (sometimes truncated) view of the ensemble's articles.

### Articles
All articles can be viewed as a catalog, where they can be deleted if chosen

### Article Detail
Articles can be viewed in their full glory and navigated through swiping based on selected filters.

### Screenshots

<p float="left">
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Articles.jpg" alt="Articles Screen" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/ArticleDetail.jpg" alt="Article Detail Screen" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/AddArticle.jpg" alt="Add Article Screen" width="200"/><br>
</p>
<p float="left">
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/Ensembles.jpg" alt="Ensembles Screen" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/AddEnsemble.jpg" alt="Add Ensemble Dialog" width="200"/>
  <img src="https://raw.githubusercontent.com/Lucodivo/RepoSampleImages/master/Merlinsbag/EnsembleDetail.jpg" alt="EnsembleDetail" width="200"/>
</p>

## Codebase

### Terminology

- "Noop" has no meaning and is a simple prefix for application specific implementations.
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
  - Most likely candidate to contain code that has not yet found a better home.
- :core:database
  - Room Database, Entities, DAOs
- :core:common
  - Utility functions & classes used by multiple modules
- :core:repository
  - Repositories defining a ViewModel's access to the data

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