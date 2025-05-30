package com.simprints.biometrics.simface

import android.graphics.Bitmap
import android.graphics.Rect
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object Utils {
    /**
     * Converts a FloatArray to a ByteArray.
     *
     * @param floatArray The float array to convert.
     * @return A byte stream representing the contents of float array.
     */
    internal fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
        val byteBuffer =
            ByteBuffer.allocate(floatArray.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
        byteBuffer.asFloatBuffer().put(floatArray)
        return byteBuffer.array()
    }

    /**
     * Converts a ByteArray back to a FloatArray.
     *
     * @param byteArray The byte stream representation of float array
     * @return A float array reconstructed from the byte stream.
     */
    internal fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
        val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer()
        return FloatArray(floatBuffer.remaining()).apply { floatBuffer.get(this) }
    }

    /**
     * Clamps the rectangle to the bounds of the image.
     *
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A new rectangle that is clamped to the bounds of the image.
     */
    internal fun Rect.clampToBounds(
        width: Int,
        height: Int,
    ): Rect = Rect(
        left.coerceAtLeast(0),
        top.coerceAtLeast(0),
        right.coerceAtMost(width),
        bottom.coerceAtMost(height),
    )

    /**
     * Convert image into a 1D array of pixel color
     * values in RGB order in [0,1] range.
     */
    internal fun Bitmap.toFloatArray(imageSize: Int): FloatArray {
        val intValues = IntArray(imageSize * imageSize)
        val resultArray = FloatArray(imageSize * imageSize * 3)

        getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize)

        var index = 0
        for (pixel in intValues) {
            resultArray[index++] = ((pixel shr 16) and 255) / 255f // Red
            resultArray[index++] = ((pixel shr 8) and 255) / 255f // Green
            resultArray[index++] = (pixel and 255) / 255f // Blue
        }
        return resultArray
    }
}
