package com.simprints.biometrics.simface.embedding

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.*
import com.google.common.truth.Truth.assertThat
import com.simprints.biometrics.loadBitmapFromTestResources
import com.simprints.biometrics.openTestModelFile
import com.simprints.biometrics.simface.SimFaceConfig
import com.simprints.biometrics.simface.Utils
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * This test class makes it trivially easy to run tests with new model files:
 * 1. Add the new model file to the anrdroidTest/res/raw folder
 * 2. Create a new test method
 * 3. Provide the model file name to `openTestModelFile()`
 * 4. Do the testing
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
        if (::modelManager.isInitialized) {
            modelManager.close()
        }
    }

    @Test
    fun test_processes_face_with_custom_model() = runTest {
        val testModelFile = context.openTestModelFile()

        modelManager =
            MLModelManager(
                SimFaceConfig(
                    context,
                    customModel =
                        SimFaceConfig.CustomModel(
                            file = testModelFile,
                            templateVersion = "TEST_1",
                        ),
                ),
            )
        embeddingProcessor = TensorFlowEmbeddingProcessor(modelManager)

        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val resultFloat = getFaceEmbeddingFromBitmap(bitmap)

        assertThat(resultFloat).usingTolerance(0.1).containsExactly(GOOD_FACE_EMBEDDING).inOrder()
    }

    private fun getFaceEmbeddingFromBitmap(bitmap: Bitmap): FloatArray =
        embeddingProcessor.getEmbedding(bitmap).let { Utils.byteArrayToFloatArray(it) }
}
