package com.inasweaterpoorlyknit.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleDaoTests: DatabaseTests() {
  @Test
  fun getAllArticlesWithThumbnails() = runBlocking(Dispatchers.IO) {
    val articles = createArticleEntity(3)
    val articleImages = Array(articles.size){ i ->
      createArticleImageEntity(
        articles[i].id,
        createImageFilenames(i + 1).sortedBy { it.filenameThumb }.toTypedArray()
      )
    }
    articleDao.insertArticles(*articles)
    articleDao.insertArticleImages(*articleImages.flatten().toTypedArray())

    val allArticlesWithThumbnails =
        articleDao.getAllArticlesWithThumbnails().first()
            .onEach { it.thumbnailPaths.sortedBy { paths -> paths.filenameThumb } }
            .sortedBy { it.articleId.toInt() }

    assertEquals(articles.size, allArticlesWithThumbnails.size)
    for (i in articles.indices) {
      assertEquals(articleImages[i].size, allArticlesWithThumbnails[i].thumbnailPaths.size)
      for(j in articleImages[i].indices) {
        assertEquals(articleImages[i][j].filenameThumb, allArticlesWithThumbnails[i].thumbnailPaths[j].filenameThumb)
      }
    }
  }

  @Test
  fun getAllArticlesWithFullImages() = runBlocking(Dispatchers.IO) {
    val articles = createArticleEntity(3)
    val articleImages = Array(articles.size){ i ->
      createArticleImageEntity(
        articles[i].id,
        createImageFilenames(i + 1).sortedBy { it.filename }.toTypedArray()
      )
    }
    articleDao.insertArticles(*articles)
    articleDao.insertArticleImages(*articleImages.flatten().toTypedArray())

    val allArticlesWithFullImages =
        articleDao.getAllArticlesWithFullImages().first()
            .onEach { it.fullImagePaths.sortedBy { paths -> paths.filename } }
            .sortedBy { it.articleId.toInt() }

    assertEquals(articles.size, allArticlesWithFullImages.size)
    for (i in articles.indices) {
      assertEquals(articleImages[i].size, allArticlesWithFullImages[i].fullImagePaths.size)
      for(j in articleImages[i].indices) {
        assertEquals(articleImages[i][j].filename, allArticlesWithFullImages[i].fullImagePaths[j].filename)
      }
    }
  }

  @Test
  fun getAllArticlesWithImages() = runBlocking(Dispatchers.IO) {
    val articles = createArticleEntity(3)
    val articleImages = Array(articles.size){ i ->
      createArticleImageEntity(
        articles[i].id,
        createImageFilenames(i + 1).sortedBy { it.filename }.toTypedArray()
      )
    }
    articleDao.insertArticles(*articles)
    articleDao.insertArticleImages(*articleImages.flatten().toTypedArray())

    val articlesWithImages =
        articleDao.getArticlesWithImages(articles.map { it.id }).first()
            .onEach { it.imagePaths.sortedBy { paths -> paths.filename } }
            .sortedBy { it.articleId.toInt() }

    assertEquals(articles.size, articlesWithImages.size)
    for (i in articles.indices) {
      assertEquals(articleImages[i].size, articlesWithImages[i].imagePaths.size)
      for(j in articleImages[i].indices) {
        assertEquals(articleImages[i][j].filename, articlesWithImages[i].imagePaths[j].filename)
        assertEquals(articleImages[i][j].filenameThumb, articlesWithImages[i].imagePaths[j].filenameThumb)
      }
    }
  }
}