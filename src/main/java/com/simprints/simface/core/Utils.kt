import java.nio.ByteBuffer
import java.nio.ByteOrder

object Utils {
    /**
     * Converts a FloatArray to a ByteArray.
     *
     * @param floatArray The FloatArray to convert.
     * @return A ByteArray representing the FloatArray.
     */
    fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4).order(ByteOrder.nativeOrder())
        for (value in floatArray) {
            byteBuffer.putFloat(value)
        }
        return byteBuffer.array()
    }

    /**
     * Converts a ByteArray back to a FloatArray.
     *
     * @param byteArray The ByteArray to convert.
     * @return A FloatArray reconstructed from the ByteArray.
     */
    fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
        val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder())
        val floatArray = FloatArray(byteArray.size / 4)
        for (i in floatArray.indices) {
            floatArray[i] = byteBuffer.getFloat(i * 4)
        }
        return floatArray
    }
}