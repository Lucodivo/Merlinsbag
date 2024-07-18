package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inasweaterpoorlyknit.core.common.testing.createFakeUriStrings
import com.inasweaterpoorlyknit.core.data.model.LazyArticlesWithImages
import com.inasweaterpoorlyknit.core.database.NoopDatabase
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.articleFilesDirStr
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ArticleRepositoryTests {
    private lateinit var context: Context
    private lateinit var articleRepository: ArticleRepository
    private lateinit var database: NoopDatabase

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, NoopDatabase::class.java)
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

    // TODO: Fix Test
    @Test
    @Throws(Exception::class)
    fun insertArticle() {
        // arrange
        val articleDirectory = articleFilesDirStr(context)
        val fullImageUris = createFakeUriStrings(2)
        val thumbnailImageUris = createFakeUriStrings(2)

        // act
        articleRepository.insertArticle(fullImageUris[0], thumbnailImageUris[0])
        articleRepository.insertArticle(fullImageUris[1], thumbnailImageUris[1])
        val lazyArticlesWithImages: LazyArticlesWithImages
        runBlocking {
            lazyArticlesWithImages = articleRepository.getArticlesWithImages().first()
        }

        // assert
        assertEquals("articles not properly inserted and retrieved", fullImageUris.size, lazyArticlesWithImages.size)
        // NOTE: Ordered by newest insertion first (modified_by)
        assertEquals("$articleDirectory${fullImageUris[0]}", lazyArticlesWithImages.lazyFullImageUris.getUriStrings(1)[0])
        assertEquals("$articleDirectory${thumbnailImageUris[0]}", lazyArticlesWithImages.lazyThumbImageUris.getUriStrings(1)[0])
        assertEquals("$articleDirectory${fullImageUris[1]}", lazyArticlesWithImages.lazyFullImageUris.getUriStrings(0)[0])
        assertEquals("$articleDirectory${thumbnailImageUris[1]}", lazyArticlesWithImages.lazyThumbImageUris.getUriStrings(0)[0])
    }
}