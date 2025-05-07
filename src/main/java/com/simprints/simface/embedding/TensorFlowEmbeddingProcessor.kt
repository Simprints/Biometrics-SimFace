package com.simprints.simface.embedding


import android.graphics.Bitmap
import com.simprints.simface.core.MLModelManager
import com.simprints.simface.core.Utils
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat

internal class TensorFlowEmbeddingProcessor() : EmbeddingProcessor {

    companion object {
        private const val imageSize = 112
    }

    override fun getEmbedding(bitmap: Bitmap): ByteArray {
        val imageTensorProcessor = TensorProcessor.Builder()
            .add(NormalizeOp(0.5f, 0.5f))
            .add(ReshapeOp())
            .build()

        val model = MLModelManager.getFaceEmbeddingModel()

        val resizedBitmap = if (bitmap.height != imageSize || bitmap.width != imageSize) {
            try {
                Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to resize the bitmap: ${e.message}", e)
            }
        } else {
            bitmap
        }

        val inputBuffer = resizedBitmap.toIntArray(imageSize)
        val floatBuffer = inputBuffer.map { it / 255f }.toFloatArray()

        val tmpFeatures =
            TensorBuffer.createFixedSize(intArrayOf(imageSize, imageSize, 3), DataType.FLOAT32)
        tmpFeatures.loadArray(floatBuffer)

        val tensorBuffer = imageTensorProcessor.process(tmpFeatures).buffer

        val inputFeatures =
            TensorBuffer.createFixedSize(intArrayOf(imageSize, imageSize, 3), DataType.FLOAT32)
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
            resultArray[index++] = (pixel shr 8) and 255  // Green
            resultArray[index++] = pixel and 255          // Blue
        }
        return resultArray
    }
}

internal class ReshapeOp : TensorOperator {
    override fun apply(tensorBuffer: TensorBuffer): TensorBuffer {
        val (height, width, dims) = tensorBuffer.shape

        val reshapedArray = FloatArray(height * width * dims)
        val inputArray = tensorBuffer.floatArray
        for (h in 0 until height) {
            for (w in 0 until width) {
                for (d in 0 until dims) {
                    reshapedArray[d * height * width + h * width + w] =
                        inputArray[h * width * dims + w * dims + d]
                }
            }
        }

        val reshapedTensor =
            TensorBufferFloat.createFixedSize(intArrayOf(dims, height, width), DataType.FLOAT32)
        reshapedTensor.loadArray(reshapedArray)
        return reshapedTensor
    }
}
