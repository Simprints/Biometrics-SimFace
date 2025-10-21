package com.simprints.simq

import android.graphics.Bitmap
import com.simprints.simq.analysis.AlignmentAnalysis
import com.simprints.simq.analysis.BlurAnalysis
import com.simprints.simq.analysis.BrightnessAnalysis
import com.simprints.simq.analysis.ContrastAnalysis
import com.simprints.simq.utils.OpenCVLoader
import com.simprints.simq.utils.QualityUtils

object SimQ {
    
    init {
        OpenCVLoader.init()
    }
    
    /**
     * Calculates face quality score (0.0 - 1.0).
     *
     * @param bitmap The cropped face bitmap
     * @param imageWidth Width of the original full image (not used in current implementation)
     * @param imageHeight Height of the original full image (not used in current implementation)
     * @param boundingBox Bounding box of the face in the original image (not used in current implementation)
     * @param pitch Face pitch angle in degrees (head nod, default: 0.0)
     * @param yaw Face yaw angle in degrees (head rotation, default: 0.0)
     * @param roll Face roll angle in degrees (head tilt, default: 0.0)
     * @param leftEyeOpenness Left eye openness probability (0.0-1.0, optional)
     * @param rightEyeOpenness Right eye openness probability (0.0-1.0, optional)
     * @param centerCrop Fraction of the bitmap to use for quality assessment (default: 0.5)
     * @param horizontalDisplacement Horizontal displacement for center crop (default: 0.0)
     * @param verticalDisplacement Vertical displacement for center crop (default: 0.0)
     * @param weights Custom weights for quality metrics (optional)
     * @param parameters Custom quality parameters (optional)
     * @return Quality score between 0.0 and 1.0, or 0.0 if calculation fails
     */
    @JvmStatic
    fun calculateFaceQuality(
        bitmap: Bitmap,
        pitch: Double = 0.0,
        yaw: Double = 0.0,
        roll: Double = 0.0,
        leftEyeOpenness: Double? = null,
        rightEyeOpenness: Double? = null,
        centerCrop: Float = 0.5f,
        horizontalDisplacement: Float = 0.0f,
        verticalDisplacement: Float = 0.0f,
        weights: QualityWeights = QualityWeights.DEFAULT,
        parameters: QualityParameters = QualityParameters.DEFAULT
    ): Float {
        return try {
            // Resize bitmap to target area (256x256 = 65536)
            val resizedBitmap = QualityUtils.resizeBitmap(bitmap, 65536.0)
            
            // Crop the bitmap
            val croppedBitmap = QualityUtils.centerCropBitmap(
                resizedBitmap,
                centerCrop,
                horizontalDisplacement,
                verticalDisplacement
            )
            
            var totalScore = 0.0
            var totalWeight = 0.0
            
            val alignmentScore = AlignmentAnalysis.calculateScore(
                pitch, yaw, roll,
                parameters.maxAlignmentAngle,
                parameters.maxIndividualAngle
            )
            totalScore += weights.alignment * alignmentScore
            totalWeight += weights.alignment
            
            val blurScore = BlurAnalysis.calculateScore(
                croppedBitmap,
                parameters.minBlur,
                parameters.maxBlur
            )
            totalScore += weights.blur * blurScore
            totalWeight += weights.blur
            
            val brightnessScore = BrightnessAnalysis.calculateScore(
                croppedBitmap,
                parameters.minBrightness,
                parameters.optimalBrightnessLow,
                parameters.optimalBrightnessHigh,
                parameters.maxBrightness,
                parameters.brightnessSteepness
            )
            totalScore += weights.brightness * brightnessScore
            totalWeight += weights.brightness
            
            val contrastScore = ContrastAnalysis.calculateScore(
                croppedBitmap,
                parameters.minContrast,
                parameters.maxContrast
            )
            totalScore += weights.contrast * contrastScore
            totalWeight += weights.contrast
            
            if (leftEyeOpenness != null && rightEyeOpenness != null) {
                val eyeScore = (leftEyeOpenness + rightEyeOpenness) / 2.0
                totalScore += weights.eyeOpenness * eyeScore
                totalWeight += weights.eyeOpenness
            }
            
            // Clean up
            if (croppedBitmap != bitmap && croppedBitmap != resizedBitmap) {
                croppedBitmap.recycle()
            }
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            
            // Normalize and clamp to 0-1 range
            val finalScore = if (totalWeight > 0) totalScore / totalWeight else 0.0
            finalScore.coerceIn(0.0, 1.0).toFloat()
            
        } catch (e: Exception) {
            0.0f
        }
    }
}
