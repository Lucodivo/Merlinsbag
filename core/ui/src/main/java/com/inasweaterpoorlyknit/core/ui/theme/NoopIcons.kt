package com.inasweaterpoorlyknit.core.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Rotate90DegreesCcw
import androidx.compose.material.icons.outlined.Rotate90DegreesCw
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.ZoomInMap
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.inasweaterpoorlyknit.core.ui.R

object NoopIcons {
  val Close = Icons.Filled.Close
  val AddPhotoAlbum = Icons.Filled.AddPhotoAlternate
  val AddPhotoCamera = Icons.Filled.AddAPhoto
  val Remove = Icons.Filled.Remove
  val Edit = Icons.Filled.Edit
  val Add = Icons.Filled.Add
  val Attachment = Icons.Filled.Attachment
  val MoreHorizontal = Icons.Filled.MoreHoriz
  val FocusBroaden = Icons.Outlined.ZoomOutMap
  val FocusNarrow = Icons.Outlined.ZoomInMap
  val RotateCCW = Icons.Outlined.Rotate90DegreesCcw
  val Delete = Icons.Outlined.Delete
  val DeleteForever = Icons.Outlined.DeleteForever
  val Cancel = Icons.Outlined.Cancel
  val RotateCW = Icons.Outlined.Rotate90DegreesCw
  val Check = Icons.Outlined.Check
  val Items = Icons.Outlined.Category
  val ItemsSelected = Icons.Filled.Category
  @Composable fun ensembles() = ImageVector.vectorResource(R.drawable.hashtag)
  @Composable fun ensemblesSelected() = ImageVector.vectorResource(R.drawable.hashtag_heavy)
  val Label = Icons.AutoMirrored.Outlined.Label
  val LabelSelected = Icons.AutoMirrored.Filled.Label
  val SelectableIndicator = Icons.Outlined.Circle
  val SelectedIndicator = Icons.Filled.CheckCircle
  val Settings = Icons.Filled.Settings
  val Web = Icons.Filled.Web
  val Code = Icons.Filled.Code
  val Clean = Icons.Filled.CleaningServices
  val Key = Icons.Filled.Key
  val Lock = Icons.Filled.Lock
  val Download = Icons.Filled.Download
  val DarkMode = Icons.Filled.DarkMode
  val LightMode = Icons.Filled.LightMode
  val Search = Icons.Filled.Search
  @Composable fun systemMode() = ImageVector.vectorResource(R.drawable.night_sight_auto)
}