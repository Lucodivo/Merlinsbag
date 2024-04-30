package com.inasweaterpoorlyknit.inknit.navigation

import android.Manifest.permission
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.RowScope
import com.inasweaterpoorlyknit.inknit.common.toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import androidx.tracing.trace
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.AddArticleScreen
import com.inasweaterpoorlyknit.inknit.ui.ArticleDetailScreen
import com.inasweaterpoorlyknit.inknit.ui.ArticlesScreen
import com.inasweaterpoorlyknit.inknit.ui.CameraFragment
import com.inasweaterpoorlyknit.inknit.ui.CameraScreen
import com.inasweaterpoorlyknit.inknit.ui.getActivity
import com.inasweaterpoorlyknit.inknit.ui.icons.InKnitIcons
import com.inasweaterpoorlyknit.inknit.viewmodels.AddArticleViewModel
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticleDetailViewModel
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticlesViewModel
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Locale

private val REQUIRED_PERMISSIONS =
  if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
    arrayOf(permission.CAMERA)
  } else {
    arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
  }

//region REGISTER FOR ACTIVITY RESULTS

// Get image already saved on phone
//endregion REGISTER FOR ACTIVITY RESULTS

object ROUTES {
  const val ARTICLES = "articles_route"
  const val CAMERA = "camera_route"

  const val IMAGE_URI_STRING_ARG = "imageUriString"
  const val ADD_ARTICLES_BASE = "add_articles_route"
  const val ADD_ARTICLES_ROUTE = "$ADD_ARTICLES_BASE?$IMAGE_URI_STRING_ARG={$IMAGE_URI_STRING_ARG}"

  const val ARTICLE_ID_ARG = "articleId"
  const val ARTICLE_DETAIL_ROUTE_BASE = "article_detail_route"
  const val ARTICLE_DETAIL_ROUTE = "$ARTICLE_DETAIL_ROUTE_BASE?$ARTICLE_ID_ARG={$ARTICLE_ID_ARG}"

  const val OUTFITS = "outfits_route"
}

fun NavController.navigateToArticles(){
  navigate(ROUTES.ARTICLES) { launchSingleTop = true }
}
fun NavController.navigateToCamera(navOptions: NavOptions? = null) = navigate(ROUTES.CAMERA, navOptions)
fun NavController.navigateToAddArticle(uriString: String, navOptions: NavOptions? = null){
  val route = "${ROUTES.ADD_ARTICLES_BASE}?${ROUTES.IMAGE_URI_STRING_ARG}=$uriString"
  navigate(route, navOptions)
}
fun NavController.navigateToArticleDetail(clothingArticleId: String, navOptions: NavOptions? = null){
  val route = "${ROUTES.ARTICLE_DETAIL_ROUTE_BASE}?${ROUTES.ARTICLE_ID_ARG}=$clothingArticleId"
  navigate(route, navOptions)
}
fun NavController.navigateToOutfits(navOptions: NavOptions? = null) = navigate(ROUTES.OUTFITS, navOptions)

@Composable
fun ArticlesRoute(
  navController: NavController,
  modifier: Modifier = Modifier,
  articlesViewModel: ArticlesViewModel = hiltViewModel(), // MainMenuViewModel
){
  val thumbnailDetails = articlesViewModel.thumbnailDetails.observeAsState()
  val _appSettingsLauncher = rememberLauncherForActivityResult(StartActivityForResult()){}
  val _photoAlbumLauncher = rememberLauncherForActivityResult(GetContent()){ uri ->
    if(uri != null) {
      navController.navigateToAddArticle(uri.toString())
    } else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
  }
  val _cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()){ permissions ->
    fun openAppSettings() = _appSettingsLauncher.launch(
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", navController.context.packageName, null)
      }
    )

    val context = navController.context
    var permissionsGranted = true
    var userCheckedNeverAskAgain = false
    permissions.entries.forEach { entry ->
      if(!entry.value) {
        userCheckedNeverAskAgain = !shouldShowRequestPermissionRationale(navController.context.getActivity()!!, entry.key)
        permissionsGranted = false
      }
    }
    if(permissionsGranted) {
      navController.navigateToCamera()
    } else {
      if(userCheckedNeverAskAgain) {
        AlertDialog.Builder(context)
          .setTitle(context.getString(R.string.permission_alert_title))
          .setMessage(context.getString(R.string.permission_alert_justification))
          .setNegativeButton(context.getString(R.string.permission_alert_negative)){ _, _ -> }
          .setPositiveButton(context.getString(R.string.permission_alert_positive)){ _, _ -> openAppSettings() }
          .show()
      } else {
        navController.context.toast("Camera permissions required")
      }
    }
  }
  ArticlesScreen(
    thumbnailUris = thumbnailDetails.value?.map { it.thumbnailUri } ?: emptyList(),
    onClickArticle = { thumbnailIndex ->
      navController.navigateToArticleDetail(thumbnailDetails.value!![thumbnailIndex].articleId)
    },
    onClickAddPhotoAlbum = {
      _photoAlbumLauncher.launch("image/*")
    },
    onClickAddPhotoCamera = {
      _cameraWithPermissionsCheckLauncher.launch(REQUIRED_PERMISSIONS)
    }
  )
}

