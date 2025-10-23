package com.simprints.simq

/**
 * Default quality weights for face assessment.
 * These control how much each metric contributes to the final score.
 */
data class QualityWeights(
    val alignment: Double = 0.3,
    val blur: Double = 0.3,
    val brightness: Double = 0.3,
    val contrast: Double = 0.1,
    val eyeOpenness: Double = 0.0,
) {
    companion object {
        val DEFAULT = QualityWeights()
    }
}
