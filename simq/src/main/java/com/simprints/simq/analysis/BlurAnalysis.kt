package com.simprints.simq.analysis

import android.graphics.Bitmap
import com.simprints.simq.utils.ScoringFunctions
import kotlin.math.pow
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.imgproc.Imgproc

internal object BlurAnalysis {
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
    ): Double =
            try {
                val laplacianVariance = calculateLaplacianVariance(bitmap)
                ScoringFunctions.rampScore(laplacianVariance, minBlur, maxBlur)
            } catch (e: Exception) {
                1.0 // Default to good score if OpenCV not available
            }

    /**
     * Calculates Laplacian blur variance using OpenCV.
     *
     * @param bitmap The face image to analyze
     * @param ksize Kernel size for Laplacian operator (default: 5)
     * @return Laplacian variance value
     */
    private fun calculateLaplacianVariance(
            bitmap: Bitmap,
            ksize: Int = 5,
    ): Double {
        val mat = Mat()
        val gray = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)

        val laplacian = Mat()
        Imgproc.Laplacian(gray, laplacian, CvType.CV_64F, ksize)

        val mean = MatOfDouble()
        val stddev = MatOfDouble()
        Core.meanStdDev(laplacian, mean, stddev)
        val laplacianVariance = stddev.toArray()[0].pow(2.0)

        mat.release()
        gray.release()
        laplacian.release()
        mean.release()
        stddev.release()

        return laplacianVariance
    }
}
