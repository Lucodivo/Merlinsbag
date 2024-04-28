package com.inasweaterpoorlyknit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inasweaterpoorlyknit.core.database.dao.ClothingArticleWithImagesDao
import com.inasweaterpoorlyknit.core.database.dao.OutfitArticlesDao
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.OutfitArticlesEntity
import com.inasweaterpoorlyknit.core.database.model.OutfitEntity

@Database(entities = [
  ClothingArticleEntity::class,
  ClothingArticleImageEntity::class,
  OutfitEntity::class,
  OutfitArticlesEntity::class,],
  version = 1)
abstract class InKnitDatabase : RoomDatabase() {
  abstract fun clothingArticleWithImagesDao(): ClothingArticleWithImagesDao
  abstract fun outfitArticlesDao(): OutfitArticlesDao
}
