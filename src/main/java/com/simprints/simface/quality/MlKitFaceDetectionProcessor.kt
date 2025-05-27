package com.simprints.simface.quality

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import com.simprints.simface.core.MLModelManager
import com.simprints.simface.core.Utils.clampToBounds
import com.simprints.simface.data.FaceDetection
import com.simprints.simface.data.FacialLandmarks
import com.simprints.simface.data.Point2D
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

internal class MlKitFaceDetectionProcessor(
    private val modelManager: MLModelManager,
) : FaceDetectionProcessor {
    override fun detectFace(
        image: Bitmap,
        onSuccess: (List<FaceDetection>) -> Unit,
        onFailure: (Exception) -> Unit,
        onCompleted: () -> Unit,
    ) {
        val detector = modelManager.getFaceDetector()
        val inputImage = InputImage.fromBitmap(image, 0)

        detector
            .process(inputImage)
            .addOnSuccessListener { faces ->
                val faceDetections = mutableListOf<FaceDetection>()
                faces?.forEach { face ->
                    val faceDetection = FaceDetection(
                        sourceWidth = image.width,
                        sourceHeight = image.height,
                        absoluteBoundingBox = face.boundingBox.clampToBounds(
                            image.width,
                            image.height,
                        ),
                        yaw = face.headEulerAngleY,
                        roll = face.headEulerAngleZ,
                        landmarks = buildLandmarks(face),
                        quality = calculateFaceQuality(face, image.width, image.height),
                    )
                    faceDetections.add(faceDetection)
                }
                onSuccess(faceDetections)
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }.addOnCompleteListener {
                onCompleted()
            }
    }

    override suspend fun detectFaceBlocking(image: Bitmap): List<FaceDetection> {
        val detector = modelManager.getFaceDetector()
        val inputImage = InputImage.fromBitmap(image, 0)

        return suspendCoroutine { continuation ->
            detector
                .process(inputImage)
                .addOnSuccessListener { faces ->
                    val faceDetections = mutableListOf<FaceDetection>()
                    faces?.forEach { face ->
                        val faceDetection = FaceDetection(
                            sourceWidth = image.width,
                            sourceHeight = image.height,
                            absoluteBoundingBox = face.boundingBox.clampToBounds(
                                image.width,
                                image.height,
                            ),
                            yaw = face.headEulerAngleY,
                            roll = face.headEulerAngleZ,
                            quality = calculateFaceQuality(face, image.width, image.height),
                            landmarks = buildLandmarks(face),
                        )
                        faceDetections.add(faceDetection)
                    }
                    continuation.resume(faceDetections)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    private fun calculateFaceQuality(
        face: Face,
        imageWidth: Int,
        imageHeight: Int,
    ): Float {
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

    private fun buildLandmarks(face: Face): FacialLandmarks? {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
            ?: face.getContour(FaceContour.LEFT_EYE)?.points?.getOrNull(4)
            ?: return null

        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
            ?: face.getContour(FaceContour.RIGHT_EYE)?.points?.getOrNull(4)
            ?: return null

        val nose = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
            ?: face.getContour(FaceContour.NOSE_BRIDGE)?.points?.lastOrNull()
            ?: return null

        val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position
            ?: face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points?.lastOrNull()
            ?: return null

        val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
            ?: face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points?.firstOrNull()
            ?: return null

        return FacialLandmarks(
            Point2D(leftEye.x, leftEye.y),
            Point2D(rightEye.x, rightEye.y),
            Point2D(nose.x, nose.y),
            Point2D(mouthLeft.x, mouthLeft.y),
            Point2D(mouthRight.x, mouthRight.y),
        )
    }













}
