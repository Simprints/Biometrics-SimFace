package com.simprints.biometrics.simface.embedding

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.*
import com.simprints.biometrics.loadBitmapFromTestResources
import com.simprints.biometrics.simface.SimFaceConfig
import com.simprints.biometrics.simface.Utils
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * This test class makes it trivially easy to run tests with new model files:
 *   1. Add the new model file to the anrdroidTest/res/raw folder
 *   2. Create a new test method
 *   3. Provide the model file name to `openTestModelFile()`
 *   4. Do the testing
 */
class CustomModelTest {
    private lateinit var context: Context
    private lateinit var modelManager: MLModelManager
    private lateinit var embeddingProcessor: EmbeddingProcessor

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun cleanup() {
        modelManager.close()
    }

    @Test
    fun test_processes_face_with_custom_model() = runTest {
        val testModelFile = openTestModelFile()

        modelManager = MLModelManager(
            SimFaceConfig(
                context,
                customModel = SimFaceConfig.CustomModel(
                    file = testModelFile,
                    templateVersion = "TEST_1",
                ),
            ),
        )
        embeddingProcessor = TensorFlowEmbeddingProcessor(modelManager)

        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val resultFloat = getFaceEmbeddingFromBitmap(bitmap)

        assertArrayEquals(GOOD_FACE_EMBEDDING, resultFloat, 0.1F)
    }

    private fun openTestModelFile(resourceName: String = "edgeface_test"): File {
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        Assert.assertTrue(
            "Test resource '$resourceName' not found in package '${context.packageName}'",
            resourceId != 0,
        )

        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        val tempFile = File.createTempFile("test_model", ".tflite", context.cacheDir)
        FileOutputStream(tempFile).use { outputStream -> inputStream.use { input -> input.copyTo(outputStream) } }
        return tempFile
    }

    private fun getFaceEmbeddingFromBitmap(bitmap: Bitmap): FloatArray = embeddingProcessor
        .getEmbedding(bitmap)
        .let { Utils.byteArrayToFloatArray(it) }
}
