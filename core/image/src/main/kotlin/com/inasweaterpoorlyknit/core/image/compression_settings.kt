package com.inasweaterpoorlyknit.core.image

import android.graphics.Bitmap
import android.os.Build
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality.ImageQuality_High
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality.ImageQuality_Perfect
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality.ImageQuality_Standard
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality.ImageQuality_VeryHigh

// Webp compression quality test case:
// Article image size 1499x1951
// | FORMAT         | SIZE      | LOSSLESS PERCENTAGE |
// WEBP_LOSSLESS:     1.6   MB    (100%)
// WEBP_LOSSY @ 100:  779.8 KB    (48.7%)
// WEBP_LOSSY @ 90:   341.7 KB    (21.4%)
// WEBP_LOSSY @ 80:   199.6 kB    (12.5%)
// WEBP_LOSSY @ 70:   147.4 KB    (9.21%)

val useDeprecatedWebpInternalFormat = Build.VERSION.SDK_INT < Build.VERSION_CODES.R
val imageExportFormat = Bitmap.CompressFormat.PNG

val compressionFormatThumb = ImageQuality_Standard.compressionFormat()
val compressionQualityThumb = ImageQuality_Standard.compressionQuality()

fun ImageQuality.compressionFormat(): Bitmap.CompressFormat = when {
    useDeprecatedWebpInternalFormat -> android.graphics.Bitmap.CompressFormat.WEBP
    this == ImageQuality_Perfect -> android.graphics.Bitmap.CompressFormat.WEBP_LOSSLESS
    else -> android.graphics.Bitmap.CompressFormat.WEBP_LOSSY
}

fun ImageQuality.compressionQuality(): Int = when(this){
    ImageQuality_Perfect -> 100
    ImageQuality_VeryHigh -> if(useDeprecatedWebpInternalFormat) 90 else 100
    ImageQuality_High -> if(useDeprecatedWebpInternalFormat) 80 else 90
    ImageQuality_Standard,
    ImageQuality.UNRECOGNIZED -> if(useDeprecatedWebpInternalFormat) 70 else 80
}
