package com.simprints.simq

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QualityWeightsTest {
    private lateinit var simQ: SimQ

    @Before
    fun setUp() {
        simQ = SimQ()
    }

    private fun createTestBitmap(
        width: Int = 256,
        height: Int = 256,
    ): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    @Test
    fun defaultWeightsSumTo1() {
        val weights = QualityWeights.DEFAULT
        val sum =
            weights.alignment +
                weights.blur +
                weights.brightness +
                weights.contrast +
                weights.eyeOpenness
        assertThat(sum).isWithin(0.001).of(1.0)
    }

    @Test
    fun customWeightsCanBeCreated() {
        val customWeights =
            QualityWeights(
                alignment = 0.25,
                blur = 0.25,
                brightness = 0.25,
                contrast = 0.15,
                eyeOpenness = 0.10,
            )

        assertThat(customWeights.alignment).isWithin(0.001).of(0.25)
        assertThat(customWeights.blur).isWithin(0.001).of(0.25)
        assertThat(customWeights.brightness).isWithin(0.001).of(0.25)
        assertThat(customWeights.contrast).isWithin(0.001).of(0.15)
        assertThat(customWeights.eyeOpenness).isWithin(0.001).of(0.10)
    }

    @Test
    fun calculateFaceQualityWithAllWeightsMaximizingOneMetric() {
        val bitmap = createTestBitmap()

        // Only alignment weight
        val alignmentOnly =
            simQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 0.0,
                yaw = 0.0,
                roll = 0.0,
                weights =
                    QualityWeights(
                        alignment = 1.0,
                        blur = 0.0,
                        brightness = 0.0,
                        contrast = 0.0,
                        eyeOpenness = 0.0,
                    ),
            )

        assertThat(alignmentOnly).isEqualTo(1f)
    }
}
