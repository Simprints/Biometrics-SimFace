package com.simprints.biometrics.simface.detection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.simprints.biometrics.simface.Constants.IMAGE_SIZE
import com.simprints.biometrics.simface.data.FacialLandmarks
import org.ejml.dense.row.SingularOps_DDRM
import org.ejml.simple.SimpleMatrix
import kotlin.math.sqrt

internal fun cropAlignFace(
    bitmap: Bitmap,
    faceBoundingBox: Rect,
): Bitmap {
    // Ensure the Rect is valid and within the bounds of the Bitmap
    if (faceBoundingBox.left < 0 ||
        faceBoundingBox.top < 0 ||
        faceBoundingBox.right > bitmap.width ||
        faceBoundingBox.bottom > bitmap.height
    ) {
        throw IllegalArgumentException("The provided faceBoundingBox is out of the Bitmap bounds: $faceBoundingBox")
    }
    if (faceBoundingBox.width() <= 0 || faceBoundingBox.height() <= 0) {
        throw IllegalArgumentException("The provided faceBoundingBox has invalid dimensions: $faceBoundingBox")
    }

    return Bitmap.createBitmap(bitmap, faceBoundingBox.left, faceBoundingBox.top, faceBoundingBox.width(), faceBoundingBox.height())
}

internal fun warpAlignFace(
    inputImage: Bitmap,
    landmarks: FacialLandmarks?,
): Bitmap {
    if (landmarks == null) return inputImage

    val ref = arrayOf(
        floatArrayOf(38.2946f, 51.6963f), // Left eye reference point in (112, 112) image
        floatArrayOf(73.5318f, 51.5014f), // Right eye reference point in (112, 112) image
        floatArrayOf(56.0252f, 71.7366f), // Nose reference point in (112, 112) image
        floatArrayOf(41.5493f, 92.3655f), // Mouth left reference point in (112, 112) image
        floatArrayOf(70.7299f, 92.2041f), // Mouth right reference point in (112, 112) image
    )

    val landmarks = landmarkSetToArray(landmarks) // changes format of landmarks
    val tfm = computeTFM(landmarks, ref) // computes 2x3 transformation matrix (tfm)
    val warped = warpAffine(inputImage, tfm) // performs transformation

    return warped
}

/**
 * Restructures the facial landmark points for more convenient later processing.
 */
private fun landmarkSetToArray(landmarkSet: FacialLandmarks): Array<FloatArray> = arrayOf(
    // Do not change the order of the different landmarks
    floatArrayOf(landmarkSet.eyeLeft.x, landmarkSet.eyeLeft.y),
    floatArrayOf(landmarkSet.eyeRight.x, landmarkSet.eyeRight.y),
    floatArrayOf(landmarkSet.nose.x, landmarkSet.nose.y),
    floatArrayOf(landmarkSet.mouthLeft.x, landmarkSet.mouthLeft.y),
    floatArrayOf(landmarkSet.mouthRight.x, landmarkSet.mouthRight.y),
)

/**
 * Computes the transformation matrix.
 *
 * @param uv facial landmarks points detected by MLKit
 * @param xy reference points in the final bitmap
 * @return tfm transformation function (sized 2x3)
 */
private fun computeTFM(
    uv: Array<FloatArray>,
    xy: Array<FloatArray>,
): SimpleMatrix {
    // Solve for trans1
    val trans1 = findNonReflectiveSimilarity(uv, xy)

    // Reflect xy across Y-axis
    val xyR = xy.map { floatArrayOf(-it[0], it[1]) }.toTypedArray()

    val trans2r = findNonReflectiveSimilarity(uv, xyR)

    // Reflect transform matrix
    val tReflectY = SimpleMatrix(
        arrayOf(
            doubleArrayOf(-1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0),
        ),
    )

    val trans2 = trans2r.mult(tReflectY)

    // Compare which transform is better
    val xy1 = apply2dAffineTransformation(trans1, uv)
    val norm1 = computeNorm(xy1, xyR)

    val xy2 = apply2dAffineTransformation(trans2, uv)
    val norm2 = computeNorm(xy2, xyR)

    val trans = when {
        norm1.isNaN() && norm2.isNaN() -> trans1 // fallback if both are bad
        norm1.isNaN() -> trans2
        norm2.isNaN() -> trans1
        norm1 <= norm2 -> trans1
        trans1.isIdentical(SimpleMatrix.identity(3), 1e-8) -> trans1
        else -> trans2
    }
    return trans.extractMatrix(0, 3, 0, 2).transpose()
}

