package com.simprints.simq.analysis

import android.graphics.Bitmap
import com.simprints.simq.utils.ScoringFunctions
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.imgproc.Imgproc

object ContrastAnalysis {
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
    ): Double = try {
        val contrast = analyzeContrast(bitmap)
        ScoringFunctions.rampScore(contrast, minContrast, maxContrast)
    } catch (e: Exception) {
        1.0 // Default to good score if OpenCV not available
    }

    /**
     * Analyzes contrast of bitmap using OpenCV (standard deviation).
     *
     * @param bitmap The face image to analyze
     * @return Standard deviation value representing contrast
     */
    private fun analyzeContrast(bitmap: Bitmap): Double {
        val mat = Mat()
        val gray = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)

        val mean = MatOfDouble()
        val stddev = MatOfDouble()
        Core.meanStdDev(gray, mean, stddev)
        val stdDevValue = stddev.toArray()[0]

        mat.release()
        gray.release()
        mean.release()
        stddev.release()

        return stdDevValue
    }
}
