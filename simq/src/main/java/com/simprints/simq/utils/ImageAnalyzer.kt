package com.simprints.simq.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.imgproc.Imgproc
import kotlin.math.pow

/**
 * Interface for image quality analysis operations. Provides high-level methods for analyzing image
 * properties like brightness, blur, and contrast.
 */
interface ImageAnalyzer {
    /**
     * Calculates the mean brightness value of a bitmap image.
     *
     * @param bitmap The input bitmap image
     * @return Mean brightness value (0-255)
     */
    fun calculateBrightness(bitmap: Bitmap): Double

    /**
     * Calculates the Laplacian variance to measure image sharpness/blur. Higher values indicate
     * sharper images, lower values indicate more blur.
     *
     * @param bitmap The input bitmap image
     * @param kernelSize Kernel size for the Laplacian operator (default: 5)
     * @return Laplacian variance value
     */
    fun calculateLaplacianVariance(
        bitmap: Bitmap,
        kernelSize: Int = 5,
    ): Double

    /**
     * Calculates the standard deviation to measure image contrast. Higher values indicate higher
     * contrast.
     *
     * @param bitmap The input bitmap image
     * @return Standard deviation value representing contrast
     */
    fun calculateContrast(bitmap: Bitmap): Double
}

internal class OpenCVImageAnalyzer : ImageAnalyzer {
    override fun calculateBrightness(bitmap: Bitmap): Double {
        val mat = Mat()
        val gray = Mat()
        try {
            Utils.bitmapToMat(bitmap, mat)
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
            return Core.mean(gray).`val`[0]
        } finally {
            mat.release()
            gray.release()
        }
    }

    override fun calculateLaplacianVariance(
        bitmap: Bitmap,
        kernelSize: Int,
    ): Double {
        val mat = Mat()
        val gray = Mat()
        val laplacian = Mat()
        val mean = MatOfDouble()
        val stddev = MatOfDouble()
        try {
            Utils.bitmapToMat(bitmap, mat)
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
            Imgproc.Laplacian(gray, laplacian, CvType.CV_64F, kernelSize)
            Core.meanStdDev(laplacian, mean, stddev)
            return stddev.toArray()[0].pow(2.0)
        } finally {
            mat.release()
            gray.release()
            laplacian.release()
            mean.release()
            stddev.release()
        }
    }

    override fun calculateContrast(bitmap: Bitmap): Double {
        val mat = Mat()
        val gray = Mat()
        val mean = MatOfDouble()
        val stddev = MatOfDouble()
        try {
            Utils.bitmapToMat(bitmap, mat)
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
            Core.meanStdDev(gray, mean, stddev)
            return stddev.toArray()[0]
        } finally {
            mat.release()
            gray.release()
            mean.release()
            stddev.release()
        }
    }
}
