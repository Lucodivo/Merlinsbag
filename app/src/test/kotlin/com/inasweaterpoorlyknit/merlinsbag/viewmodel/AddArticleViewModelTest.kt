package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.net.Uri
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.ml.image.SegmentedImage
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddArticleViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var articleRepository: ArticleRepository
  @MockK lateinit var userPreferencesRepository: UserPreferencesRepository
  @MockK lateinit var application: Application
  lateinit var segmentedImage: SegmentedImage

  lateinit var viewModel: AddArticleViewModel

  companion object {
    val articleId: String? = null
    val imageUriStrings = listOf(
      "file://cat.jpg"
    )
    val allArticlesWithThumbnails: LazyArticleThumbnails = LazyArticleThumbnails(
      directory = "file://",
      articleThumbnailPaths = List(10){ articleIndex ->
        ArticleWithThumbnails(
          articleId = "articleId $articleIndex",
          thumbnailPaths = List(2){ thumbIndex ->
            ThumbnailFilename(
              filenameThumb = "articleID $articleIndex thumb $thumbIndex"
            )
          }
        )
      }
    )
  }

  @Before
  fun setup() = runTest {
    mockkStatic(Uri::class)
    val uriMock = mockk<Uri>()
    every { Uri.parse(any()) } returns uriMock

    mockkStatic(Bitmap::class)
    val bitmap = mockk<Bitmap>()
    every { Bitmap.createBitmap(any<Int>(), any<Int>(), any<Config>(), any<Boolean>()) } returns bitmap
    segmentedImage = mockk<SegmentedImage>()

    coEvery { articleRepository.getAllArticlesWithThumbnails() } returns flowOf(allArticlesWithThumbnails)
    justRun { segmentedImage.cleanup() }
    justRun { segmentedImage.process(any(), any(), any()) }
    viewModel = AddArticleViewModel(
      imageUriStrings = imageUriStrings,
      articleId = articleId,
      application = application,
      articleRepository = articleRepository,
      userPreferencesRepository = userPreferencesRepository,
      segmentedImage = segmentedImage,
    )
    viewModel.attachArticleThumbnails.first()
  }

  @Test
  fun `Dialogs are hidden by default`() = runTest {
    assertEquals(AddArticleViewModel.DialogState.None, viewModel.dialogState)
  }
}