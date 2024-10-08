package com.inasweaterpoorlyknit.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inasweaterpoorlyknit.core.database.NoopDatabase
import com.inasweaterpoorlyknit.core.data.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.testing.createFakeUriString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class EnsembleRepositoryTests {
  private lateinit var articleRepository: ArticleRepository
  private lateinit var ensembleRepository: EnsembleRepository
  private lateinit var database: NoopDatabase

  @Before
  fun beforeEach() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, NoopDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    articleRepository = ArticleRepository(
      context = context,
      articleDao = database.ArticleDao(),
      ensembleDao = database.EnsembleDao(),
    )
    ensembleRepository = EnsembleRepository(
      context = context,
      ensembleDao = database.EnsembleDao(),
      articleDao = database.ArticleDao(),
    )
  }

  @After
  @Throws(IOException::class)
  fun afterEach() { database.close() }

  @Test
  @Throws(Exception::class)
  fun insertArticle() {
    // arrange
    val ensembleSizes = Array(4){i -> i + 1}
    for(i in 1..ensembleSizes.max()){
      val fullImageUris = createFakeUriString()
      val thumbnailImageUris = createFakeUriString()
      articleRepository.insertArticle(fullImageUris, thumbnailImageUris)
    }
    var articleIds: List<String>
    runBlocking {
      val lazyArticlesWithImages = articleRepository.getAllArticlesWithThumbnails().first()
      articleIds = lazyArticlesWithImages.articleIds()
    }

    // act
    for (ensembleSize in ensembleSizes) {
      ensembleRepository.insertEnsemble("ensemble$ensembleSize", articleIds.slice(0..<ensembleSize))
    }
    var ensembles: List<LazyEnsembleThumbnails>
    runBlocking { ensembles = ensembleRepository.getAllEnsembleArticleThumbnails().first() }

    // assert
    assertEquals(ensembleSizes.size, ensembles.size)
    // NOTE: Ordered by newest insertion first (modified_by)
    assertEquals(ensembles[0].thumbnails.size, ensembleSizes[3])
    assertEquals(ensembles[1].thumbnails.size, ensembleSizes[2])
    assertEquals(ensembles[2].thumbnails.size, ensembleSizes[1])
    assertEquals(ensembles[3].thumbnails.size, ensembleSizes[0])
  }
}