@Composable
fun ArticleDetailRoute(
  navController: NavController,
  clothingArticleId: String,
  modifier: Modifier = Modifier,
  articleDetailViewModel: ArticleDetailViewModel = hiltViewModel(), // MainMenuViewModel
){
  val clothingDetail = articleDetailViewModel.getArticleDetails(clothingArticleId).observeAsState(initial = null)
  ArticleDetailScreen(
    imageUriString = clothingDetail.value?.imageUriString,
  )
}

@Composable
fun CameraRoute(
  navController: NavController,
  modifier: Modifier = Modifier,
  articleDetailViewModel: ArticleDetailViewModel = hiltViewModel(), // MainMenuViewModel
){
  val TAG = CameraFragment::class.simpleName
  val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
  val imageCapture = remember {
    ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
      .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
      .setJpegQuality(100)
      .build()
  }
  fun takePhoto() {
    // Get a stable reference of the modifiable image capture use case
    val context = navController.context

    // Create time stamped name and MediaStore entry.
    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
      .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, name)
      put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
      if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/InKnit")
      }
    }

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions
      .Builder(context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues)
      .build()

    // Set up image capture listener, which is triggered after photo has
    // been taken
    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
      override fun onError(exc: ImageCaptureException) {
        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
      }
      override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
        val uriString = output.savedUri.toString()
        navController.navigateToAddArticle(uriString)
      }
    })
  }
  CameraScreen(
    imageCapture = imageCapture,
    onClick = ::takePhoto,
  )
}

@Composable
fun AddArticleRoute(
  navController: NavController,
  imageUriString: String,
){
  val addArticleViewModel = hiltViewModel<AddArticleViewModel, AddArticleViewModel.AddArticleViewModelFactory>{ factory ->
    factory.create(imageUriString)
  }

  val shouldCloseEvent = addArticleViewModel.shouldClose.observeAsState()
  shouldCloseEvent.value?.getContentIfNotHandled()?.let { shouldClose ->
    if(shouldClose) navController.popBackStack()
  }

  AddArticleScreen(
    processing = addArticleViewModel.processing.value,
    processedImage = addArticleViewModel.processedBitmap.value,
    imageRotation = addArticleViewModel.rotation.floatValue,
    onNarrowFocusClick = { addArticleViewModel.onFocusClicked() },
    onBroadenFocusClick = { addArticleViewModel.onWidenClicked() },
    onPrevClick = { addArticleViewModel.onPrevClicked() },
    onNextClick = { addArticleViewModel.onNextClicked() },
    onRotateCW = { addArticleViewModel.onRotateCW() },
    onRotateCCW = { addArticleViewModel.onRotateCCW() },
    onSave = { addArticleViewModel.onSave() },
  )
}

enum class TopLevelDestination(
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val iconTextId: Int,
) {
  COLLECTIONS(
    selectedIcon = InKnitIcons.Collections,
    unselectedIcon = InKnitIcons.CollectionsBorder,
    iconTextId = R.string.collections,
  ),
  ARTICLES(
    selectedIcon = InKnitIcons.Articles,
    unselectedIcon = InKnitIcons.ArticlesBorder,
    iconTextId = R.string.articles,
  ),
}

@Composable
fun rememberInKnitAppState(
  windowSizeClass: WindowSizeClass,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  navController: NavHostController = rememberNavController(),
): InKnitAppState {
  return remember(
    navController,
    coroutineScope,
    windowSizeClass,
  ) {
    InKnitAppState(
      navController = navController,
      coroutineScope = coroutineScope,
      windowSizeClass = windowSizeClass,
    )
  }
}

