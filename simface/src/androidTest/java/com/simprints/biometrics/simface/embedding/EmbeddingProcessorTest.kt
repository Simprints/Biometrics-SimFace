package com.simprints.biometrics.simface.embedding

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.assertThat
import com.simprints.biometrics.loadBitmapFromTestResources
import com.simprints.biometrics.simface.SimFaceConfig
import com.simprints.biometrics.simface.Utils
import org.junit.After
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

        assertThat(resultFloat.size).isEqualTo(512)

        assertThat(resultFloat)
            .usingTolerance(0.1)
            .containsExactly(GOOD_FACE_EMBEDDING)
    }

    @Test
    fun compare_embeddings_between_different_images() {
        val bitmap1: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val bitmap2: Bitmap = context.loadBitmapFromTestResources("royalty_free_bad_face")

        val embedding1 = embeddingProcessor.getEmbedding(bitmap1)
        val embedding2 = embeddingProcessor.getEmbedding(bitmap2)

        assertThat(embedding1).isNotEqualTo(embedding2)
    }

    @Test
    fun consistency_test_with_same_image() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")

        val embedding1 = embeddingProcessor.getEmbedding(bitmap)
        val embedding2 = embeddingProcessor.getEmbedding(bitmap)

        assertThat(embedding1).isEqualTo(embedding2)
    }
}
