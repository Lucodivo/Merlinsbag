package com.inasweaterpoorlyknit.core.database

import com.inasweaterpoorlyknit.core.common.Counter
import com.inasweaterpoorlyknit.core.common.createFakeUriString
import com.inasweaterpoorlyknit.core.common.randUUIDString
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity

fun createArticleEntity(id: String = Counter.next().toString()) = ArticleEntity(id = id)
fun createArticleEntity(count: Int) = Array(count){ createArticleEntity() }

fun createEnsembleEntity(id: String = Counter.next().toString()) = EnsembleEntity(id = id, title = "TestEnsemble$id")
fun createEnsembleEntity(count: Int) = Array(count){ createEnsembleEntity() }

fun createArticleImageEntity(ArticleId: String = randUUIDString()) = ArticleImageEntity(
    articleId = ArticleId,
    filename = createFakeUriString(),
    filenameThumb = createFakeUriString(),
)
fun createArticleImageEntity(count: Int, ArticleId: String = randUUIDString()) = Array(count){
    createArticleImageEntity(ArticleId = ArticleId)
}