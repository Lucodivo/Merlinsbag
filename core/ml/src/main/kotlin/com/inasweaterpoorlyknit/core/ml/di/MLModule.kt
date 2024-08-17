package com.inasweaterpoorlyknit.core.ml.di

import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.inasweaterpoorlyknit.core.ml.image.SegmentedImage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {
  @Provides
  fun providesSubjectSegmenter(): SubjectSegmenter {
    return SubjectSegmentation.getClient(
      SubjectSegmenterOptions.Builder()
          .enableMultipleSubjects(
            SubjectSegmenterOptions.SubjectResultOptions.Builder()
                .enableConfidenceMask()
                .build()
          )
          .build()
    )
  }

  @Provides
  @Singleton
  fun providesSegmentedImage(subjectSegmenter: SubjectSegmenter): SegmentedImage = SegmentedImage(subjectSegmenter)
}