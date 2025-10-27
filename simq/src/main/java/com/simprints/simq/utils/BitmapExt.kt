package com.simprints.simq.utils

import android.graphics.Bitmap
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Crops bitmap to center region with optional displacement.
 *
 * @param centerCrop Fraction of the bitmap to use (0.0-1.0)
 * @param horizontalDisplacement Horizontal displacement factor (-1.0 to 1.0)
 * @param verticalDisplacement Vertical displacement factor (-1.0 to 1.0)
 * @return Cropped bitmap
 */
internal fun Bitmap.centerCrop(
        centerCrop: Float,
        horizontalDisplacement: Float = 0f,
        verticalDisplacement: Float = 0f,
): Bitmap {
    val hAbsDisplacement = (width * horizontalDisplacement).toInt()
    val vAbsDisplacement = (height * verticalDisplacement).toInt()

    val cropWidth = (width * centerCrop).toInt()
    val cropHeight = (height * centerCrop).toInt()
    val startX = hAbsDisplacement + (width - cropWidth) / 2
    val startY = vAbsDisplacement + (height - cropHeight) / 2

    return Bitmap.createBitmap(this, startX, startY, cropWidth, cropHeight)
}

/**
 * Resizes bitmap to a target area while maintaining aspect ratio.
 *
 * @param targetArea Target area in pixels (default: 65536 = 256x256)
 * @return Resized bitmap
 */
internal fun Bitmap.resizeToArea(targetArea: Double = 65536.0): Bitmap {
    val aspectRatio = width.toFloat() / height.toFloat()
    val newHeight = sqrt(targetArea / aspectRatio)
    val newWidth = aspectRatio * newHeight

    return Bitmap.createScaledBitmap(this, newWidth.roundToInt(), newHeight.roundToInt(), true)
}
