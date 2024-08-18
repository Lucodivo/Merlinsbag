package com.inasweaterpoorlyknit.core.ml.di

import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.inasweaterpoorlyknit.core.ml.image.SegmentedImage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
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
  fun providesSegmentedImage(subjectSegmenter: SubjectSegmenter): SegmentedImage = SegmentedImage(subjectSegmenter)
}