package com.simprints.simface.quality

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.simprints.simface.core.MLModelManager
import com.simprints.simface.core.SimFace
import kotlinx.coroutines.tasks.await
import kotlin.math.absoluteValue

class FaceDetectionProcessor(): IFaceDetectionProcessoor {

    override suspend fun detectFace(bitmap: Bitmap): List<SimFace> {
        // Convert bitmap to ML Kit's InputImage format
        val image = InputImage.fromBitmap(bitmap, 0)
        val faces = MLModelManager.getFaceDetector()?.process(image)?.await()
        val simFaces = mutableListOf<SimFace>()

        faces?.forEach { face ->
            val croppedBitmap = cropBitmapToFace(bitmap, face.boundingBox)
            val simFace = SimFace(
                sourceWidth = bitmap.width,
                sourceHeight = bitmap.height,
                absoluteBoundingBox = face.boundingBox,
                yaw = face.headEulerAngleY,
                roll = face.headEulerAngleZ,
                quality = calculateFaceQuality(face, bitmap.width, bitmap.height)
            )
            simFaces.add(simFace)
        }

        return simFaces
    }

    private fun calculateFaceQuality(face: Face, imageWidth: Int, imageHeight: Int): Float {
        var score = 0.0

        // These should add to 1.0
        val faceRotationWeight = 0.3
        val faceTiltWeight = 0.05
        val faceNodWeight = 0.05
        val faceSizeWeight = 0.3
        val eyeOpennessWeight = 0.3

        // Face Rotation Score
        score += faceRotationWeight * (1.0 - (face.headEulerAngleY.absoluteValue / 90.0))

        // Face Tilt Score
        score += faceTiltWeight * (1.0 - (face.headEulerAngleZ.absoluteValue / 90.0))

        // Face Nod Score
        score += faceNodWeight * (1.0 - (face.headEulerAngleX.absoluteValue / 90.0))

        // Face Size Relative to Image Size
        val faceArea = face.boundingBox.width() * face.boundingBox.height()
        val imageArea = imageWidth * imageHeight
        score += faceSizeWeight * (faceArea.toDouble() / imageArea)

        // Eye Openness Score
        score += eyeOpennessWeight * calculateEyeOpennessScore(face)

        // TODO: Blur Detection
        // TODO: Brightness and Contrast Score

        return score.toFloat()
    }


    fun calculateEyeOpennessScore(face: Face): Double {
        val leftScore = face.leftEyeOpenProbability
        val rightScore = face.rightEyeOpenProbability

        val averageEyeOpenness =
            (rightScore?.let { leftScore?.plus(it) })?.div(2.0) // Use 2.0 to ensure floating-point division

        // Normalize the score to be between 0 and 1
        if (averageEyeOpenness == null) {
            return 0.0
        }

        return averageEyeOpenness
    }

    private fun cropBitmapToFace(originalBitmap: Bitmap, boundingBox: Rect): Bitmap {
        // Ensure the bounding box is within the original bitmap dimensions
        val left = boundingBox.left.coerceIn(0, originalBitmap.width)
        val top = boundingBox.top.coerceIn(0, originalBitmap.height)
        val right = boundingBox.right.coerceIn(0, originalBitmap.width)
        val bottom = boundingBox.bottom.coerceIn(0, originalBitmap.height)

        // Calculate the width and height of the cropped area
        val width = right - left
        val height = bottom - top

        // Check for valid dimensions
        return Bitmap.createBitmap(originalBitmap, left, top, width, height)
    }

}