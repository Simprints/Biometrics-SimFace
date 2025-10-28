package com.simprints.simq

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimQTest {
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
    fun calculateFaceQualityReturnsValueBetween0And1() {
        val bitmap = createTestBitmap()
        val quality = simQ.calculateFaceQuality(bitmap)

        assertThat(quality).isAtLeast(0.0f)
        assertThat(quality).isAtMost(1.0f)
    }

    @Test
    fun calculateFaceQualityWithPerfectAlignmentReturnsHigherScore() {
        val bitmap = createTestBitmap()

        val perfectScore =
            simQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 0.0,
                yaw = 0.0,
                roll = 0.0,
            )

        val poorAlignmentScore =
            simQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 20.0,
                yaw = 20.0,
                roll = 20.0,
            )

        assertThat(perfectScore).isAtLeast(poorAlignmentScore)
    }

    @Test
    fun calculateFaceQualityWithExtremeAnglesReturnsLowScore() {
        val bitmap = createTestBitmap()

        val quality =
            simQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 30.0,
                yaw = 0.0,
                roll = 0.0,
            )

        assertThat(quality).isLessThan(0.5f)
    }

    @Test
    fun calculateFaceQualityWithEyeOpennessIncludesItInCalculation() {
        val bitmap = createTestBitmap()
        val simQWithEyeWeight =
            SimQ(
                faceWeights =
                    QualityWeights(
                        alignment = 0.3,
                        blur = 0.2,
                        brightness = 0.2,
                        contrast = 0.1,
                        eyeOpenness = 0.2,
                    ),
            )

        val openEyesScore =
            simQWithEyeWeight.calculateFaceQuality(
                bitmap = bitmap,
                leftEyeOpenness = 1.0,
                rightEyeOpenness = 1.0,
            )

        val closedEyesScore =
            simQWithEyeWeight.calculateFaceQuality(
                bitmap = bitmap,
                leftEyeOpenness = 0.0,
                rightEyeOpenness = 0.0,
            )

        assertThat(openEyesScore).isAtLeast(closedEyesScore)
    }

    @Test
    fun calculateFaceQualityWithoutEyeOpennessIgnoresEyeWeight() {
        val bitmap = createTestBitmap()
        val simQWithEyeWeight = SimQ(faceWeights = QualityWeights(eyeOpenness = 0.2))

        val qualityNoEyes =
            simQWithEyeWeight.calculateFaceQuality(
                bitmap = bitmap,
                leftEyeOpenness = null,
                rightEyeOpenness = null,
            )

        assertThat(qualityNoEyes).isAtLeast(0.0f)
        assertThat(qualityNoEyes).isAtMost(1.0f)
    }

    @Test
    fun calculateFaceQualityWithOnlyLeftEyeOpennessProvided() {
        val bitmap = createTestBitmap()
        val simQWithEyeWeight = SimQ(faceWeights = QualityWeights(eyeOpenness = 0.2))

        val quality =
            simQWithEyeWeight.calculateFaceQuality(
                bitmap = bitmap,
                leftEyeOpenness = 1.0,
                rightEyeOpenness = null,
            )

        assertThat(quality).isIn(Range.closed(0.0f, 1.0f))
    }

    @Test
    fun calculateFaceQualityWithCustomWeightsAffectsResult() {
        val bitmap = createTestBitmap()
        val alignmentWeightedSimQ =
            SimQ(
                faceWeights =
                    QualityWeights(
                        alignment = 0.9,
                        blur = 0.025,
                        brightness = 0.025,
                        contrast = 0.025,
                        eyeOpenness = 0.025,
                    ),
            )
        val otherWeightedSimQ =
            SimQ(
                faceWeights =
                    QualityWeights(
                        alignment = 0.025,
                        blur = 0.325,
                        brightness = 0.325,
                        contrast = 0.325,
                        eyeOpenness = 0.0,
                    ),
            )

        val alignmentWeighted =
            alignmentWeightedSimQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 20.0,
                yaw = 20.0,
                roll = 20.0,
            )

        val otherWeighted =
            otherWeightedSimQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 20.0,
                yaw = 20.0,
                roll = 20.0,
            )

        assertThat(alignmentWeighted).isLessThan(otherWeighted)
    }

    @Test
    fun calculateFaceQualityWithCustomParametersAffectsThresholds() {
        val bitmap = createTestBitmap()
        val strictSimQ =
            SimQ(
                faceParameters =
                    QualityParameters(
                        maxAlignmentAngle = 10.0,
                        maxIndividualAngle = 20.0,
                    ),
            )
        val lenientSimQ =
            SimQ(
                faceParameters =
                    QualityParameters(
                        maxAlignmentAngle = 30.0,
                        maxIndividualAngle = 40.0,
                    ),
            )

        val strictQuality =
            strictSimQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 15.0,
                yaw = 15.0,
                roll = 15.0,
            )

        val lenientQuality =
            lenientSimQ.calculateFaceQuality(
                bitmap = bitmap,
                pitch = 15.0,
                yaw = 15.0,
                roll = 15.0,
            )

        assertThat(lenientQuality).isAtLeast(strictQuality)
    }

    @Test
    fun calculateFaceQualityHandlesDifferentBitmapSizes() {
        val smallBitmap = createTestBitmap(64, 64)
        val mediumBitmap = createTestBitmap(256, 256)
        val largeBitmap = createTestBitmap(1024, 1024)

        val smallQuality = simQ.calculateFaceQuality(smallBitmap)
        val mediumQuality = simQ.calculateFaceQuality(mediumBitmap)
        val largeQuality = simQ.calculateFaceQuality(largeBitmap)

        assertThat(smallQuality).isIn(Range.closed(0.0f, 1.0f))
        assertThat(mediumQuality).isIn(Range.closed(0.0f, 1.0f))
        assertThat(largeQuality).isIn(Range.closed(0.0f, 1.0f))
    }

    @Test
    fun calculateFaceQualityWithDefaultParametersReturnsReasonableScore() {
        val bitmap = createTestBitmap()
        val quality = simQ.calculateFaceQuality(bitmap)

        assertThat(quality).isIn(Range.closed(0.0f, 1.0f))
    }

    @Test
    fun calculateFaceQualityIsConsistentWithSameInputs() {
        val bitmap = createTestBitmap()

        val quality1 = simQ.calculateFaceQuality(bitmap, pitch = 10.0, yaw = 5.0, roll = -3.0)
        val quality2 = simQ.calculateFaceQuality(bitmap, pitch = 10.0, yaw = 5.0, roll = -3.0)

        assertThat(quality1).isWithin(0.001f).of(quality2)
    }

    @Test
    fun calculateFaceQualityWithZeroWeightsReturnsZeroOrHandledGracefully() {
        val bitmap = createTestBitmap()
        val zeroWeightsSimQ =
            SimQ(
                faceWeights =
                    QualityWeights(
                        alignment = 0.0,
                        blur = 0.0,
                        brightness = 0.0,
                        contrast = 0.0,
                        eyeOpenness = 0.0,
                    ),
            )

        val quality = zeroWeightsSimQ.calculateFaceQuality(bitmap = bitmap)

        assertThat(quality).isIn(Range.closed(0.0f, 1.0f))
    }
}
