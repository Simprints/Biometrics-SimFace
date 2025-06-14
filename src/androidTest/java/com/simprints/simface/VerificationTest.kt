package com.simprints.simface

import android.content.Context
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.*
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.Utils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerificationTest {
    private lateinit var simFace: SimFace

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(context))
    }

    @After
    fun cleanup() {
        simFace.release()
    }

    @Test
    fun score_between_identical_vectors_should_be_one() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f, 0.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f, 0.0f))

        val distance = simFace.verificationScore(array1, array2)

        assertEquals(1.0, distance, 0.0001)
    }

    @Test
    fun score_between_orthogonal_vectors_should_be_one_half() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(0.0f, 1.0f))

        val distance = simFace.verificationScore(array1, array2)

        assertEquals(0.5, distance, 0.0001)
    }

    @Test
    fun score_between_opposite_vectors_should_be_zero() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(-1.0f, 0.0f))

        val distance = simFace.verificationScore(array1, array2)

        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    fun score_between_arbitrary_vectors_should_be_between_zero_and_one() {
        val array1 = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 2.0f, 3.0f))
        val array2 = Utils.floatArrayToByteArray(floatArrayOf(4.0f, 5.0f, 6.0f))

        val distance = simFace.verificationScore(array1, array2)

        assertTrue(distance > 0.0 && distance < 1.0)
    }
}
