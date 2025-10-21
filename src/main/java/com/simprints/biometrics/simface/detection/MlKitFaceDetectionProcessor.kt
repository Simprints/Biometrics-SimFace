package com.simprints.biometrics.simface.detection

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceLandmark
import com.simprints.biometrics.simface.Utils.clampToBounds
import com.simprints.biometrics.simface.data.FaceDetection
import com.simprints.biometrics.simface.data.FacialLandmarks
import com.simprints.biometrics.simface.data.Point2D
import com.simprints.simq.QualityParameters
import com.simprints.simq.QualityWeights
import com.simprints.simq.SimQ
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class MlKitFaceDetectionProcessor(
    private val faceDetector: FaceDetector,
) : FaceDetectionProcessor {
    override fun detectFace(
        image: Bitmap,
        onSuccess: (List<FaceDetection>) -> Unit,
        onFailure: (Exception) -> Unit,
        onCompleted: () -> Unit,
    ) {
        val inputImage = InputImage.fromBitmap(image, 0)

        faceDetector
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
                        quality = calculateFaceQuality(face, image),
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
        val inputImage = InputImage.fromBitmap(image, 0)

        return suspendCoroutine { continuation ->
            faceDetector
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
                            quality = calculateFaceQuality(face, image),
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
        image: Bitmap,
    ): Float {
        return try {
            val boundingBox = face.boundingBox.clampToBounds(image.width, image.height)
            val faceBitmap = Bitmap.createBitmap(
                image,
                boundingBox.left,
                boundingBox.top,
                boundingBox.width(),
                boundingBox.height()
            )

            val parameters = QualityParameters(
                maxAlignmentAngle = 20.0,
                maxIndividualAngle = 25.0,
                minBlur = 50.0,
                maxBlur = 100.0,
                minBrightness = 30.0,
                optimalBrightnessLow = 80.0,
                optimalBrightnessHigh = 150.0,
                maxBrightness = 190.0,
                brightnessSteepness = 0.3,
                minContrast = 30.0,
                maxContrast = 47.0
            )

            val weights = QualityWeights(
                alignment = 0.28,
                blur = 0.3,
                brightness = 0.1,
                contrast = 0.3,
                eyeOpenness = 0.02
            )

            val qualityScore = SimQ.calculateFaceQuality(
                bitmap = faceBitmap,
                pitch = face.headEulerAngleX.toDouble(),
                yaw = face.headEulerAngleY.toDouble(),
                roll = face.headEulerAngleZ.toDouble(),
                leftEyeOpenness = face.leftEyeOpenProbability?.toDouble(),
                rightEyeOpenness = face.rightEyeOpenProbability?.toDouble(),
                weights = weights,
                parameters = parameters
            )
            
            faceBitmap.recycle()
            
            qualityScore
        } catch (e: Exception) {
            0.0f
        }
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
