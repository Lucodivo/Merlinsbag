package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleCount
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StatisticsViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var ensembleRepository: EnsembleRepository
  @MockK lateinit var articleRepository: ArticleRepository

  lateinit var viewModel: StatisticsViewModel

  companion object {
    val articleCount = 100
    val articleImagesCount = articleCount + 50
    val ensembleCount = 70
    val popularArticleThumbnails = LazyArticleThumbnails(
      directory = "file://",
      articleThumbnailPaths = List(1){ articleIndex ->
        ArticleWithThumbnails(
          articleId = "articleId $articleIndex",
          thumbnailPaths = List(10){ thumbnailIndex ->
            ThumbnailFilename(
              filenameThumb = "articleId $articleIndex thumb $thumbnailIndex"
            )
          }
        )
      },
    )
    val ensembleArticleCounts = List(10){ ensembleIndex ->
      EnsembleArticleCount(
        title = "ensembleTitle $ensembleIndex",
        count = ensembleIndex.toLong(),
      )
    }
    val mostPopularArticleEnsemblesCount = Pair(
      listOf(0, 1, 2),
      popularArticleThumbnails
    )
  }

  @Before
  fun beforeEach() = runTest {
    coEvery { articleRepository.getCountArticles() } returns flowOf(articleCount)
    coEvery { articleRepository.getCountArticleImages() } returns flowOf(articleImagesCount)
    coEvery { articleRepository.getMostPopularArticlesImageCount(any()) } returns flowOf(popularArticleThumbnails)
    coEvery { ensembleRepository.getCountEnsembles() } returns flowOf(ensembleCount)
    coEvery { ensembleRepository.getMostPopularEnsembles(any()) } returns flowOf(ensembleArticleCounts)
    coEvery { ensembleRepository.getMostPopularArticlesEnsembleCount(any()) } returns flowOf(mostPopularArticleEnsemblesCount)
    viewModel = StatisticsViewModel(
      ensembleRepository = ensembleRepository,
      articleRepository = articleRepository,
    )
    viewModel.uiState.first()
  }

  @Test
  fun `UI state`() = runTest {
    assertEquals(articleCount, viewModel.uiState.value.articleCount)
    assertEquals(articleImagesCount, viewModel.uiState.value.articleImageCount)
    assertEquals(ensembleCount, viewModel.uiState.value.ensembleCount)
    assertEquals(ensembleArticleCounts, viewModel.uiState.value.ensemblesWithMostArticles)
    assertEquals(popularArticleThumbnails.getUriStrings(0), viewModel.uiState.value.articleWithMostImagesUriStrings)
    assertEquals(
      StatisticsViewModel.ArticleWithMostEnsembles(
        mostPopularArticleEnsemblesCount.first[0],
        mostPopularArticleEnsemblesCount.second.getUriStrings(0)
      ),
      viewModel.uiState.value.articleWithMostEnsembles
    )
  }
}