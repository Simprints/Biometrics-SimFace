package com.simprints.simface.embedding

import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.simprints.simface.core.MLModelManager
import com.simprints.simface.core.Utils
import com.simprints.simface.core.Utils.IMAGE_SIZE
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

internal class TensorFlowEmbeddingProcessor : EmbeddingProcessor {
    override fun getEmbedding(bitmap: Bitmap): ByteArray {
        val imageTensorProcessor = TensorProcessor
            .Builder()
            .add(NormalizeOp(0.5f, 0.5f))
            .add(ReshapeOp())
            .build()

        val model = MLModelManager.getFaceEmbeddingModel()

        val resizedBitmap = if (bitmap.height != IMAGE_SIZE || bitmap.width != IMAGE_SIZE) {
            try {
                bitmap.scale(IMAGE_SIZE, IMAGE_SIZE, false)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to resize the bitmap: ${e.message}", e)
            }
        } else {
            bitmap
        }

        val inputBuffer = resizedBitmap.toIntArray(IMAGE_SIZE)
        val floatBuffer = inputBuffer.map { it / 255f }.toFloatArray()

        val tmpFeatures =
            TensorBuffer.createFixedSize(intArrayOf(IMAGE_SIZE, IMAGE_SIZE, 3), DataType.FLOAT32)
        tmpFeatures.loadArray(floatBuffer)

        val tensorBuffer = imageTensorProcessor.process(tmpFeatures).buffer

        val inputFeatures =
            TensorBuffer.createFixedSize(intArrayOf(IMAGE_SIZE, IMAGE_SIZE, 3), DataType.FLOAT32)
        inputFeatures.loadBuffer(tensorBuffer)

        val outputs = model.process(inputFeatures)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val floatArray = outputFeature0.floatArray ?: return ByteArray(0)

        return Utils.floatArrayToByteArray(floatArray)
    }

    private fun Bitmap.toIntArray(imageSize: Int): IntArray {
        val intValues = IntArray(imageSize * imageSize)
        val resultArray = IntArray(imageSize * imageSize * 3)

        getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize)

        var index = 0
        for (pixel in intValues) {
            resultArray[index++] = (pixel shr 16) and 255 // Red
            resultArray[index++] = (pixel shr 8) and 255 // Green
            resultArray[index++] = pixel and 255 // Blue
        }
        return resultArray
    }
}
