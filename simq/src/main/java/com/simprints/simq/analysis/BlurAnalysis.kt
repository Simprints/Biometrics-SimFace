package com.simprints.simq.analysis

import android.graphics.Bitmap
import com.simprints.simq.utils.ImageAnalyzer
import com.simprints.simq.utils.OpenCVImageAnalyzer
import com.simprints.simq.utils.ScoringFunctions

internal object BlurAnalysis {
    private var imageAnalyzer: ImageAnalyzer = OpenCVImageAnalyzer()

    /**
     * Calculates blur score using Laplacian variance.
     *
     * @param bitmap The face image to analyze
     * @param minBlur Minimum acceptable blur threshold
     * @param maxBlur Maximum blur threshold for optimal score
     * @return Blur score between 0.0 and 1.0
     */
    fun calculateScore(
        bitmap: Bitmap,
        minBlur: Double,
        maxBlur: Double,
    ): Double = try {
        val laplacianVariance = imageAnalyzer.calculateLaplacianVariance(bitmap)
        ScoringFunctions.rampScore(laplacianVariance, minBlur, maxBlur)
    } catch (e: Exception) {
        1.0 // Default to good score if OpenCV not available
    }
}
