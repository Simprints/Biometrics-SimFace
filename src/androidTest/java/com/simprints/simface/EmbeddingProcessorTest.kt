package com.simprints.simface

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.Utils
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmbeddingProcessorTest {
    private lateinit var simFace: SimFace
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(context))
    }

    @Test
    fun get_embedding_with_image() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val result = simFace.getEmbedding(bitmap)
        val resultFloat = Utils.byteArrayToFloatArray(result)

        assertTrue(Utils.byteArrayToFloatArray(result).size == 512)

        // Verify results
        assertArrayEquals(GOOD_FACE_EMBEDDING, resultFloat, 0.1F)
    }

    @Test
    fun compare_embeddings_between_different_images() {
        val bitmap1: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val bitmap2: Bitmap = context.loadBitmapFromTestResources("royalty_free_bad_face")

        val embedding1 = simFace.getEmbedding(bitmap1)
        val embedding2 = simFace.getEmbedding(bitmap2)

        assertTrue(!embedding1.contentEquals(embedding2)) // Embeddings should be different
    }

    @Test
    fun consistency_test_with_same_image() {
        val bitmap: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")

        val embedding1 = simFace.getEmbedding(bitmap)
        val embedding2 = simFace.getEmbedding(bitmap)

        assertArrayEquals(embedding1, embedding2) // Embeddings should be identical
    }
}
