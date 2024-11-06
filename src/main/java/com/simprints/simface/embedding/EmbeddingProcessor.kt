package com.simprints.simface.embedding


import android.content.Context
import android.graphics.Bitmap
import com.simprints.simface.core.MLModelManager
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat

class EmbeddingProcessor(): IEmbeddingProcessor {

    override fun getEmbedding(bitmap: Bitmap): List<Float> {
        val imageSize = 112
        val imageTensorProcessor = TensorProcessor.Builder()
            .add(NormalizeOp(0.5f, 0.5f))
            .add(ReshapeOp())
            .build()

        val model = MLModelManager.getFaceEmbeddingModel()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
        val inputBuffer = resizedBitmap.toIntArray(imageSize)
        val floatBuffer = inputBuffer.map { it / 255f }.toFloatArray()

        val tmpFeatures = TensorBuffer.createFixedSize(intArrayOf(imageSize, imageSize,3), DataType.FLOAT32)
        tmpFeatures.loadArray(floatBuffer)

        val tensorBuffer = imageTensorProcessor.process(tmpFeatures).buffer

        val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(imageSize, imageSize,3), DataType.FLOAT32)
        inputFeatures.loadBuffer(tensorBuffer)

        val outputs = model?.process(inputFeatures)
        val outputFeature0 = outputs?.outputFeature0AsTensorBuffer
        model?.close()

        return outputFeature0?.floatArray?.toList() ?: emptyList()
    }

    private fun Bitmap.toIntArray(imageSize: Int): IntArray {
        val intValues = IntArray(imageSize * imageSize)
        getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize)

        val resultArray = IntArray(imageSize * imageSize * 3)
        var j = 0
        for (i in intValues.indices) {
            resultArray[j++] = intValues[i].shr(16).and(255)
            resultArray[j++] = intValues[i].shr(8).and(255)
            resultArray[j++] = intValues[i].and(255)
        }

        return resultArray
    }


}

internal class ReshapeOp() : TensorOperator {
    override fun apply(p0: TensorBuffer): TensorBuffer {
        val (height, width, dims) = p0.shape

        val outPixels = p0.floatArray
            .toList()
            .chunked(dims)
            .rotateListOfLists()
            .flatten()
            .toFloatArray()

        val output = TensorBufferFloat.createFixedSize(intArrayOf(dims, height, width), DataType.FLOAT32)
        output.loadArray(outPixels)
        return output
    }

    private fun <T> List<List<T>>.rotateListOfLists(): List<List<T>> {
        // Transpose the list
        val transposed = MutableList(this[0].size) { MutableList<T?>(this.size) { null } }
        for (i in this.indices) {
            for (j in this[0].indices) {
                transposed[j][i] = this[i][j]
            }
        }
        return transposed.map { it.filterNotNull() }
    }
}