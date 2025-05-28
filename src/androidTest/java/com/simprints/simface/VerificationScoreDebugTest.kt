package com.simprints.simface

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.*
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerificationScoreDebugTest {
    private lateinit var simFace: SimFace
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(context))
    }

    @Test
    fun compare_image_with_itself() = runTest {
        val bitmap1: Bitmap = context.loadBitmapFromTestResources("royalty_free_male_face")
        val template1 = extractTemplate(bitmap1)

        val bitmap2: Bitmap = context.loadBitmapFromTestResources("royalty_free_male_face")
        val template2 = extractTemplate(bitmap2)

        val score = simFace.verificationScore(template1, template2)

        println("Score: $score")
        assertTrue(score > 0.99)
    }

    @Test
    fun compare_old_male_to_young_male() = runTest {
        val bitmap1: Bitmap = context.loadBitmapFromTestResources("royalty_free_male_face")
        val template1 = extractTemplate(bitmap1)

        val bitmap2: Bitmap = context.loadBitmapFromTestResources("royalty_free_good_face")
        val template2 = extractTemplate(bitmap2)

        val score = simFace.verificationScore(template1, template2)

        println("Score: $score")
        assertTrue(score < 0.3)
    }

    @Test
    fun compare_old_male_with_young_female() = runTest {
        val bitmap1: Bitmap = context.loadBitmapFromTestResources("royalty_free_male_face")
        val template1 = extractTemplate(bitmap1)

        val bitmap2: Bitmap = context.loadBitmapFromTestResources("royalty_free_female_face")
        val template2 = extractTemplate(bitmap2)

        val score = simFace.verificationScore(template1, template2)

        println("Score: $score")
        assertTrue(score < 0.3)
    }

    private suspend fun extractTemplate(bitmap1: Bitmap): ByteArray {
        val face = simFace.detectFaceBlocking(bitmap1)
        val alignedImage = face[0].alignedFaceImage(bitmap1)
        return simFace.getEmbedding(alignedImage)
    }
}
