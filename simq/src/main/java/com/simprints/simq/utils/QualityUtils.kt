package com.simprints.simq.utils

import android.graphics.Bitmap
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sqrt

object QualityUtils {
    
    /**
     * Ramp scoring function: linear interpolation between min and max.
     * 
     * @param x The input value to score
     * @param min Minimum threshold (scores 0.0 below this)
     * @param max Maximum threshold (scores 1.0 above this)
     * @return Score between 0.0 and 1.0
     */
    fun rampScore(x: Double, min: Double, max: Double): Double {
        return when {
            x < min -> 0.0
            x > max -> 1.0
            else -> (x - min) / (max - min)
        }
    }
    
    /**
     * Plateau scoring function: optimal range with smooth sigmoid falloff.
     * 
     * @param x The input value to score
     * @param centerLow Lower bound of optimal range
     * @param centerHigh Upper bound of optimal range
     * @param edgeLow Lower edge threshold
     * @param edgeHigh Upper edge threshold
     * @param steepness Steepness of sigmoid falloff
     * @return Score between 0.0 and 1.0
     */
    fun plateauScore(
        x: Double,
        centerLow: Double,
        centerHigh: Double,
        edgeLow: Double,
        edgeHigh: Double,
        steepness: Double
    ): Double {
        return when {
            x in centerLow..centerHigh -> 1.0
            x < centerLow -> 1.0 / (1.0 + exp(-steepness * (x - edgeLow)))
            else -> 1.0 / (1.0 + exp(steepness * (x - edgeHigh)))
        }.coerceIn(0.0, 1.0)
    }
    
    /**
     * Crops bitmap to center region with optional displacement.
     * 
     * @param bitmap The bitmap to crop
     * @param centerCrop Fraction of the bitmap to use (0.0-1.0)
     * @param horizontalDisplacement Horizontal displacement factor (-1.0 to 1.0)
     * @param verticalDisplacement Vertical displacement factor (-1.0 to 1.0)
     * @return Cropped bitmap
     */
    fun centerCropBitmap(
        bitmap: Bitmap,
        centerCrop: Float,
        horizontalDisplacement: Float,
        verticalDisplacement: Float
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val hAbsDisplacement = (width * horizontalDisplacement).toInt()
        val vAbsDisplacement = (height * verticalDisplacement).toInt()
        
        val cropWidth = (width * centerCrop).toInt()
        val cropHeight = (height * centerCrop).toInt()
        val startX = hAbsDisplacement + (width - cropWidth) / 2
        val startY = vAbsDisplacement + (height - cropHeight) / 2
        
        return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
    }
    
    /**
     * Resizes bitmap to a target area while maintaining aspect ratio.
     * 
     * @param bitmap The bitmap to resize
     * @param targetArea Target area in pixels (default: 65536 = 256x256)
     * @return Resized bitmap
     */
    fun resizeBitmap(bitmap: Bitmap, targetArea: Double = 65536.0): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val newHeight = sqrt(targetArea / aspectRatio)
        val newWidth = aspectRatio * newHeight
        
        return Bitmap.createScaledBitmap(
            bitmap,
            newWidth.roundToInt(),
            newHeight.roundToInt(),
            true
        )
    }
}
