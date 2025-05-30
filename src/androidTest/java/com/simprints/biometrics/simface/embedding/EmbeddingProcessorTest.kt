package com.simprints.biometrics.simface.embedding

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.*
import com.simprints.biometrics.loadBitmapFromTestResources
import com.simprints.biometrics.simface.SimFaceConfig
import com.simprints.biometrics.simface.Utils
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmbeddingProcessorTest {
    private lateinit var context: Context
    private lateinit var modelManager: MLModelManager
    private lateinit var embeddingProcessor: EmbeddingProcessor

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        modelManager = MLModelManager(SimFaceConfig(context))
        embeddingProcessor = TensorFlowEmbeddingProcessor(modelManager)
    }

    @After
    fun cleanup() {
        modelManager.close()
    }

    @Test
    fun get_embedding_with_image() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val result = embeddingProcessor.getEmbedding(bitmap)
        val resultFloat = Utils.byteArrayToFloatArray(result)

        assertTrue(Utils.byteArrayToFloatArray(result).size == 512)

        // Verify results
        assertArrayEquals(GOOD_FACE_EMBEDDING, resultFloat, 0.1F)
    }

    @Test
    fun compare_embeddings_between_different_images() {
        val bitmap1: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val bitmap2: Bitmap = context.loadBitmapFromTestResources("royalty_free_bad_face")

        val embedding1 = embeddingProcessor.getEmbedding(bitmap1)
        val embedding2 = embeddingProcessor.getEmbedding(bitmap2)

        assertTrue(!embedding1.contentEquals(embedding2)) // Embeddings should be different
    }

    @Test
    fun consistency_test_with_same_image() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")

        val embedding1 = embeddingProcessor.getEmbedding(bitmap)
        val embedding2 = embeddingProcessor.getEmbedding(bitmap)

        assertArrayEquals(embedding1, embedding2) // Embeddings should be identical
    }
}