@Stable
class InKnitAppState(
  val navController: NavHostController,
  coroutineScope: CoroutineScope,
  val windowSizeClass: WindowSizeClass,
){
  val currentDestination: NavDestination?
    @Composable get() = navController
      .currentBackStackEntryAsState().value?.destination

  /**
   * UI logic for navigating to a top level destination in the app. Top level destinations have
   * only one copy of the destination of the back stack, and save and restore state whenever you
   * navigate to and from it.
   *
   * @param topLevelDestination: The destination the app needs to navigate to.
   */
  fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
    trace("Navigation: ${topLevelDestination.name}") {
      val topLevelNavOptions = navOptions {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
          saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
      }

      when (topLevelDestination) {
        TopLevelDestination.COLLECTIONS -> navController.navigateToOutfits(topLevelNavOptions)
        TopLevelDestination.ARTICLES -> navController.navigateToArticles()
      }
    }
  }
}

@Composable
fun InKnitNavHost(
  appState: InKnitAppState,
  modifier: Modifier = Modifier,
  startDestination: String = ROUTES.ARTICLES,
) {
  val navController = appState.navController
  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = modifier,
  ){
    composable(
      route = ROUTES.ARTICLES
    ) {
      ArticlesRoute(navController = navController)
    }
    composable(
      route = ROUTES.ARTICLE_DETAIL_ROUTE,
      arguments = listOf(
        navArgument(ROUTES.ARTICLE_ID_ARG) { nullable = false; type = NavType.StringType },
      ),
    ) { navBackStackEntry ->
      val articleIdArg = navBackStackEntry.arguments!!.getString(ROUTES.ARTICLE_ID_ARG)!!
      ArticleDetailRoute(navController = navController, clothingArticleId = articleIdArg)
    }
    composable(route = ROUTES.CAMERA){
      CameraRoute(navController = navController)
    }
    composable(
      route = ROUTES.ADD_ARTICLES_ROUTE,
      arguments = listOf(
        navArgument(ROUTES.IMAGE_URI_STRING_ARG) { nullable = false; type = NavType.StringType },
      ),
    ) { navBackStackEntry ->
      val imageUriStringArg = navBackStackEntry.arguments!!.getString(ROUTES.IMAGE_URI_STRING_ARG)!!
      AddArticleRoute(navController = navController, imageUriString = imageUriStringArg)
    }
  }
}

object InKnitNavigationDefaults {
  @Composable fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant
  @Composable fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer
  @Composable fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun RowScope.InKnitNavigationBarItem(
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  alwaysShowLabel: Boolean = true,
  icon: @Composable () -> Unit,
  selectedIcon: @Composable () -> Unit = icon,
  label: @Composable (() -> Unit)? = null,
) {
  NavigationBarItem(
    selected = selected,
    onClick = onClick,
    icon = if (selected) selectedIcon else icon,
    modifier = modifier,
    enabled = enabled,
    label = label,
    alwaysShowLabel = alwaysShowLabel,
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = InKnitNavigationDefaults.navigationSelectedItemColor(),
      unselectedIconColor = InKnitNavigationDefaults.navigationContentColor(),
      selectedTextColor = InKnitNavigationDefaults.navigationSelectedItemColor(),
      unselectedTextColor = InKnitNavigationDefaults.navigationContentColor(),
      indicatorColor = InKnitNavigationDefaults.navigationIndicatorColor(),
    ),
  )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
  this?.hierarchy?.any {
    it.route?.contains(destination.name, true) ?: false
  } ?: false

@Composable
private fun InKnitBottomBar(
  destinations: List<TopLevelDestination>,
  onNavigateToDestination: (TopLevelDestination) -> Unit,
  currentDestination: NavDestination?,
  modifier: Modifier = Modifier,
) {
  NavigationBar(
    modifier = modifier,
  ) {
    destinations.forEach { destination ->
      val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
      InKnitNavigationBarItem(
        selected = selected,
        onClick = { onNavigateToDestination(destination) },
        icon = { Icon(imageVector = destination.unselectedIcon, contentDescription = null) },
        selectedIcon = { Icon(imageVector = destination.selectedIcon, contentDescription = null) },
        label = { Text(stringResource(destination.iconTextId)) },
        modifier = Modifier,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun InKnitApp(
  appState: InKnitAppState,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxSize(),
  ) {
    CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
      InKnitNavHost(
        appState = appState,
        modifier = modifier,
      )
    }
  }
}