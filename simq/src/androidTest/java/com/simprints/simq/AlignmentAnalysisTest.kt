package com.simprints.simq

import com.google.common.truth.Truth.assertThat
import com.simprints.simq.analysis.AlignmentAnalysis
import org.junit.Test

class AlignmentAnalysisTest {
    private val maxAngle = 20.0
    private val maxIndividualAngle = 25.0

    @Test
    fun perfectAlignmentReturnsScoreOf1() {
        val score =
            AlignmentAnalysis.calculateScore(
                pitch = 0.0,
                yaw = 0.0,
                roll = 0.0,
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )
        assertThat(score).isWithin(0.001).of(1.0)
    }

    @Test
    fun angleAtHalfMaxThresholdReturnsScoreOf0_5() {
        val halfAngle = maxAngle / 2
        val score =
            AlignmentAnalysis.calculateScore(
                pitch = halfAngle,
                yaw = halfAngle,
                roll = halfAngle,
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )
        assertThat(score).isWithin(0.001).of(0.5)
    }

    @Test
    fun negativeAnglesAreTreatedAsAbsoluteValues() {
        val score1 =
            AlignmentAnalysis.calculateScore(
                pitch = -10.0,
                yaw = -10.0,
                roll = -10.0,
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )

        val score2 =
            AlignmentAnalysis.calculateScore(
                pitch = 10.0,
                yaw = 10.0,
                roll = 10.0,
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )

        assertThat(score1).isWithin(0.001).of(score2)
    }

    @Test
    fun anySingleAngleExceedingMaxIndividualAngleRejectsEntireScore() {
        // Test pitch rejection
        val pitchScore =
            AlignmentAnalysis.calculateScore(
                pitch = 26.0,
                yaw = 0.0,
                roll = 0.0,
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )
        assertThat(pitchScore).isWithin(0.001).of(0.0)

        // Test yaw rejection
        val yawScore =
            AlignmentAnalysis.calculateScore(
                pitch = 0.0,
                yaw = 26.0,
                roll = 0.0,
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )
        assertThat(yawScore).isWithin(0.001).of(0.0)

        // Test roll rejection
        val rollScore =
            AlignmentAnalysis.calculateScore(
                pitch = 0.0,
                yaw = 0.0,
                roll = 26.0,
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )
        assertThat(rollScore).isWithin(0.001).of(0.0)
    }

    @Test
    fun mixedAnglesCalculateAverageScoreCorrectly() {
        // Perfect pitch, half-max yaw, max roll
        val score =
            AlignmentAnalysis.calculateScore(
                pitch = 0.0, // Score: 1.0
                yaw = 10.0, // Score: 0.5 (10/20)
                roll = 20.0, // Score: 0.0 (20/20)
                maxAngle = maxAngle,
                maxIndividualAngle = maxIndividualAngle,
            )
        // Average: (1.0 + 0.5 + 0.0) / 3 = 0.5
        assertThat(score).isWithin(0.001).of(0.5)
    }
}
