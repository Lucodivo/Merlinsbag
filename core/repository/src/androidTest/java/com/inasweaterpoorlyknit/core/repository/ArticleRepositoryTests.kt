package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inasweaterpoorlyknit.core.common.createFakeUriStrings
import com.inasweaterpoorlyknit.core.database.InKnitDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class ArticleRepositoryTests {
    private lateinit var articleRepository: ArticleRepository
    private lateinit var database: InKnitDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, InKnitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        articleRepository = ArticleRepository(
            context = context,
            articleDao = database.ArticleDao(),
            ensembleDao = database.EnsembleDao(),
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
}
