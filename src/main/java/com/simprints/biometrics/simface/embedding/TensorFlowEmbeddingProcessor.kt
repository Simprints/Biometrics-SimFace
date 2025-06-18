package com.simprints.biometrics.simface.embedding

import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.simprints.biometrics.simface.Constants.IMAGE_SIZE
import com.simprints.biometrics.simface.Constants.OUTPUT_EMBEDDING_SIZE
import com.simprints.biometrics.simface.Utils.floatArrayToByteArray
import com.simprints.biometrics.simface.Utils.toFloatArray
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

internal class TensorFlowEmbeddingProcessor(
    modelManager: MLModelManager,
) : EmbeddingProcessor {
    private val interpreter: Interpreter = modelManager.getInterpreter()

    override fun getEmbedding(bitmap: Bitmap): ByteArray {
        val resizedBitmap = if (bitmap.height != IMAGE_SIZE || bitmap.width != IMAGE_SIZE) {
            try {
                bitmap.scale(IMAGE_SIZE, IMAGE_SIZE, false)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to resize the bitmap: ${e.message}", e)
            }
        } else {
            bitmap
        }

        val floatBuffer = resizedBitmap.toFloatArray(IMAGE_SIZE)
        val tmpFeatures = TensorBuffer
            .createFixedSize(intArrayOf(IMAGE_SIZE, IMAGE_SIZE, 3), DataType.FLOAT32)
            .also { it.loadArray(floatBuffer) }

        val imageTensorProcessor = TensorProcessor
            .Builder()
            .add(NormalizeOp(0.5f, 0.5f))
            .add(ReshapeOp())
            .build()
        val tensorBuffer = imageTensorProcessor.process(tmpFeatures).buffer
        val outputEmbeddingBuffer = Array(1) { FloatArray(OUTPUT_EMBEDDING_SIZE) }

        return try {
            interpreter.run(tensorBuffer, outputEmbeddingBuffer)
            floatArrayToByteArray(outputEmbeddingBuffer[0])
        } catch (e: Exception) {
            println("Error running TFLite model inference, ${e.message}")
            // Handle error, maybe return an empty array or throw a custom exception
            ByteArray(0)
        }
    }
}
