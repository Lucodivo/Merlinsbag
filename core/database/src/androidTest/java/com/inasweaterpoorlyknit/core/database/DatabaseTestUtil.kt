package com.inasweaterpoorlyknit.core.database

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun randUUIDString() = UUID.randomUUID().toString()
object Counter{
    private var count = 0
    fun next(): Int = count++
}

fun createArticleEntity(id: String = Counter.next().toString()) = ArticleEntity(id = id)
fun createArticleEntity(count: Int) = Array(count){ createArticleEntity() }

fun createFakeUriString() = "content://com.inasweaterpoorlyknit.inknit/fakeimage${Counter.next()}"
fun createFakeUriStrings(count: Int) = Array(count){ createFakeUriString() }
fun createFakeUri(): Uri = Uri.parse(createFakeUriString())
fun createFakeUris(count: Int) = Array(count){ createFakeUri() }

fun createArticleImageEntity(ArticleId: String = randUUIDString()) = ArticleImageEntity(
    articleId = ArticleId,
    uri = createFakeUriString(),
    thumbnailUri = createFakeUriString(),
)
fun createArticleImageEntity(count: Int, ArticleId: String = randUUIDString()) = Array(count){
    createArticleImageEntity(ArticleId = ArticleId)
}

class LiveDataTestUtil<T> {
    fun getValue(liveData: LiveData<T>): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = Observer<T> { t ->
            data = t
            latch.countDown()
        }
        liveData.observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)
        liveData.removeObserver(observer)
        return data ?: throw NullPointerException("LiveData value was null")
    }
}
