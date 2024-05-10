package com.inasweaterpoorlyknit.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class DatabaseArticleWithImagesTests {
    // TODO: Remove DAO, test through repository only
    private lateinit var articleDao: ArticleDao
    private lateinit var articleRepository: ArticleRepository
    private lateinit var database: InKnitDatabase

    // NOTE: Used to observeForever on the main thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, InKnitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        articleDao = database.ArticleDao()
        articleRepository = ArticleRepository(
            articleDao = articleDao
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() { database.close() }

    @Test
    @Throws(Exception::class)
    fun insertArticle() {
        // arrange
        val fullImageUris = createFakeUriStrings(2)
        val thumbnailUriImageUris = createFakeUriStrings(2)

        // act
        articleRepository.insertArticle(fullImageUris[0], thumbnailUriImageUris[0])
        articleRepository.insertArticle(fullImageUris[1], thumbnailUriImageUris[1])
        val articlesWithImages: List<ArticleWithImages>
        runBlocking {
            articlesWithImages = articleRepository.getAllArticlesWithImages().first()
        }

        // assert
        assertEquals("articles not properly inserted and retreived", fullImageUris.size, articlesWithImages.size)
    }

    // TODO: Adapt this test for usage of Repository only
    @Test
    @Throws(Exception::class)
    fun insertArticleWithImages() {
        // arrange
        val articlesToInsert = createArticleEntity(3)
        val articleImagesToInsert1 = createArticleImageEntity(ArticleId = articlesToInsert[0].id)
        val articleImagesToInsert2 = createArticleImageEntity(2, ArticleId = articlesToInsert[1].id)
        val articleImagesToInsert3 = createArticleImageEntity(3, ArticleId = articlesToInsert[2].id)

        // act
        articleDao.insertArticles(*articlesToInsert)
        articleDao.insertArticleImages(articleImagesToInsert1)
        articleDao.insertArticleImages(*articleImagesToInsert2)
        articleDao.insertArticleImages(*articleImagesToInsert3)
        val articlesWithImages1 = LiveDataTestUtil<ArticleWithImages>()
            .getValue(articleDao.getArticleWithImages(articlesToInsert[0].id))
        val articlesWithImages2 = LiveDataTestUtil<ArticleWithImages>()
            .getValue(articleDao.getArticleWithImages(articlesToInsert[1].id))
        val articlesWithImages3 = LiveDataTestUtil<ArticleWithImages>()
            .getValue(articleDao.getArticleWithImages(articlesToInsert[2].id))

        // assert
        assertEquals("Could not acquire article with images", 1, articlesWithImages1.images.size)
        assertEquals("article with images contains wrong article id", articleImagesToInsert1.articleId, articlesWithImages1.articleId)
        assertEquals("article with images contains wrong uri", articleImagesToInsert1.uri, articlesWithImages1.images[0].uri)
        assertEquals("Could not acquire article with images", articleImagesToInsert2.size, articlesWithImages2.images.size)
        assertEquals("article with images contains wrong article id", articleImagesToInsert2[0].articleId, articlesWithImages2.articleId)
        assertEquals("article with images contains wrong article id", articleImagesToInsert2[1].articleId, articlesWithImages2.articleId)
        assertEquals("article with images contains wrong uri", articleImagesToInsert2[0].uri, articlesWithImages2.images[0].uri)
        assertEquals("article with images contains wrong uri", articleImagesToInsert2[1].uri, articlesWithImages2.images[1].uri)
        assertEquals("Could not acquire article with images", articleImagesToInsert3.size, articlesWithImages3.images.size)
        assertEquals("article with images contains wrong article id", articleImagesToInsert3[0].articleId, articlesWithImages3.articleId)
        assertEquals("article with images contains wrong article id", articleImagesToInsert3[1].articleId, articlesWithImages3.articleId)
        assertEquals("article with images contains wrong article id", articleImagesToInsert3[2].articleId, articlesWithImages3.articleId)
        assertEquals("article with images contains wrong uri", articleImagesToInsert3[0].uri, articlesWithImages3.images[0].uri)
        assertEquals("article with images contains wrong uri", articleImagesToInsert3[1].uri, articlesWithImages3.images[1].uri)
        assertEquals("article with images contains wrong uri", articleImagesToInsert3[2].uri, articlesWithImages3.images[2].uri)
    }
}
