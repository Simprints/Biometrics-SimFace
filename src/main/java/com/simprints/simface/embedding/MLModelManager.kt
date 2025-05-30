package com.simprints.simface.embedding

import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.Utils
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel

internal class MLModelManager(
    config: SimFaceConfig,
) {
    private val interpreter: Interpreter

    val templateVersion: String = config.customModel?.templateVersion ?: Utils.DEFAULT_TEMPLATE_VERSION

    init {

        val modelFileByteBuffer = if (config.customModel == null) {
            loadDefaultModelFile(config)
        } else {
            config.customModel.file.inputStream().channel.use { fileChannel ->
                fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, fileChannel.size())
            }
        }
        val options = Interpreter.Options().apply {
            setNumThreads(1)
        }

        interpreter = Interpreter(modelFileByteBuffer, options)
    }

    fun getInterpreter(): Interpreter = interpreter

    fun close() {
        interpreter.close()
    }

    private fun loadDefaultModelFile(config: SimFaceConfig): ByteBuffer = config.applicationContext
        .assets
        .open(DEFAULT_MODEL_FILENAME)
        .use { inputStream ->
            val size = inputStream.available()
            val buffer = ByteBuffer.allocateDirect(size) // Use allocateDirect for native libs
            Channels.newChannel(inputStream).use { channel -> channel.read(buffer) }
            buffer.flip()
            buffer
        }

    companion object {
        private const val DEFAULT_MODEL_FILENAME = "edgeface_s_gamma_05.tflite"
    }
}
