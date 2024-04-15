package com.inasweaterpoorlyknit.inknit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import androidx.annotation.ColorInt
import com.google.mlkit.vision.segmentation.subject.Subject
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.inasweaterpoorlyknit.inknit.GraphicOverlay.Graphic

private val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f

class SubjectSegmentationGraphic(
    segmentationResult: SubjectSegmentationResult,
    private val imageWidth: Int,
    private val imageHeight: Int,
    private var confidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
) : Graphic() {
    private val subjects: List<Subject> = segmentationResult.subjects

    fun changeConfidenceThreshold(confidenceThreshold: Float) {
        this.confidenceThreshold = confidenceThreshold
    }

    /** Draws the segmented background on the supplied canvas.  */
    override fun draw(
        canvas: Canvas,
        overlayWidth: Int,
        overlayHeight: Int,
        transformationMatrix: Matrix) {
        val bitmap = Bitmap.createBitmap(
                        maskColorsFromFloatBuffer(),
                        imageWidth,
                        imageHeight,
                        Bitmap.Config.ARGB_8888
            )
        val isRawSizeMaskEnabled = imageWidth != overlayWidth || imageHeight != overlayHeight
        val scaleX = overlayWidth * 1f / imageWidth
        val scaleY = overlayHeight * 1f / imageHeight
        if (isRawSizeMaskEnabled) {
            val matrix = Matrix(transformationMatrix)
            matrix.preScale(scaleX, scaleY)
            canvas.drawBitmap(bitmap, matrix, null)
        } else {
            canvas.drawBitmap(bitmap, transformationMatrix, null)
        }

        bitmap.recycle()
    }

    /** Converts FloatBuffer floats from all subjects to ColorInt array that can be used as a mask.  */
    @ColorInt
    private fun maskColorsFromFloatBuffer(): IntArray {
        @ColorInt
        val colors = IntArray(imageWidth * imageHeight)
        subjects.forEachIndexed{ index, subject ->
            val color = COLORS[index % COLORS.size]
            subject.confidenceMask?.let { mask ->
                for (j in 0 until subject.height) {
                    for (i in 0 until subject.width) {
                        if (mask.get() > confidenceThreshold) {
                            colors[(subject.startY + j) * imageWidth + subject.startX + i] = color
                        }
                    }
                }
                mask.rewind()
            }
        }
        return colors
    }

    companion object {
        private val ALPHA = 128
        private val COLORS = arrayOf(
            Color.argb(ALPHA, 255, 0, 255), Color.argb(ALPHA, 0, 255, 255), Color.argb(ALPHA, 255, 255, 0),
            Color.argb(ALPHA, 255, 0, 0), Color.argb(ALPHA, 0, 255, 0), Color.argb(ALPHA, 0, 0, 255),
            Color.argb(ALPHA, 128, 0, 128), Color.argb(ALPHA, 0, 128, 128), Color.argb(ALPHA, 128, 128, 0),
            Color.argb(ALPHA, 128, 0, 0), Color.argb(ALPHA, 0, 128, 0), Color.argb(ALPHA, 0, 0, 128)
        )
    }
}
