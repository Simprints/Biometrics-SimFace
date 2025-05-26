package com.simprints.simface

import android.content.Context
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.*
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.Utils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IdentificationTest {
    private lateinit var simFace: SimFace

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        simFace = SimFace()
        simFace.initialize(SimFaceConfig(context))
    }

    @Test
    fun score_map_should_be_ordered_by_distance() {
        val referenceArray = Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f))
        val arrayList = listOf(
            Utils.floatArrayToByteArray(floatArrayOf(-1.0f, 0.0f)), // opposite to referenceArray
            Utils.floatArrayToByteArray(floatArrayOf(1.0f, 0.0f)), // identical to referenceArray
            Utils.floatArrayToByteArray(floatArrayOf(0.0f, 1.0f)), // orthogonal to referenceArray
            Utils.floatArrayToByteArray(floatArrayOf(0.707f, 0.707f)), // 45 degrees to referenceArray
        )

        val sortedMap = simFace.getMatchProcessor().identificationScore(referenceArray, arrayList)
        val sortedDistances = sortedMap.values.toList()

        // Closest match (identical vector) should have a score of 1
        Assert.assertEquals(1.0, sortedDistances[0], 0.0001)

        // 45-degree vector (second closest) should have a score of around 0.85355
        Assert.assertEquals(0.85355, sortedDistances[1], 0.0001)

        // Orthogonal vector (further away) should have a score of 0.5
        Assert.assertEquals(0.5, sortedDistances[2], 0.0001)

        // Opposite vector (furthest away) should have a score of 0.0
        Assert.assertEquals(0.0, sortedDistances[3], 0.0001)
    }
}
