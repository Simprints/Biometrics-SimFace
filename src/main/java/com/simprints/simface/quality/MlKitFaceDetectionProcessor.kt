package com.simprints.simface.quality

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.simprints.simface.core.MLModelManager
import com.simprints.simface.core.SimFace
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

internal class MlKitFaceDetectionProcessor() : FaceDetectionProcessor {

    override fun detectFace(
        image: Bitmap,
        onSuccess: (List<SimFace>) -> Unit,
        onFailure: (Exception) -> Unit,
        onCompleted: () -> Unit
    ) {
        val detector = MLModelManager.getFaceDetector()
        val inputImage = InputImage.fromBitmap(image, 0)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val simFaces = mutableListOf<SimFace>()
                faces?.forEach { face ->
                    val simFace = SimFace(
                        sourceWidth = image.width,
                        sourceHeight = image.height,
                        absoluteBoundingBox = face.boundingBox,
                        yaw = face.headEulerAngleY,
                        roll = face.headEulerAngleZ,
                        quality = calculateFaceQuality(face, image.width, image.height)
                    )
                    simFaces.add(simFace)
                }
                onSuccess(simFaces)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }.addOnCompleteListener {
                onCompleted()
            }
    }

    override suspend fun detectFaceBlocking(image: Bitmap): List<SimFace> {
        val detector = MLModelManager.getFaceDetector()
        val inputImage = InputImage.fromBitmap(image, 0)

        return suspendCoroutine { continuation ->
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val simFaces = mutableListOf<SimFace>()
                    faces?.forEach { face ->
                        val simFace = SimFace(
                            sourceWidth = image.width,
                            sourceHeight = image.height,
                            absoluteBoundingBox = face.boundingBox,
                            yaw = face.headEulerAngleY,
                            roll = face.headEulerAngleZ,
                            quality = calculateFaceQuality(face, image.width, image.height)
                        )
                        simFaces.add(simFace)
                    }
                    continuation.resume(simFaces)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    override fun alignFace(bitmap: Bitmap, faceBoundingBox: Rect): Bitmap {
        // Ensure the Rect is valid and within the bounds of the Bitmap
        if (faceBoundingBox.left < 0 || faceBoundingBox.top < 0 || faceBoundingBox.right > bitmap.width || faceBoundingBox.bottom > bitmap.height) {
            throw IllegalArgumentException("The provided faceBoundingBox is out of the Bitmap bounds: $faceBoundingBox")
        }
        if (faceBoundingBox.width() <= 0 || faceBoundingBox.height() <= 0) {
            throw IllegalArgumentException("The provided faceBoundingBox has invalid dimensions: $faceBoundingBox")
        }

        return Bitmap.createBitmap(bitmap, faceBoundingBox.left, faceBoundingBox.top, faceBoundingBox.width(), faceBoundingBox.height())
    }

    private fun calculateFaceQuality(face: Face, imageWidth: Int, imageHeight: Int): Float {
        return try {
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

            // Just in case limit to 0-1 range
            return score.coerceIn(0.0, 1.0).toFloat()
        } catch (e: Exception) {
            println("Error calculating face quality: ${e.message}")
            0.0f
        }
    }

    private fun calculateEyeOpennessScore(face: Face): Double {
        val leftEyeScore = face.leftEyeOpenProbability ?: return 0.0
        val rightEyeScore = face.rightEyeOpenProbability ?: return 0.0

        return (leftEyeScore + rightEyeScore) / 2.0
    }
}
