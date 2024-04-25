package com.inasweaterpoorlyknit.inknit.database

import android.net.Uri
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleEntity
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleImageEntity
import java.util.UUID

fun randUUIDString() = UUID.randomUUID().toString()
object Counter{
    private var count = 0
    fun next(): Int = count++
}

fun createClothingArticleEntity(id: String = Counter.next().toString()) = ClothingArticleEntity(id = id)
fun createClothingArticleEntity(count: Int) = Array(count){ createClothingArticleEntity() }

fun createUri() = Uri.parse("content://com.inasweaterpoorlyknit.inknit/fakeimage${Counter.next()}")
fun createFakeUris(count: Int) = Array(count){ createUri() }

fun createClothingArticleImageEntity(clothingArticleId: String = randUUIDString()) = ClothingArticleImageEntity(
    clothingArticleId = clothingArticleId,
    uri = createUri(),
    thumbnailUri = createUri(),
)
fun createClothingArticleImageEntity(count: Int, clothingArticleId: String = randUUIDString()) = Array(count){
    createClothingArticleImageEntity(clothingArticleId = clothingArticleId)
}
