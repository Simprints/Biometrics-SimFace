package com.simprints.biometrics.simface.matcher

import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.assertThat
import com.simprints.biometrics.simface.Utils.floatArrayToByteArray
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IdentificationTest {
    private lateinit var matchProcessor: MatchProcessor

    @Before
    fun setup() {
        matchProcessor = CosineDistanceMatchProcessor()
    }

    @Test
    fun score_map_should_be_ordered_by_distance() {
        val referenceArray = floatArrayToByteArray(floatArrayOf(1.0f, 0.0f))
        val arrayList = listOf(
            floatArrayToByteArray(floatArrayOf(-1.0f, 0.0f)), // opposite to referenceArray
            floatArrayToByteArray(floatArrayOf(1.0f, 0.0f)), // identical to referenceArray
            floatArrayToByteArray(floatArrayOf(0.0f, 1.0f)), // orthogonal to referenceArray
            floatArrayToByteArray(floatArrayOf(0.707f, 0.707f)), // 45 degrees to referenceArray
        )

        val sortedScores = matchProcessor.identificationScore(referenceArray, arrayList)
        val sortedDistances = sortedScores.map { it.second }

        // Closest match (identical vector) should have a score of 1
        assertThat(sortedDistances[0]).isWithin(0.0001).of(1.0)

        // 45-degree vector (second closest) should have a score of around 0.85355
        assertThat(sortedDistances[1]).isWithin(0.0001).of(0.85355)

        // Orthogonal vector (further away) should have a score of 0.5
        assertThat(sortedDistances[2]).isWithin(0.0001).of(0.5)

        // Opposite vector (furthest away) should have a score of 0.0
        assertThat(sortedDistances[3]).isWithin(0.0001).of(0.0)
    }
}