/**
 * Estimates a non-reflective similarity transformation that maps a set of source landmarks
 * to a set of target landmarks. It computes the optimal similarity transformation
 * (consisting of scaling, rotation, and translation — but excluding reflection) that best
 * aligns the source points to the target points in a least squares sense.
 *
 * @param facialLandmarks the initial facial landmarks
 * @param referencePoints the reference points on the output bitmap
 */
private fun findNonReflectiveSimilarity(
    facialLandmarks: Array<FloatArray>,
    referencePoints: Array<FloatArray>,
): SimpleMatrix {
    val referencePointCount = referencePoints.size

    // Extract x and y from referencePoints
    val x = SimpleMatrix(referencePointCount, 1).apply {
        for (i in 0 until referencePointCount) {
            this[i, 0] = referencePoints[i][0].toDouble()
        }
    }
    val y = SimpleMatrix(referencePointCount, 1).apply {
        for (i in 0 until referencePointCount) {
            this[i, 0] = referencePoints[i][1].toDouble()
        }
    }

    // Build matrix X
    val ones = SimpleMatrix(referencePointCount, 1).apply { fill(1.0) }
    val zeros = SimpleMatrix(referencePointCount, 1).apply { fill(0.0) }
    val tmp1 = x.concatColumns(y, ones, zeros)
    val tmp2 = y.concatColumns(x.scale(-1.0), zeros, ones)
    val concatenatedMatrices = tmp1.concatRows(tmp2)

    // Extract u and v from fac
    val u = SimpleMatrix(referencePointCount, 1).also { m -> facialLandmarks.forEachIndexed { i, row -> m[i] = row[0].toDouble() } }
    val v = SimpleMatrix(referencePointCount, 1).also { m -> facialLandmarks.forEachIndexed { i, row -> m[i] = row[1].toDouble() } }
    val uv = u.concatRows(v)

    val rank = SingularOps_DDRM.rank(concatenatedMatrices.ddrm, 1e-6)

    // Solve X * r = U using least squares
    if (rank >= 4) {
        val r = concatenatedMatrices.pseudoInverse().mult(uv)
        val sc = r[0]
        val ss = r[1]
        val tx = r[2]
        val ty = r[3]

        // Build Tinv matrix
        val tInverted = SimpleMatrix(
            3,
            3,
            true,
            sc,
            -ss,
            0.0,
            ss,
            sc,
            0.0,
            tx,
            ty,
            1.0,
        )

        // Invert Tinv to get T
        val t = tInverted.invert()
        t[0, 2] = 0.0
        t[1, 2] = 0.0
        t[2, 2] = 1.0

        return t
    } else {
        return SimpleMatrix.identity(3)
    }
}

/**
 * Applies a 2D affine transformation to a set of points.
 */
private fun apply2dAffineTransformation(
    trans: SimpleMatrix,
    uv: Array<FloatArray>,
): Array<FloatArray> {
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
            xyMatrix.get(i, 1).toFloat(),
        )
    }
}

/**
 * Computes norm between two arrays.
 */
private fun computeNorm(
    a: Array<FloatArray>,
    b: Array<FloatArray>,
): Double {
    var sum = 0.0
    for (i in a.indices) {
        val dx = a[i][0] - b[i][0]
        val dy = a[i][1] - b[i][1]
        sum += (dx * dx + dy * dy).toDouble()
    }
    return sqrt(sum)
}

/**
 * Performs the warp affine transformation reproducing the behaviour of
 * openCV's warpAffine. It takes as input the original image and the transformation matrix
 * to return a (112, 112) sized aligned image that can be directly processed by the face
 * recognition model.
 */
private fun warpAffine(
    inputBitmap: Bitmap,
    tfm: SimpleMatrix, // 2x3 matrix from EJML
    cropWidth: Int = IMAGE_SIZE,
    cropHeight: Int = IMAGE_SIZE,
): Bitmap {
    // If tfm is structured so that it leaves all points unchanged (due to failures in previous
    // computing steps), then the original image is returned.
    if (
        tfm.isIdentical(
            SimpleMatrix(arrayOf(doubleArrayOf(1.0, 0.0, 0.0), doubleArrayOf(0.0, 1.0, 0.0))),
            1e-8,
        )
    ) {
        return inputBitmap
    }

    val outputBitmap = createBitmap(cropWidth, cropHeight)

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
