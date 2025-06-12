package com.simprints.simface

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.*
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.Utils
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test

/**
 * This test class makes it trivially easy to run tests with new model files:
 *   1. Add the new model file to the anrdroidTest/res/raw folder
 *   2. Create a new test method
 *   3. Provide the model file name to `openTestModelFile()`
 *   4. Do the testing
 */
class CustomModelTest {
    private lateinit var simFace: SimFace
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
    }

    @After
    fun cleanup() {
        simFace.release()
    }

    @Test
    fun test_processes_face_with_custom_model() = runTest {
        val testModelFile = context.openTestModelFile()

        simFace.initialize(
            SimFaceConfig(
                context,
                customModel = SimFaceConfig.CustomModel(
                    file = testModelFile,
                    templateVersion = "TEST_1",
                ),
            ),
        )
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val resultFloat = getFaceEmbeddingFromBitmap(bitmap)

        assertArrayEquals(GOOD_FACE_EMBEDDING, resultFloat, 0.1F)
    }

    private fun getFaceEmbeddingFromBitmap(bitmap: Bitmap): FloatArray = simFace
        .getEmbedding(bitmap)
        .let { Utils.byteArrayToFloatArray(it) }
}
