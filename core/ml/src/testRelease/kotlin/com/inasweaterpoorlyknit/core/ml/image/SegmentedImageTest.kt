package com.inasweaterpoorlyknit.core.ml.image

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class SegmentedImageTest {

  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var subjectSegmenter: SubjectSegmenter

  lateinit var segmentedImage: SegmentedImage

  @Before
  fun beforeEach() {
    mockkStatic(Bitmap::class)
    val bitmap = mockk<Bitmap>()
    every { Bitmap.createBitmap(any<Int>(), any<Int>(), any<Config>(), any<Boolean>()) } returns bitmap

    mockkStatic(InputImage::class)
    val bitmapMockk = mockk<InputImage>()
    every { InputImage.fromBitmap(any(), any()) } returns bitmapMockk
    every { InputImage.fromFilePath(any(), any()) } returns bitmapMockk

    val taskMockk = mockk<Task<SubjectSegmentationResult>>()
    every { subjectSegmenter.process(bitmapMockk) } returns taskMockk
    every { taskMockk.addOnSuccessListener(any()) } returns taskMockk
    every { taskMockk.addOnFailureListener(any()) } returns taskMockk

    justRun { subjectSegmenter.close() }

    segmentedImage = SegmentedImage(subjectSegmenter)
  }

  @Test
  fun cleanup() {
    segmentedImage.cleanup()

    verify(exactly = 1) { subjectSegmenter.close() }
  }
}
