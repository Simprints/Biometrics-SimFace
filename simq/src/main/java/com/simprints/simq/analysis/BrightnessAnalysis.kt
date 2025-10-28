package com.simprints.simq.analysis

import android.graphics.Bitmap
import com.simprints.simq.utils.ImageAnalyzer
import com.simprints.simq.utils.OpenCVImageAnalyzer
import com.simprints.simq.utils.ScoringFunctions

internal object BrightnessAnalysis {
    private var imageAnalyzer: ImageAnalyzer = OpenCVImageAnalyzer()

    /**
     * Calculates brightness score using plateau function.
     *
     * @param bitmap The face image to analyze
     * @param edgeLow Lower edge threshold (minimum acceptable brightness)
     * @param centerLow Lower center threshold (start of optimal range)
     * @param centerHigh Upper center threshold (end of optimal range)
     * @param edgeHigh Upper edge threshold (maximum acceptable brightness)
     * @param steepness Steepness of the sigmoid falloff
     * @return Brightness score between 0.0 and 1.0
     */
    fun calculateScore(
        bitmap: Bitmap,
        edgeLow: Double,
        centerLow: Double,
        centerHigh: Double,
        edgeHigh: Double,
        steepness: Double,
    ): Double = try {
        val brightness = imageAnalyzer.calculateBrightness(bitmap)
        ScoringFunctions.plateauScore(
            brightness,
            centerLow,
            centerHigh,
            edgeLow,
            edgeHigh,
            steepness,
        )
    } catch (e: Exception) {
        1.0
    }
}
