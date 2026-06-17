package com.simprints.biometrics.spoofing.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.scale
import com.simprints.biometrics.spoofing.cropWithMirroringExpand
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.channels.Channels
import kotlin.math.abs

class OnnxAntiSpoof : ModelImplementation {
    private lateinit var interpreter: Interpreter

    override fun initialize(context: Context) = try {
        val modelBytes = context
            .assets
            .open("best_model_quantized.tflite")
            .use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteBuffer.allocateDirect(size) // Use allocateDirect for native libs
                Channels.newChannel(inputStream).use { channel -> channel.read(buffer) }
                buffer.flip()
                buffer
            }
        val options = Interpreter.Options().apply { setNumThreads(1) }
        interpreter = Interpreter(modelBytes, options)
    } catch (e: Exception) {
        Log.wtf("!!!!!!!!!", "SimAntiSpoofing.init error")
        throw RuntimeException("Failed to initialize: ${e.message}", e)
    }

    override fun calculateSpoofingScore(
        sourceBitmap: Bitmap,
        faceBoundingBox: Rect,
    ): Pair<Bitmap?, Float> {
        val croppedBitmap = cropWithMirroringExpand(sourceBitmap, faceBoundingBox, 1.5f)

        val resizedBitmap = if (croppedBitmap.height != IMAGE_SIZE || croppedBitmap.width != IMAGE_SIZE) {
            try {
                croppedBitmap.scale(IMAGE_SIZE, IMAGE_SIZE, false)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to resize the bitmap: ${e.message}", e)
            }
        } else {
            croppedBitmap
        }

        val floatBuffer = resizedBitmap.toFloatArray(IMAGE_SIZE)
        val tmpFeatures = TensorBuffer
            .createFixedSize(
                intArrayOf(IMAGE_SIZE, IMAGE_SIZE, 3),
                DataType.FLOAT32,
            ).also { it.loadArray(floatBuffer) }

        val imageTensorProcessor = TensorProcessor
            .Builder()
            .build()

        val tensorBuffer = imageTensorProcessor.process(tmpFeatures).buffer
        val outputBuffer = Array(1) { FloatArray(OUTPUT_SIZE) }

        try {
            interpreter.run(tensorBuffer, outputBuffer)
            // floatArrayToByteArray(outputBuffer[0])
        } catch (e: Exception) {
            Log.wtf("!!!!!!!!!", "SimAntiSpoofing.calculateSpoofingScore error ${e.message}")
            e.printStackTrace()
            println("Error running TFLite model inference, ${e.message}")
            // Handle error, maybe return an empty array or throw a custom exception
            return null to 0f
        }

        Log.wtf("!!!!!!!!!", "SimAntiSpoofing.calculateSpoofingScore RESULT: ${outputBuffer[0].contentToString()}}")

        val resultBuffer = outputBuffer[0]
        val realScore = resultBuffer[0]
        val spoofScore = resultBuffer[1]
        val diff = realScore - spoofScore
        val isReal = diff >= 0f
        val confidence = abs(diff)

        Log.wtf("!!!!!!!!!", "SimAntiSpoofing.calculateSpoofingScore IS_REAL: $isReal - confidence: $confidence")

        return croppedBitmap to diff
    }

    override fun close() = try {
        interpreter.close()
    } catch (e: Exception) {
        println("Error releasing MLModelManager: ${e.message}")
    }

    /**
     * Convert image into a 1D array of pixel color
     * values in RGB order in [0,1] range.
     */
    private fun Bitmap.toFloatArray(imageSize: Int): FloatArray {
        val intValues = IntArray(imageSize * imageSize)
        val resultArray = FloatArray(imageSize * imageSize * 3)

        getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize)

        // It requires BRG order
        var index = 0
        for (pixel in intValues) {
            resultArray[index++] = (pixel and 255) / 255f // Blue
            resultArray[index++] = ((pixel shr 16) and 255) / 255f // Red
            resultArray[index++] = ((pixel shr 8) and 255) / 255f // Green
        }
        return resultArray
    }

    companion object {
        private const val IMAGE_SIZE = 128
        private const val OUTPUT_SIZE = 2
    }
}
