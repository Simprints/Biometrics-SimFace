package com.simprints.simq.analysis

import android.graphics.Bitmap
import com.simprints.simq.utils.QualityUtils
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object BrightnessAnalysis {
    
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
        steepness: Double
    ): Double {
        return try {
            val brightness = analyzeBrightness(bitmap)
            QualityUtils.plateauScore(brightness, centerLow, centerHigh, edgeLow, edgeHigh, steepness)
        } catch (e: Exception) {
            1.0
        }
    }
    
    /**
     * Analyzes brightness of bitmap using OpenCV.
     * 
     * @param bitmap The face image to analyze
     * @return Mean brightness value (0-255)
     */
    private fun analyzeBrightness(bitmap: Bitmap): Double {
        val mat = Mat()
        val gray = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val brightness = Core.mean(gray).`val`[0]
        mat.release()
        gray.release()
        return brightness
    }
}
