package com.simprints.simface.core

import android.graphics.Rect
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object Utils {
    const val IMAGE_SIZE = 112

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
}
