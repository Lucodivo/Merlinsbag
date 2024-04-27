package com.inasweaterpoorlyknit.core.database

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleImageEntity
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun randUUIDString() = UUID.randomUUID().toString()
object Counter{
    private var count = 0
    fun next(): Int = count++
}

fun createClothingArticleEntity(id: String = Counter.next().toString()) = ClothingArticleEntity(id = id)
fun createClothingArticleEntity(count: Int) = Array(count){ createClothingArticleEntity() }

fun createFakeUriString() = "content://com.inasweaterpoorlyknit.inknit/fakeimage${Counter.next()}"
fun createFakeUriStrings(count: Int) = Array(count){ createFakeUriString() }
fun createFakeUri(): Uri = Uri.parse(createFakeUriString())
fun createFakeUris(count: Int) = Array(count){ createFakeUri() }

fun createClothingArticleImageEntity(clothingArticleId: String = randUUIDString()) = ClothingArticleImageEntity(
    clothingArticleId = clothingArticleId,
    uri = createFakeUriString(),
    thumbnailUri = createFakeUriString(),
)
fun createClothingArticleImageEntity(count: Int, clothingArticleId: String = randUUIDString()) = Array(count){
    createClothingArticleImageEntity(clothingArticleId = clothingArticleId)
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
