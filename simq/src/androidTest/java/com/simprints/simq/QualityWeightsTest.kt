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
