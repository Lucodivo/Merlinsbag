package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EnsembleDetailViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var ensembleRepository: EnsembleRepository
  @MockK lateinit var articleRepository: ArticleRepository

  lateinit var viewModel: EnsembleDetailViewModel

  companion object {
    val testEnsemble = Ensemble(
      id = "1234",
      title = "testEnsemble"
    )
    val allArticleThumbnails = LazyArticleThumbnails(
      directory = "file://",
      articleThumbnailPaths = List(10){ articleIndex ->
        ArticleWithThumbnails(
          articleId = "articleId $articleIndex",
          thumbnailPaths = List(2){ thumbnailIndex ->
            ThumbnailFilename(
              filenameThumb = "articleId $articleIndex thumb $thumbnailIndex"
            )
          }
        )
      },
    )
    val ensembleArticleThumbnails = LazyArticleThumbnails(
      directory = allArticleThumbnails.directory,
      articleThumbnailPaths = allArticleThumbnails.paths.subList(0, 2)
    )
  }

  @Before
  fun beforeEach() = runTest {
    every { ensembleRepository.getEnsemble(testEnsemble.id) } returns flowOf(testEnsemble)
    every { ensembleRepository.getEnsembleArticleThumbnails(testEnsemble.id) } returns flowOf(ensembleArticleThumbnails)
    every { articleRepository.getAllArticlesWithThumbnails() } returns flowOf(allArticleThumbnails)
    viewModel = EnsembleDetailViewModel(
      ensembleId = testEnsemble.id,
      ensemblesRepository = ensembleRepository,
      articleRepository = articleRepository,
    )
    viewModel.ensembleUiState.first()
    viewModel.ensembleTitle.first()
  }

  @Test
  fun `Dialogs are hidden by default`() = runTest {
    assertEquals(EnsembleDetailViewModel.DialogState.None, viewModel.dialogState)
  }
}
