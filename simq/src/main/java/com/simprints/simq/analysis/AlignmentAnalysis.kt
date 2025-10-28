package com.simprints.simq.analysis

import kotlin.math.absoluteValue

internal object AlignmentAnalysis {
    /**
     * Calculates alignment score based on pitch, yaw, and roll angles.
     *
     * @param pitch Face pitch angle in degrees (head nod)
     * @param yaw Face yaw angle in degrees (head rotation)
     * @param roll Face roll angle in degrees (head tilt)
     * @param maxAngle Maximum acceptable angle for quality scoring
     * @param maxIndividualAngle Absolute maximum angle before rejection
     * @return Alignment score between 0.0 and 1.0
     */
    fun calculateScore(
        pitch: Double,
        yaw: Double,
        roll: Double,
        maxAngle: Double,
        maxIndividualAngle: Double,
    ): Double {
        // Reject if any angle is too extreme
        if (pitch.absoluteValue > maxIndividualAngle ||
            yaw.absoluteValue > maxIndividualAngle ||
            roll.absoluteValue > maxIndividualAngle
        ) {
            return 0.0
        }

        val pitchScore = maxOf(0.0, 1.0 - (pitch.absoluteValue / maxAngle))
        val yawScore = maxOf(0.0, 1.0 - (yaw.absoluteValue / maxAngle))
        val rollScore = maxOf(0.0, 1.0 - (roll.absoluteValue / maxAngle))

        return (pitchScore + yawScore + rollScore) / 3.0
    }
}
