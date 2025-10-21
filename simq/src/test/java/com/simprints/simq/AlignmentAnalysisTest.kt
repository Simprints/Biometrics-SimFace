package com.simprints.simq

import com.simprints.simq.analysis.AlignmentAnalysis
import org.junit.Assert
import org.junit.Test

class AlignmentAnalysisTest {

    private val maxAngle = 20.0
    private val maxIndividualAngle = 25.0

    @Test
    fun `perfect alignment returns score of 1_0`() {
        val score = AlignmentAnalysis.calculateScore(
            pitch = 0.0,
            yaw = 0.0,
            roll = 0.0,
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )
        Assert.assertEquals(1.0, score, 0.001)
    }

    @Test
    fun `angle at half max threshold returns score of 0_5`() {
        val halfAngle = maxAngle / 2
        val score = AlignmentAnalysis.calculateScore(
            pitch = halfAngle,
            yaw = halfAngle,
            roll = halfAngle,
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )
        Assert.assertEquals(0.5, score, 0.001)
    }

    @Test
    fun `negative angles are treated as absolute values`() {
        val score1 = AlignmentAnalysis.calculateScore(
            pitch = -10.0,
            yaw = -10.0,
            roll = -10.0,
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )

        val score2 = AlignmentAnalysis.calculateScore(
            pitch = 10.0,
            yaw = 10.0,
            roll = 10.0,
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )

        Assert.assertEquals(score1, score2, 0.001)
    }

    @Test
    fun `any single angle exceeding maxIndividualAngle rejects entire score`() {
        // Test pitch rejection
        val pitchScore = AlignmentAnalysis.calculateScore(
            pitch = 26.0,
            yaw = 0.0,
            roll = 0.0,
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )
        Assert.assertEquals(0.0, pitchScore, 0.001)

        // Test yaw rejection
        val yawScore = AlignmentAnalysis.calculateScore(
            pitch = 0.0,
            yaw = 26.0,
            roll = 0.0,
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )
        Assert.assertEquals(0.0, yawScore, 0.001)

        // Test roll rejection
        val rollScore = AlignmentAnalysis.calculateScore(
            pitch = 0.0,
            yaw = 0.0,
            roll = 26.0,
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )
        Assert.assertEquals(0.0, rollScore, 0.001)
    }

    @Test
    fun `mixed angles calculate average score correctly`() {
        // Perfect pitch, half-max yaw, max roll
        val score = AlignmentAnalysis.calculateScore(
            pitch = 0.0,      // Score: 1.0
            yaw = 10.0,       // Score: 0.5 (10/20)
            roll = 20.0,      // Score: 0.0 (20/20)
            maxAngle = maxAngle,
            maxIndividualAngle = maxIndividualAngle
        )
        // Average: (1.0 + 0.5 + 0.0) / 3 = 0.5
        Assert.assertEquals(0.5, score, 0.001)
    }
}
