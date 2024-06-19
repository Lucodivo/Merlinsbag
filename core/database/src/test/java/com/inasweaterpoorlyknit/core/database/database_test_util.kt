package com.inasweaterpoorlyknit.core.database

import com.inasweaterpoorlyknit.core.common.testing.Counter
import com.inasweaterpoorlyknit.core.common.testing.createFakeUriString
import com.inasweaterpoorlyknit.core.common.testing.randUUIDString
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.ImageFilenames

fun createCounterString() = Counter.next().toString()
fun createCounterString(count: Int) = Array(count){ Counter.next().toString() }

fun createArticleEntity(id: String = createCounterString()) = ArticleEntity(id = id, created = 0, modified = 0)
fun createArticleEntity(count: Int) = Array(count){ createArticleEntity() }

fun createEnsembleEntity(id: String = createCounterString()) = EnsembleEntity(id = id, title = id, created = 0, modified = 0)
fun createEnsembleEntity(count: Int) = Array(count){ createEnsembleEntity() }

fun createArticleImageEntity(ArticleId: String = randUUIDString()) = ArticleImageEntity(
    articleId = ArticleId,
    id = createCounterString(),
    filename = createFakeUriString(),
    filenameThumb = createFakeUriString(),
)
fun createArticleImageEntity(count: Int, ArticleId: String = randUUIDString()) = Array(count){
    createArticleImageEntity(ArticleId = ArticleId)
}
fun createArticleImageEntity(articleId: String, filenames: Array<ImageFilenames>) =
    filenames.map { ArticleImageEntity(id = createCounterString(), articleId = articleId, filename = it.filename, filenameThumb = it.filenameThumb) }.toTypedArray()

fun createImageFilenames(filename: String = createCounterString()) =
    ImageFilenames("${filename}_full.jpg", "${filename}_thumb.jpg")
fun createImageFilenames(count: Int) = Array(count){ createImageFilenames() }