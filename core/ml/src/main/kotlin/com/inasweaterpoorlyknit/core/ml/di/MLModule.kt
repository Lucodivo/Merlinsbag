package com.inasweaterpoorlyknit.core.ml.di

import com.inasweaterpoorlyknit.core.ml.image.SegmentedImage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

// TODO: Test if a smart usage of a singleton SegmentedImage can aid in memory concerns
@Module
@InstallIn(ViewModelComponent::class)
object MLModule {
  @Provides
  fun providesSegmentedImage(): SegmentedImage = SegmentedImage()
}