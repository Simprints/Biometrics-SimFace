package com.simprints.simq

data class QualityParameters(
    // Alignment thresholds
    val maxAlignmentAngle: Double = 20.0,
    val maxIndividualAngle: Double = 25.0,
    // Blur thresholds (Laplacian variance)
    val minBlur: Double = 50_000.0,
    val maxBlur: Double = 100_000.0,
    // Brightness thresholds (0-255)
    val minBrightness: Double = 30.0,
    val optimalBrightnessLow: Double = 80.0,
    val optimalBrightnessHigh: Double = 150.0,
    val maxBrightness: Double = 190.0,
    val brightnessSteepness: Double = 0.3,
    // Contrast thresholds (std dev)
    val minContrast: Double = 30.0,
    val maxContrast: Double = 47.0,
) {
    companion object {
        val DEFAULT = QualityParameters()
    }
}
