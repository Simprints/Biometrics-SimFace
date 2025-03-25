package com.simprints.simface.core

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
}