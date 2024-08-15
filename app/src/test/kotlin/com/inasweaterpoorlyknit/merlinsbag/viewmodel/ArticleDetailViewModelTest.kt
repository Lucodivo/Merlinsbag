package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.model.LazyArticlesWithImages
import com.inasweaterpoorlyknit.core.data.model.LazyFilenames
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.ImageFilenames
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ArticleDetailViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK
  lateinit var articleRepository: ArticleRepository

  @MockK
  lateinit var ensembleRepository: EnsembleRepository

  lateinit var viewModel: ArticleDetailViewModel

  companion object {
    const val testArticleIndex = 1
    val testEnsembles = List(10){ index ->
      Ensemble(
        id = "ensembleId $index",
        title = "Goth $index Boss",
      )
    }
    val testArticlesWithImages = LazyArticlesWithImages(
      directory = "file://",
      articlesWithImages = List(10){ articleIndex ->
      ArticleWithImages(
        articleId = "articleId $articleIndex",
        imagePaths = List(2){ imageIndex ->
          ImageFilenames(
            filename = "article $articleIndex image $imageIndex",
            filenameThumb = "article $articleIndex thumb $imageIndex",
          )
        }
      )
    })
  }

  @Before
  fun setup() = runTest {
    val ensembleId = null
    coEvery { articleRepository.getArticlesWithImages(ensembleId = ensembleId) } returns flowOf(testArticlesWithImages)
    coEvery { ensembleRepository.getAllEnsembles() } returns flowOf(testEnsembles)
    viewModel = ArticleDetailViewModel(
      ensembleId = ensembleId,
      articleIndex = testArticleIndex,
      articleRepository = articleRepository,
      ensembleRepository = ensembleRepository,
    )
    viewModel.lazyArticleFilenames.first()
    viewModel.filter.first()
  }

  @Test
  fun `Dialogs are hidden by default`() = runTest {
    assertEquals(ArticleDetailViewModel.AlertDialogState.None, viewModel.alertDialogState)
  }

  @Test
  fun `Delete article displays dialog`() = runTest {
    viewModel.onClickDelete()

    coVerify(exactly = 0) { articleRepository.deleteArticle(any()) }
    assertEquals(ArticleDetailViewModel.AlertDialogState.DeleteArticle, viewModel.alertDialogState)
  }

  @Test
  fun `Delete all article images displays delete article dialog`() = runTest {
    viewModel.onLongPressArticleThumbnail(0)
    viewModel.onLongPressArticleThumbnail(1)
    viewModel.onClickRemoveImages()

    coVerify(exactly = 0) { articleRepository.deleteArticle(any()) }
    coVerify(exactly = 0) { articleRepository.deleteArticleImages(any()) }
    assertEquals(ArticleDetailViewModel.AlertDialogState.DeleteArticleByRemovingAllArticles, viewModel.alertDialogState)
  }

  @Test
  fun `Export permissions dialog`() = runTest {
    viewModel.neverAskExportPermissionAgain()

    coVerify(exactly = 0) { articleRepository.exportImage(any()) }
    assertEquals(ArticleDetailViewModel.AlertDialogState.ExportPermissions, viewModel.alertDialogState)
  }

  @Test
  fun `Remove ensembles dialog`() = runTest {
    viewModel.onLongPressEnsemble(0)
    viewModel.onClickRemoveFromEnsembles()

    coVerify(exactly = 0) { ensembleRepository.deleteArticlesFromEnsemble(any(), any()) }
    assertEquals(ArticleDetailViewModel.AlertDialogState.RemoveFromEnsembles, viewModel.alertDialogState)
  }

  @Test
  fun `Remove images dialog`() = runTest {
    viewModel.onLongPressArticleThumbnail(0)
    viewModel.onClickRemoveImages()

    coVerify(exactly = 0) { articleRepository.deleteArticleImages(any()) }
    assertEquals(ArticleDetailViewModel.AlertDialogState.RemoveImages, viewModel.alertDialogState)
  }
}