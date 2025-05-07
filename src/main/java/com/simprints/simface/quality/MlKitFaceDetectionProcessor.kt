package com.simprints.simface.quality
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import com.simprints.simface.core.FacialLandmarks
import com.simprints.simface.core.MLModelManager
import com.simprints.simface.core.Point2D
import com.simprints.simface.core.SimFace
import com.simprints.simface.core.Utils.clampToBounds
import org.ejml.dense.row.SingularOps_DDRM
import org.ejml.simple.SimpleMatrix
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue
import kotlin.math.sqrt

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
                        absoluteBoundingBox = face.boundingBox.clampToBounds(
                            image.width,
                            image.height
                        ),
                        yaw = face.headEulerAngleY,
                        roll = face.headEulerAngleZ,
                        landmarks = buildLandmarks(face),
                        quality = calculateFaceQuality(face, image.width, image.height),
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
                            absoluteBoundingBox = face.boundingBox.clampToBounds(
                                image.width,
                                image.height
                            ),
                            yaw = face.headEulerAngleY,
                            roll = face.headEulerAngleZ,
                            quality = calculateFaceQuality(face, image.width, image.height),
                            landmarks = buildLandmarks(face),
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

    private fun buildLandmarks(face: Face): FacialLandmarks? {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
            ?: face.getContour(FaceContour.LEFT_EYE)?.points?.getOrNull(4)
            ?: return null

        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
            ?: face.getContour(FaceContour.RIGHT_EYE)?.points?.getOrNull(4)
            ?: return null

        val nose = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
            ?: face.getContour(FaceContour.NOSE_BRIDGE)?.takeIf { it.points.isNotEmpty() }?.points?.last()
            ?: return null

        val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position
            ?: face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.takeIf { it.points.isNotEmpty() }?.points?.last()
            ?: return null

        val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
            ?: face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.takeIf { it.points.isNotEmpty() }?.points?.first()
            ?: return null

        return FacialLandmarks(
            Point2D(leftEye.x, leftEye.y),
            Point2D(rightEye.x, rightEye.y),
            Point2D(nose.x, nose.y),
            Point2D(mouthLeft.x, mouthLeft.y),
            Point2D(mouthRight.x, mouthRight.y)
        )
    }


    override fun warpAlignFace(face: FacialLandmarks, inputImage: Bitmap): Bitmap {
        /**
         * Takes as input the facial landmarks obtained by MLKit and the original image
         * and it returns a (112, 112) image, so that it can be directly processed by the face
         * recognition model. A warp transformation is applied to the original image so that in the
         * output image the positions of the landmarks corresponds the expected ones, which are
         * defined here in the ref variable.
         */

        val ref = arrayOf(
            floatArrayOf(38.2946f, 51.6963f),  // Left eye reference point in (112, 112) image
            floatArrayOf(73.5318f, 51.5014f),  // Right eye reference point in (112, 112) image
            floatArrayOf(56.0252f, 71.7366f),  // Nose reference point in (112, 112) image
            floatArrayOf(41.5493f, 92.3655f),  // Mouth left reference point in (112, 112) image
            floatArrayOf(70.7299f, 92.2041f)   // Mouth right reference point in (112, 112) image
        )

        val landmarks = landmarkSetToArray(face)  // changes format of landmarks
        val tfm = computeTFM(landmarks, ref) // computes 2x3 transformation matrix (tfm)
        val warped = warpAffine(inputImage, tfm)  // performs transformation

        return warped
    }

    fun warpAffine(
        inputBitmap: Bitmap,
        tfm: SimpleMatrix,   // 2x3 matrix from EJML
        cropWidth: Int = 112,
        cropHeight: Int = 112
    ): Bitmap {
        /**
         * Performs the warp affine transformation reproducing the behaviour of
         * openCV's warpAffine. It takes as input the original image and the transformation matrix
         * to return a (112, 112) sized aligned image that can be directly processed by the face
         * recognition model.
         */

        /*
        If tfm is structured so that it leaves all points unchanged (due to failures in previous
        computing steps), then the original image is returned.
         */
        if (tfm.isIdentical(SimpleMatrix(arrayOf(
                doubleArrayOf(1.0, 0.0, 0.0),
                doubleArrayOf( 0.0, 1.0, 0.0))), 1e-8)) {
            return inputBitmap
        }

        val outputBitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888)

        // Extract matrix values from tfm (2x3)
        val values = floatArrayOf(
            tfm[0, 0].toFloat(), // scale X
            tfm[0, 1].toFloat(), // skew X
            tfm[0, 2].toFloat(), // translate X
            tfm[1, 0].toFloat(), // skew Y
            tfm[1, 1].toFloat(), // scale Y
            tfm[1, 2].toFloat(), // translate Y
            0f, // perspective X
            0f, // perspective Y
            1f, // perspective scale
        )

        // Create Android Matrix with the transformation
        val androidMatrix = Matrix().apply {
            setValues(values)
        }

        // Apply transformation to canvas
        val canvas = Canvas(outputBitmap)
        canvas.drawBitmap(inputBitmap, androidMatrix, null)

        return outputBitmap
    }

    fun findNonreflectiveSimilarity(fac: Array<FloatArray>, ref: Array<FloatArray>): SimpleMatrix {
        /**
         * Estimates a non-reflective similarity transformation that maps a set of source landmarks
         * to a set of target landmarks. It computes the optimal similarity transformation
         * (consisting of scaling, rotation, and translation — but excluding reflection) that best
         * aligns the source points to the target points in a least squares sense.
         *
         * @param fac the initial facial landmarks
         * @param ref the reference points on the output bitmap
         */

        val K = 2
        val M = ref.size

        // Extract x and y from ref
        val x = SimpleMatrix(M, 1).apply {
            for (i in 0 until M) {
                this[i, 0] = ref[i][0].toDouble()
            }
        }

        val y = SimpleMatrix(M, 1).apply {
            for (i in 0 until M) {
                this[i, 0] = ref[i][1].toDouble()
            }
        }

        // Build matrix X
        val ones = SimpleMatrix(M, 1).apply { fill(1.0) }
        val zeros = SimpleMatrix(M, 1).apply { fill(0.0) }


        val tmp1 = x.concatColumns(y, ones, zeros)
        val tmp2 = y.concatColumns(x.scale(-1.0), zeros, ones)

        val X = tmp1.concatRows(tmp2)

        // Extract u and v from fac
        val u = SimpleMatrix(M, 1).also { m -> fac.forEachIndexed { i, row -> m[i] = row[0].toDouble() } }
        val v = SimpleMatrix(M, 1).also { m -> fac.forEachIndexed { i, row -> m[i] = row[1].toDouble() } }

        val U = u.concatRows(v)

        val rank = SingularOps_DDRM.rank(X.ddrm, 1e-6)

        // Solve X * r = U using least squares
        if (rank >= 2 * K) {
            val r = X.pseudoInverse().mult(U)

            val sc = r[0]
            val ss = r[1]
            val tx = r[2]
            val ty = r[3]

            // Build Tinv matrix
            val Tinv = SimpleMatrix(3, 3, true,
                sc, -ss, 0.0,
                ss,  sc, 0.0,
                tx,  ty, 1.0
            )

            // Invert Tinv to get T
            val T = Tinv.invert()
            T[0, 2] = 0.0
            T[1, 2] = 0.0
            T[2, 2] = 1.0

             return T
        } else {
            return SimpleMatrix.identity(3)

        }
    }

    fun computeTFM(uv: Array<FloatArray>, xy: Array<FloatArray>): SimpleMatrix {
        /**
         * Computes the transformation matrix. It calls another function called
         * findNonreflectiveSimilarity. If this fails, it returns the identity matrix sized 3.
         * Then, computeTFM returns a transformation functions so that the original
         * points are left unchanged. Ultimately, in case of failure the whole pipeline returns
         * the unchanged original image.
         *
         * @param uv facial landmarks points detected by MLKit
         * @param xy reference points in the final bitmap
         * @return tfm transformation function (sized 2x3)
         */

        // Solve for trans1
        val trans1 = findNonreflectiveSimilarity(uv, xy)

        // Reflect xy across Y-axis
        val xyR = xy.map { floatArrayOf(-it[0], it[1]) }.toTypedArray()

        val trans2r = findNonreflectiveSimilarity(uv, xyR)

        // Reflect transform matrix
        val TreflectY = SimpleMatrix(arrayOf(
            doubleArrayOf(-1.0, 0.0, 0.0),
            doubleArrayOf( 0.0, 1.0, 0.0),
            doubleArrayOf( 0.0, 0.0, 1.0)
        ))

        val trans2 = trans2r.mult(TreflectY)

        // Compare which transform is better
        val xy1 = tformfwd(trans1, uv)
        val norm1 = computeNorm(xy1, xyR)

        val xy2 = tformfwd(trans2, uv)
        val norm2 = computeNorm(xy2, xyR)

        val trans = when {
            norm1.isNaN() && norm2.isNaN() -> trans1 // fallback if both are bad
            norm1.isNaN() -> trans2
            norm2.isNaN() -> trans1
            norm1 <= norm2 -> trans1
            trans1.isIdentical(SimpleMatrix.identity(3), 1e-8) -> trans1
            else -> trans2
        }

        val tfm = trans.extractMatrix(0, 3, 0, 2).transpose()

        return tfm
    }

    fun tformfwd(trans: SimpleMatrix, uv: Array<FloatArray>): Array<FloatArray> {
        /**
         * Applies a 2D affine transformation to a set of points.
         * Called by findNonreflectiveSimilarity
         */
        val numPoints = uv.size
        val uvMatrix = SimpleMatrix(numPoints, 3)
        for (i in 0 until numPoints) {
            uvMatrix.set(i, 0, uv[i][0].toDouble())
            uvMatrix.set(i, 1, uv[i][1].toDouble())
            uvMatrix.set(i, 2, 1.0)
        }

        val xyMatrix = uvMatrix.mult(trans)

        return Array(numPoints) { i ->
            floatArrayOf(
                xyMatrix.get(i, 0).toFloat(),
                xyMatrix.get(i, 1).toFloat()
            )
        }
    }

    fun computeNorm(a: Array<FloatArray>, b: Array<FloatArray>): Double {
        /**
         * Computes norm between two arrays.
         */
        var sum = 0.0
        for (i in a.indices) {
            val dx = a[i][0] - b[i][0]
            val dy = a[i][1] - b[i][1]
            sum += (dx * dx + dy * dy).toDouble()
        }
        return sqrt(sum)
    }

    fun landmarkSetToArray(landmarkSet: FacialLandmarks): Array<FloatArray> {
        /**
         * Restructures the facial landmark points for more convenient later processing.
         */
        // Do not change the order of the different landmarks
        return arrayOf(
            floatArrayOf(landmarkSet.eyeLeft.x, landmarkSet.eyeLeft.y),
            floatArrayOf(landmarkSet.eyeRight.x, landmarkSet.eyeRight.y),
            floatArrayOf(landmarkSet.nose.x, landmarkSet.nose.y),
            floatArrayOf(landmarkSet.mouthLeft.x, landmarkSet.mouthLeft.y),
            floatArrayOf(landmarkSet.mouthRight.x, landmarkSet.mouthRight.y),
        )
    }


}
