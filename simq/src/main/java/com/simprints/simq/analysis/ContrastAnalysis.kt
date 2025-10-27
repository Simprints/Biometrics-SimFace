package com.simprints.simq.analysis

import android.graphics.Bitmap
import com.simprints.simq.utils.ImageAnalyzer
import com.simprints.simq.utils.OpenCVImageAnalyzer
import com.simprints.simq.utils.ScoringFunctions

internal object ContrastAnalysis {
    private var imageAnalyzer: ImageAnalyzer = OpenCVImageAnalyzer()

    /**
     * Calculates contrast score using standard deviation.
     *
     * @param bitmap The face image to analyze
     * @param minContrast Minimum acceptable contrast threshold
     * @param maxContrast Maximum contrast threshold for optimal score
     * @return Contrast score between 0.0 and 1.0
     */
    fun calculateScore(
            bitmap: Bitmap,
            minContrast: Double,
            maxContrast: Double,
    ): Double =
            try {
                val contrast = imageAnalyzer.calculateContrast(bitmap)
                ScoringFunctions.rampScore(contrast, minContrast, maxContrast)
            } catch (e: Exception) {
                1.0 // Default to good score if OpenCV not available
            }
}
