package com.simprints.biometrics.spoofing

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.max

internal fun cropWithMirroringExpand(
    bitmap: Bitmap,
    faceBoundingBox: Rect,
    scale: Float,
): Bitmap {
    val side = (max(faceBoundingBox.width(), faceBoundingBox.height()).coerceAtLeast(1) * scale).toInt()
    val centerX = faceBoundingBox.exactCenterX()
    val centerY = faceBoundingBox.exactCenterY()
    val left = (centerX - side / 2f).toInt()
    val top = (centerY - side / 2f).toInt()

    val output = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(side * side)

    for (y in 0 until side) {
        val srcY = mirrorIndex(top + y, bitmap.height)
        for (x in 0 until side) {
            val srcX = mirrorIndex(left + x, bitmap.width)
            pixels[y * side + x] = bitmap.getPixel(srcX, srcY)
        }
    }

    output.setPixels(pixels, 0, side, 0, 0, side, side)
    return output
}

private fun mirrorIndex(
    index: Int,
    size: Int,
): Int {
    if (size <= 1) return 0

    var reflected = index
    while (reflected !in 0..<size) {
        reflected = if (reflected < 0) {
            -reflected - 1
        } else {
            (2 * size) - reflected - 1
        }
    }
    return reflected
}
