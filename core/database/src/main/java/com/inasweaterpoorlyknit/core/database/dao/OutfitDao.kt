package com.inasweaterpoorlyknit.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleEntity
import com.inasweaterpoorlyknit.core.database.model.OutfitArticlesEntity
import com.inasweaterpoorlyknit.core.database.model.OutfitEntity

@Dao
interface OutfitArticlesDao {
  @Insert
  fun insertOutfit(vararg outfitEntity: OutfitEntity)
  @Update
  fun updateOutfit(outfitEntity: OutfitEntity)

  @Insert
  fun insertOutfitArticle(vararg outfitArticlesEntity: OutfitArticlesEntity)

  @Query(""" SELECT clothing_articles.*
                FROM clothing_articles
                JOIN outfit_articles ON clothing_articles.id = outfit_articles.clothing_article_id
                WHERE outfit_articles.outfit_id = :outfitId """)
  fun getAllOutfitArticles(outfitId: String): LiveData<List<ClothingArticleEntity>>
}
