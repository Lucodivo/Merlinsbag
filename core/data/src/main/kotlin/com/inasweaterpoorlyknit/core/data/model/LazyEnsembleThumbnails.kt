package com.inasweaterpoorlyknit.core.data.model

import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleThumbnails

class LazyEnsembleThumbnails(
    val ensemble: Ensemble,
    val thumbnails: LazyArticleThumbnails,
) {
    companion object {
        fun fromEnsembleArticleThumbnails(
            ensembleArticleThumbnails: EnsembleArticleThumbnails,
            articleImagesDirStr: String,
        ) = LazyEnsembleThumbnails(
            Ensemble(
                ensembleArticleThumbnails.ensembleId,
                ensembleArticleThumbnails.ensembleTitle,
            ),
            LazyArticleThumbnails(
                articleImagesDirStr,
                ensembleArticleThumbnails.articles
            )
        )
    }
}