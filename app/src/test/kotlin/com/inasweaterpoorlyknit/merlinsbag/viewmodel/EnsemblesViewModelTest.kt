package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyEnsembleThumbnails
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

class EnsemblesViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var ensembleRepository: EnsembleRepository
  @MockK lateinit var articleRepository: ArticleRepository

  lateinit var viewModel: EnsemblesViewModel

  companion object {
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
    val allEnsembleThumbnails = List(10){ ensembleIndex ->
      LazyEnsembleThumbnails(
        ensemble = Ensemble(
          id = "ensembleId $ensembleIndex",
          title = "testEnsemble $ensembleIndex"
        ),
        thumbnails = allArticleThumbnails,
      )
    }
  }

  @Before
  fun beforeEach() = runTest {
    every { ensembleRepository.getAllEnsembleArticleThumbnails() } returns flowOf(allEnsembleThumbnails)
    every { ensembleRepository.searchEnsembleArticleThumbnails(any()) } returns flowOf(allEnsembleThumbnails)
    every { articleRepository.getAllArticlesWithThumbnails() } returns flowOf(allArticleThumbnails)
    viewModel = EnsemblesViewModel(
      ensembleRepository = ensembleRepository,
      articleRepository = articleRepository,
    )
    viewModel.lazyEnsembles.first()
    viewModel.addArticleThumbnails.first()
  }

  @Test
  fun `Dialogs are hidden by default`() = runTest {
    assertEquals(EnsemblesViewModel.DialogState.None, viewModel.dialogState)
  }
}
