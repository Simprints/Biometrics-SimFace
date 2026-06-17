package com.simprints.biometrics.spoofing.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.graphics.set
import com.simprints.biometrics.spoofing.cropWithMirroringExpand
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer
import java.nio.channels.Channels
import kotlin.math.exp

class DeepFace : ModelImplementation {
    private lateinit var interpreter1: Interpreter
    private lateinit var interpreter2: Interpreter

    override fun initialize(context: Context) = try {
        val modelBytes1 = context
            .assets
            .open("spoof_model_scale_2_7.tflite")
            .use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteBuffer.allocateDirect(size) // Use allocateDirect for native libs
                Channels.newChannel(inputStream).use { channel -> channel.read(buffer) }
                buffer.flip()
                buffer
            }
        val options1 = Interpreter.Options().apply { setNumThreads(1) }
        interpreter1 = Interpreter(modelBytes1, options1)

        val modelBytes2 = context
            .assets
            .open("spoof_model_scale_4_0.tflite")
            .use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteBuffer.allocateDirect(size) // Use allocateDirect for native libs
                Channels.newChannel(inputStream).use { channel -> channel.read(buffer) }
                buffer.flip()
                buffer
            }
        val options2 = Interpreter.Options().apply { setNumThreads(1) }
        interpreter2 = Interpreter(modelBytes2, options2)
    } catch (e: Exception) {
        Log.wtf("!!!!!!!!!", "SimAntiSpoofing.init error")
        throw RuntimeException("Failed to initialize: ${e.message}", e)
    }

    override fun calculateSpoofingScore(
        sourceBitmap: Bitmap,
        faceBoundingBox: Rect,
    ): Pair<Bitmap?, Float> {
        val (bitmap1, result1) = processImage(sourceBitmap, faceBoundingBox, interpreter1, 2.7f)
        val (_, result2) = processImage(sourceBitmap, faceBoundingBox, interpreter2, 4.0f)

        val result1max = softMax(result1)
        val result2max = softMax(result2)

        val output = result1max.zip(result2max).map { (it.first + it.second) }
        val label = output.indexOf(output.max())
        val iSpoof = label != 1
        val score = output[label] / 2f

        Log.wtf("!!!!!!!!!", "DeepFace.calculateSpoofingScore results: ${result1.contentToString()} ${result2.contentToString()}")
        Log.wtf("!!!!!!!!!", "DeepFace.calculateSpoofingScore softMax: ${result1max.contentToString()} ${result2max.contentToString()}")
        Log.wtf("!!!!!!!!!", "DeepFace.calculateSpoofingScore output: $output, label: $label")
        Log.wtf("!!!!!!!!!", "DeepFace.calculateSpoofingScore IS_SPOOF: $iSpoof - confidence: $score")

        return bitmap1 to score
    }

    private fun processImage(
        sourceBitmap: Bitmap,
        faceBoundingBox: Rect,
        interpreter: Interpreter,
        scale: Float,
    ): Pair<Bitmap?, FloatArray> {
        val croppedBitmap = cropWithMirroringExpand(sourceBitmap, faceBoundingBox, scale)
        val resizedBitmap = croppedBitmap.scale(IMAGE_SIZE, IMAGE_SIZE, false)

        // Flip color channels to BGR
        for (i in 0 until resizedBitmap.width) {
            for (j in 0 until resizedBitmap.height) {
                resizedBitmap[i, j] = Color.rgb(
                    Color.blue(resizedBitmap[i, j]),
                    Color.green(resizedBitmap[i, j]),
                    Color.red(resizedBitmap[i, j]),
                )
            }
        }

        val imageTensorProcessor = ImageProcessor
            .Builder()
            .add(CastOp(DataType.FLOAT32))
            .build()

        val tensorBuffer = imageTensorProcessor.process(TensorImage.fromBitmap(resizedBitmap)).buffer
        val outputBuffer = Array(1) { FloatArray(OUTPUT_SIZE) }
        try {
            interpreter.run(tensorBuffer, outputBuffer)
        } catch (e: Exception) {
            Log.wtf("!!!!!!!!!", "SimAntiSpoofing.calculateSpoofingScore error ${e.message}")
            e.printStackTrace()
            println("Error running TFLite model inference, ${e.message}")
            // Handle error, maybe return an empty array or throw a custom exception
            return null to floatArrayOf()
        }
        return resizedBitmap to outputBuffer[0]
    }

    private fun softMax(x: FloatArray): FloatArray {
        val exp = x.map { exp(it) }
        val expSum = exp.sum()
        return exp.map { it / expSum }.toFloatArray()
    }

    override fun close() = try {
        interpreter1.close()
        interpreter2.close()
    } catch (e: Exception) {
        println("Error releasing MLModelManager: ${e.message}")
    }

    companion object {
        private const val IMAGE_SIZE = 80
        private const val OUTPUT_SIZE = 3
    }
}
